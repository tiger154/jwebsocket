//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Twitter Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.twitter;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 *
 * @author aschulze logout see:
 * http://stackoverflow.com/questions/1960957/twitter-api-logout
 * http://groups.google.com/group/twitter4j/browse_thread/thread/5957722d596e269c/c2956d43a46b31b5?lnk=gst&q=stateless
 */
public class TwitterPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private static final String TWITTER_VAR = "$twitter";
	private static final String OAUTH_REQUEST_TOKEN = "$twUsrReqTok";
	private static final String OAUTH_VERIFIER = "$twUsrVerifier";
	private static final String TWITTER_STREAM = "$twStream";
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TWITTER = JWebSocketServerConstants.NS_BASE + ".plugins.twitter";
	// the global Twitter object is used for general, non user specific information
	private Twitter mTwitter = null;
	private final static int MAX_STREAM_KEYWORDS_PER_CONNECTION = 5;
	private final static int MAX_STREAM_KEYWORDS_TOTAL = 50;
	private static int mStatsMaxConnectors = 0;
	private static int mStatsMaxKeywords = 0;
	private TwitterStream mTwitterStream = null;
	// connections registered to the twitter stream, 
	// list contains the keywords per connection
	private Map<WebSocketConnector, Set<String>> mConnectors = new FastMap<WebSocketConnector, Set<String>>();
	// for every keyword the connectors are counted,
	// if connectors are 0 the keyword gets deleted
	private Map<String, Integer> mKeywords = new FastMap<String, Integer>();
	private TokenServer mServer = null;
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	// the twitter facttory can be shared to generate multiple twitter object, one per user for streams and tweeting
	private static TwitterFactory mTwitterFactory;

	/**
	 *
	 * @param aConfiguration
	 */
	public TwitterPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Twitter plug-in...");
		}

		// specify default name space for admin plugin
		this.setNamespace(NS_TWITTER);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for Twitter plug-in, some features may not be available.");
			} else {
				mBeanFactory = getConfigBeanFactory();
				mSettings = (Settings) mBeanFactory.getBean("settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("Twitter plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating Twitter plug-in"));
		}

	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("tweet")) {
				tweet(aConnector, aToken);
			} else if (lType.equals("requestAccessToken")) {
				requestAccessToken(aConnector, aToken);
			} else if (lType.equals("login")) {
				login(aConnector, aToken);
			} else if (lType.equals("logout")) {
				logout(aConnector, aToken);
			} else if (lType.equals("getTimeline")) {
				getTimeline(aConnector, aToken);
			} else if (lType.equals("getStatistics")) {
				getStatistics(aConnector, aToken);
			} else if (lType.equals("getTrends")) {
				getTrends(aConnector, aToken);
			} else if (lType.equals("getPublicTimeline")) {
				getPublicTimeline(aConnector, aToken);
			} else if (lType.equals("query")) {
				query(aConnector, aToken);
			} else if (lType.equals("getUserData")) {
				getUserData(aConnector, aToken);
			} else if (lType.equals("setVerifier")) {
				setVerifier(aConnector, aToken);
			} else if (lType.equals("setStream")) {
				setStream(aConnector, aToken);
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		mRemoveConnector(aConnector);
		aConnector.removeVar(TWITTER_VAR);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		super.engineStopped(aEngine);

		// stop Twitter stream if used for this connection
		mStopStream();
	}

	private boolean mCheckAuth(Token aToken) {
		String lMsg;
		try {
			if (mTwitterFactory == null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Authenticating against Twitter...");
				}
				// The factory instance is re-useable and thread safe.
				/*
				ConfigurationBuilder lCB = new ConfigurationBuilder();
				lCB.setDebugEnabled(true);
				lCB.setOAuthConsumerKey(mSettings.getConsumerKey());
				lCB.setOAuthConsumerSecret(mSettings.getConsumerSecret());
				lCB.setOAuthAccessToken(mSettings.getAccessKey());
				lCB.setOAuthAccessTokenSecret(mSettings.getAccessSecret());
				mTwitterFactory = new TwitterFactory(lCB.build());
				*/
				mTwitterFactory = new TwitterFactory();
				mTwitter = mTwitterFactory.getInstance();
				// mTwitter.setOAuthConsumer(mSettings.getConsumerKey(), mSettings.getConsumerSecret());

				// AccessToken lAccessToken = new AccessToken(mSettings.getAccessKey(), mSettings.getAccessSecret());
				// mTwitter.setOAuthAccessToken(lAccessToken);
				lMsg = "Successfully authenticated against Twitter.";
			} else {
				lMsg = "Already authenticated against Twitter.";
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
			}
			return true;
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			aToken.setInteger("code", -1);
			aToken.setString("msg", lMsg);
			mLog.error(lMsg);
		}
		return false;
	}

	private void requestAccessToken(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		String lCallbackURL = aToken.getString("callbackURL");
		try {
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				// get a new Twitter object for the user
				TwitterFactory lTwitterFactory = new TwitterFactory();
				Twitter lTwitter = lTwitterFactory.getInstance();
				lTwitter.setOAuthConsumer(mSettings.getConsumerKey(), mSettings.getConsumerSecret());

				// pass callback URL to Twitter API if given
				RequestToken lReqToken =
						(lCallbackURL != null
						? lTwitter.getOAuthRequestToken(lCallbackURL)
						: lTwitter.getOAuthRequestToken());

				String lAuthenticationURL = lReqToken.getAuthenticationURL();
				String lAuthorizationURL = lReqToken.getAuthorizationURL();

				lResponse.setString("authenticationURL", lAuthenticationURL);
				lResponse.setString("authorizationURL", lAuthorizationURL);
				lMsg = "authenticationURL: " + lAuthenticationURL
						+ ", authorizationURL: " + lAuthorizationURL;
				lResponse.setString("msg", lMsg);
				if (mLog.isInfoEnabled()) {
					mLog.info(lMsg);
				}

				aConnector.setVar(OAUTH_REQUEST_TOKEN, lReqToken);
				aConnector.setVar(TWITTER_VAR, lTwitter);
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * is called by the jWebSocket OAuth confirmation window to pass the OAuth
	 * AccessToken Verifier from the Browser to the Server so that the server is
	 * able to run Twitter API commands w/o knowing the user's credentials.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void setVerifier(WebSocketConnector aConnector, Token aToken) {
		// TokenServer lServer = getServer();

		// instantiate response token
		String lVerifier = aToken.getString("verifier");
		aConnector.setString(OAUTH_VERIFIER, lVerifier);
	}

	private void login(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		try {
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				/*
				 * TwitterFactory lTwitterFactory = new TwitterFactory();
				 * Twitter lTwitter = lTwitterFactory.getInstance();
				 * lTwitter.setOAuthConsumer(mSettings.getConsumerKey(),
				 * mSettings.getConsumerSecret()); // pass callback URL to
				 * Twitter API RequestToken lReqToken =
				 * lTwitter.getOAuthRequestToken("http://localhost/demos/twitter/twauth.htm?isAuth=true");
				 *
				 * String lAuthenticationURL = lReqToken.getAuthenticationURL();
				 * String lAuthorizationURL = lReqToken.getAuthorizationURL();
				 *
				 * lResponse.setString("authenticationURL", lAuthenticationURL);
				 * lResponse.setString("authorizationURL", lAuthorizationURL);
				 * lMsg = "authenticationURL: " + lAuthenticationURL + ",
				 * authorizationURL: " + lAuthorizationURL;
				 * lResponse.setString("msg", lMsg); if (mLog.isInfoEnabled()) {
				 * mLog.info(lMsg); }
				 *
				 * // every connector maintains it's own twitter connection
				 * aConnector.setVar(TWITTER_VAR, lTwitter); // persist the
				 * request token, it's required // to get access token from
				 * verifier aConnector.setVar(OAUTH_REQUEST_TOKEN, lReqToken);
				 */
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void logout(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		try {
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				Twitter lTwitter = (Twitter) aConnector.getVar(TWITTER_VAR);
				if (lTwitter != null) {
					lTwitter.shutdown();
					lResponse.setString("msg", "Twitter instance has been shut down.");
				} else {
					lResponse.setString("msg", "Twitter instance down (not up before).");
				}
				aConnector.removeVar(TWITTER_VAR);
				aConnector.removeVar(OAUTH_REQUEST_TOKEN);
				aConnector.removeVar(OAUTH_VERIFIER);
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * Gets the Twitter timeline for a given user. If no user is given the user
	 * registered for the app is used as default.
	 */
	private void getTimeline(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		String lUsername = aToken.getString("username");

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Receiving timeline for user '"
						+ (lUsername != null ? lUsername : "[not given]")
						+ "'...");
			}
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				List<Status> lStatuses;
				// getting timelines is public so we can use the mTwitter object here
				if (lUsername != null && lUsername.length() > 0) {
					lStatuses = mTwitter.getUserTimeline(lUsername);
				} else {
					lStatuses = mTwitter.getUserTimeline();
				}
				// return the list of messages as an array of strings...
				List<String> lMessages = new FastList<String>();
				for (Status lStatus : lStatuses) {
					lMessages.add(lStatus.getUser().getName() + ": " + lStatus.getText());
					/*
					 * // If each status is supposed to be sent separately...
					 * Token lItem = TokenFactory.createToken(NS_TWITTER,
					 * BaseToken.TT_EVENT); lItem.setString("username",
					 * lStatus.getUser().getName()); lItem.setString("message",
					 * lStatus.getText()); lServer.sendToken(aConnector, lItem);
					 */
				}
				lResponse.setList("messages", lMessages);
				if (mLog.isInfoEnabled()) {
					mLog.info("Twitter timeline for user '"
							+ (lUsername != null ? lUsername : "[not given]")
							+ "' successfully received");
				}
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getUserData(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lUsername = aToken.getString("username");
		Integer lUserId = aToken.getInteger("userid");
		try {
			User lUser;
			// if user id is given use this to get user data
			if (lUserId != null && lUserId != 0) {
				lUser = mTwitter.showUser(lUserId);
				// if user name is given use this to get user data
			} else if (lUsername != null && lUsername.length() > 0) {
				lUser = mTwitter.showUser(lUsername);
				// otherwise return user data of provider (ourselves)
			} else {
				lUser = mTwitter.verifyCredentials();
			}
			if (lUser != null) {
				lResponse.setString("screenname", lUser.getScreenName());
				lResponse.setLong("id", lUser.getId());
				lResponse.setString("description", lUser.getDescription());
				lResponse.setString("location", lUser.getLocation());
				lResponse.setString("lang", lUser.getLang());
				lResponse.setString("name", lUser.getName());
			} else {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "Neither UserId nor Username passed.");
			}
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * posts a Twitter message on behalf of a OAtuh authenticated user by using
	 * the retrieved AccessToken and its verifier.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void tweet(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = aToken.getString("message");
		try {
			// to send tweet we need an authenticated user
			Twitter lTwitter = (Twitter) aConnector.getVar(TWITTER_VAR);
			RequestToken lReqToken = (RequestToken) aConnector.getVar(OAUTH_REQUEST_TOKEN);
			String lVerifier = aConnector.getString(OAUTH_VERIFIER);

			if (lTwitter == null) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "Not yet authenticated against Twitter!");
			} else if (lReqToken == null) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No Access Token available!");
			} else if (lVerifier == null) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No Access Verifier available!");
			} else if (lMsg == null || lMsg.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No message passed for tweet.");
			} else {
				AccessToken lAccessToken = lTwitter.getOAuthAccessToken(lReqToken, lVerifier);
				lTwitter.setOAuthAccessToken(lAccessToken);

				lTwitter.updateStatus(lMsg);
				lMsg = "Twitter status successfully updated for user '" + lTwitter.getScreenName() + "'.";
				lResponse.setString("msg", lMsg);
				if (mLog.isInfoEnabled()) {
					mLog.info(lMsg);
				}
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void query(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		String lQuery = aToken.getString("query");

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Searching for '"
						+ (lQuery != null ? lQuery : "[not given]")
						+ "'...");
			}
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				// return the list of messages as an array of strings...
				List<String> lMessages = new FastList<String>();

				QueryResult lQueryRes;
				// getting timelines is public so we can use the mTwitter object here
				if (lQuery != null && lQuery.length() > 0) {
					Query lTwQuery = new Query(lQuery);
					lQueryRes = mTwitter.search(lTwQuery);
					List<Tweet> lTweets = lQueryRes.getTweets();
					for (Tweet lTweet : lTweets) {
						lMessages.add(lTweet.getText());
					}
					lResponse.setList("messages", lMessages);

					if (mLog.isInfoEnabled()) {
						mLog.info("Tweets for query '"
								+ (lQuery != null ? lQuery : "[not given]")
								+ "' successfully received");
					}
				} else {
					lMsg = "No query string given";
					mLog.error(lMsg);
					lResponse.setInteger("code", -1);
					lResponse.setString("msg", lMsg);
				}
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getTrends(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Retreiving trends...");
			}
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				// return the list of messages as an array of strings...
				Map<String, List<String>> lAsOf = new FastMap<String, List<String>>();
				List<String> lMessages;
				ResponseList lTrendList = mTwitter.getDailyTrends();
				for (int lIdx = 0; lIdx < lTrendList.size(); lIdx++) {
					lMessages = new FastList<String>();
					Trends lTrends = (Trends) lTrendList.get(lIdx);
					Trend[] lTrendArray = lTrends.getTrends();
					for (Trend lTrend : lTrendArray) {
						lMessages.add(lTrend.getName() + ": " + lTrend.getQuery() + ", URL: " + lTrend.getUrl());
					}
					lAsOf.put(Tools.DateToISO8601(lTrends.getTrendAt()), lMessages);
				}
				lResponse.setMap("messages", lAsOf);
				if (mLog.isInfoEnabled()) {
					mLog.info("Trends successfully received");
				}
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getStatistics(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Retreiving statistics...");
		}

		lResponse.setInteger("listenerCount", mConnectors.size());
		lResponse.setInteger("keywordCount", mKeywords.size());
		lResponse.setString("keywords", mKeywords.toString());
		lResponse.setInteger("listenerMax", mStatsMaxConnectors);
		lResponse.setInteger("keywordMax", mStatsMaxKeywords);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getPublicTimeline(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Retreiving public timeline...");
			}
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				// return the list of messages as an array of strings...
				List<String> lMessages = new FastList<String>();

				ResponseList lRespList = mTwitter.getPublicTimeline();
				for (int lIdx = 0; lIdx < lRespList.size(); lIdx++) {
					Status lStatus = (Status) lRespList.get(lIdx);
					lMessages.add(lStatus.getText());
				}
				lResponse.setList("messages", lMessages);
				if (mLog.isInfoEnabled()) {
					mLog.info("Public timeline successfully received");
				}
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
	StatusListener mTwitterStreamListener = new StatusListener() {

		@Override
		public void onStatus(Status aStatus) {
			Token lToken = TokenFactory.createToken(NS_TWITTER, "event");
			lToken.setString("name", "status");
			String lStatus = aStatus.getText();
			lToken.setString("status", lStatus);
			User lUser = aStatus.getUser();
			if (lUser != null) {
				lToken.setString("userName", lUser.getName());
				lToken.setLong("userId", lUser.getId());
				URL lURL = lUser.getURL();
				if (lURL != null) {
					lToken.setString("userURL", lURL.toString());
				} else {
					lToken.setString("userURL", "");
				}
				lURL = lUser.getProfileImageURL();
				if (lURL != null) {
					lToken.setString("userImgURL", lURL.toString());
				} else {
					lToken.setString("userImgURL", "");
				}
				lToken.setString("userBckImgURL", lUser.getProfileBackgroundImageUrl());
				lToken.setString("userBckCol", lUser.getProfileBackgroundColor());
			} else {
				lToken.setString("userName", "");
				lToken.setInteger("userId", -1);
				lToken.setString("userURL", "");
				lToken.setString("userImgURL", "");
				lToken.setString("userBckImgURL", "");
				lToken.setString("userBckCol", "");
			}

			if (null != lStatus) {
				lStatus = lStatus.toLowerCase();
				for (WebSocketConnector lConnector : mConnectors.keySet()) {
					Set<String> lKeywords = mConnectors.get(lConnector);
					boolean lMatch = false;
					for (String lKeyword : lKeywords) {
						lMatch = lStatus.indexOf(lKeyword) >= 0;
						if (lMatch) {
							break;
						}
					}
					if (lMatch) {
						mServer.sendToken(lConnector, lToken);
					}
				}
			}

		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice aStatusDeletionNotice) {
			Token lToken = TokenFactory.createToken(NS_TWITTER, "event");
			lToken.setString("name", "deletion");
			lToken.setLong("userId", aStatusDeletionNotice.getUserId());
			lToken.setInteger("statusId", (int) aStatusDeletionNotice.getStatusId());
			for (WebSocketConnector lConnector : mConnectors.keySet()) {
				mServer.sendToken(lConnector, lToken);
			}
		}

		@Override
		public void onTrackLimitationNotice(int aNumberOfLimitedStatuses) {
			Token lToken = TokenFactory.createToken(NS_TWITTER, "event");
			lToken.setString("name", "trackLimit");
			lToken.setInteger("limit", aNumberOfLimitedStatuses);
			for (WebSocketConnector lConnector : mConnectors.keySet()) {
				mServer.sendToken(lConnector, lToken);
			}
		}

		@Override
		public void onException(Exception aEx) {
			Token lToken = TokenFactory.createToken(NS_TWITTER, "event");
			lToken.setString("name", "exception");
			lToken.setString("message", aEx.getMessage());
			lToken.setString("exception", aEx.getClass().getSimpleName());
			for (WebSocketConnector lConnector : mConnectors.keySet()) {
				mServer.sendToken(lConnector, lToken);
			}
		}

		@Override
		public void onScrubGeo(long aArg0, long aArg1) {
			// not required for now
		}
	};

	private void mAddConnector(WebSocketConnector aConnector, Set<String> aKeywords) {
		if (null == aConnector || null == aKeywords) {
			return;
		}
		// put the connector as a listener to lthe list
		mConnectors.put(aConnector, aKeywords);
		// increment counts for the keyowrds list
		for (String lKeyword : aKeywords) {
			Integer lCount = mKeywords.get(lKeyword);
			if (null == lCount) {
				mKeywords.put(lKeyword, 1);
			} else {
				mKeywords.put(lKeyword, lCount + 1);
			}
		}
		if (mConnectors.size() > mStatsMaxConnectors) {
			mStatsMaxConnectors = mConnectors.size();
		}
	}

	private void mRemoveConnector(WebSocketConnector aConnector) {
		if (null == aConnector) {
			return;
		}
		Set<String> lKeywords = mConnectors.get(aConnector);
		if (null == lKeywords) {
			return;
		}
		for (String lKeyword : lKeywords) {
			Integer lCount = mKeywords.get(lKeyword);
			if (null == lCount || 1 == lCount) {
				mKeywords.remove(lKeyword);
			} else {
				mKeywords.put(lKeyword, lCount - 1);
			}
		}
		mConnectors.remove(aConnector);
	}

	private void mUpdateStream(Token aToken) {
		try {
			String[] lKeywordArray = new String[mKeywords.size()];
			int lIdx = 0;
			for (String lKeyword : mKeywords.keySet()) {
				lKeywordArray[lIdx] = lKeyword;
				lIdx++;
				if (lIdx >= MAX_STREAM_KEYWORDS_TOTAL) {
					break;
				}
			}
			if (lIdx > mStatsMaxKeywords) {
				mStatsMaxKeywords = lIdx;
			}

			FilterQuery lFilter = new FilterQuery(
					0,
					new long[]{},
					lKeywordArray);
			// if no TwitterStream object created up to now, create one...
			if (mTwitterStream == null) {
				mTwitterStream = new TwitterStreamFactory().getInstance();
				mTwitterStream.addListener(mTwitterStreamListener);
				mTwitterStream.setOAuthConsumer(mSettings.getConsumerKey(), mSettings.getConsumerSecret());
				AccessToken lAccessToken = new AccessToken(mSettings.getAccessKey(), mSettings.getAccessSecret());
				mTwitterStream.setOAuthAccessToken(lAccessToken);
			}
			// apply the filter to the stream object
			mTwitterStream.filter(lFilter);
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			aToken.setInteger("code", -1);
			aToken.setString("msg", lMsg);
		}
	}

	private void mStopStream() {
		// if (still) some stream is allocated shut it down and release it!
		if (mTwitterStream != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Cleaning up Twitter stream...");
			}
			mTwitterStream.cleanUp();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Shutting down Twitter stream...");
			}
			mTwitterStream.shutdown();
		}
	}

	private void setStream(final WebSocketConnector aConnector, Token aToken) {

		if (null == mServer) {
			mServer = getServer();
		}

		// instantiate response token
		Token lResponse = mServer.createResponse(aToken);
		String lMsg;

		String lKeyWordString = aToken.getString("keywords");
		String[] lKeyWordArray;
		Set<String> lKeywordSet = new FastSet<String>();
		if (lKeyWordString != null) {
			lKeyWordArray = lKeyWordString.split(" ");
			int lAccepted = 0;
			for (int lIdx = 0, lCnt = lKeyWordArray.length;
					lIdx < lCnt && lAccepted < MAX_STREAM_KEYWORDS_PER_CONNECTION;
					lIdx++) {
				String lKeyword = lKeyWordArray[lIdx];
				// validate keywords
				if (lKeyword != null && lKeyword.length() >= 4) {
					lKeywordSet.add(lKeyword.toLowerCase());
					lAccepted++;
				}
			}
			mRemoveConnector(aConnector);
			if (lKeywordSet.size() > 0) {
				// remove existing connector from listener list
				// add connector with new keywords to listener list
				mAddConnector(aConnector, lKeywordSet);
				// and update the stream using ALL keywords from all clients
				mUpdateStream(aToken);
			} else {
				lMsg = "Twitter stream stopped.";
				lResponse.setString("msg", lMsg);
			}
		} else {
			lMsg = "No keywords argument passed, remaining in current state.";
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		mServer.sendToken(aConnector, lResponse);
	}
}
