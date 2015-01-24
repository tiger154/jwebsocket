//	---------------------------------------------------------------------------
//	jWebSocket - TwitterStreamActivity (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.android.demo;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.android.demo.ImageThreadLoader.ImageLoadedListener;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Prashant
 */
public class TwitterStreamActivity extends ListActivity implements
		WebSocketClientTokenListener {

	private ArrayList<Tweet> mTweets = null;
	private TweetAdapter mTweetAdapter;
	TwitterStreamSettingsActivity mSettingsDialog;
	private final int CAPACITY = 20;
	private SharedPreferences mPrefs;
	private String mKeywords;

	/**
	 *
	 * @param aIcicle
	 */
	@Override
	public void onCreate(Bundle aIcicle) {
		super.onCreate(aIcicle);
		setContentView(R.layout.twitter_stream_activity);
		this.mTweets = new ArrayList<Tweet>();
		this.mTweetAdapter = new TweetAdapter(this, R.layout.tweet_row, mTweets);
		setListAdapter(mTweetAdapter);
		mPrefs = getPreferences(MODE_PRIVATE);

	}

	/**
	 *
	 * @param aMenu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu aMenu) {
		MenuInflater lMenInfl = getMenuInflater();
		lMenInfl.inflate(R.menu.twitter_stream_menu, aMenu);
		return true;
	}

	/**
	 *
	 * @param aItem
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem aItem) {
		// Handle item selection
		switch (aItem.getItemId()) {
			case R.id.twitterStreamSettings:
				if (mSettingsDialog == null) {
					mSettingsDialog = new TwitterStreamSettingsActivity(
							this,
							new TwitterStreamSettingsActivity.TwitterSettingsListener() {
								@Override
								public void setSettings(String keywords) {
									savePreference(keywords);
									setKeywords(keywords);
								}
							}, mKeywords);
				}
				mSettingsDialog.show();
				return true;
			default:
				return super.onOptionsItemSelected(aItem);
		}
	}

	private void setKeywords(String aKeywords) {
		Token lToken = TokenFactory.createToken(
				"org.jwebsocket.plugins.twitter", "setStream");
		lToken.setString("keywords", aKeywords);
		try {
			JWC.sendToken(lToken);
		} catch (WebSocketException ex) {
			Logger.getLogger(TwitterStreamActivity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 */
	@Override
	protected void onResume() {
		super.onResume();
		connect();
		mKeywords = mPrefs.getString("keywords", "");
		if (!mKeywords.equals("")) {
			setKeywords(mKeywords);
		}
	}

	/**
	 *
	 */
	@Override
	protected void onPause() {
		super.onPause();
		disConnect();

	}

	private void savePreference(String aKeyWords) {
		SharedPreferences.Editor lEditor = mPrefs.edit();
		lEditor.putString("keywords", aKeyWords);
		lEditor.commit();
	}

	private void connect() {
		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
		}
	}

	private void disConnect() {
		try {
			JWC.removeListener(this);
			JWC.close();
		} catch (WebSocketException ex) {
			// TODO: log exception
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aToken
	 */
	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		if (aToken.getNS().equals("org.jwebsocket.plugins.twitter")
				&& aToken.getType().equals("event")
				&& aToken.getString("name").equals("status")) {
			mTweets.add(
					0,
					new Tweet(aToken.getString("status"), aToken
							.getString("userImgURL"), aToken
							.getString("userName")));
			if (mTweets.size() > CAPACITY) {
				mTweets.remove(CAPACITY);
			}

			mTweetAdapter.notifyDataSetChanged();
		}
		// fillDemoTweets();
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	@Override
	public void processPacket(WebSocketClientEvent aEvent,
			WebSocketPacket aPacket) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
	}

	/**
	 *
	 */
	public class Tweet {

		private String mTweet;
		private String mUserImgURL;
		private String mUserName;

		/**
		 *
		 * @param aTweet
		 * @param aUserImageUrl
		 * @param aUserName
		 */
		public Tweet(String aTweet, String aUserImageUrl, String aUserName) {
			this.mTweet = aTweet;
			this.mUserImgURL = aUserImageUrl;
			this.mUserName = aUserName;
		}

		/**
		 * @return the tweet
		 */
		public String getTweet() {
			return mTweet;
		}

		/**
		 * @param tweet
		 */
		public void setTweet(String tweet) {
			this.mTweet = tweet;
		}

		/**
		 * @return the userImgURL
		 */
		public String getUserImgURL() {
			return mUserImgURL;
		}

		/**
		 * @param aUserImgURL the userImgURL to set
		 */
		public void setUserImgURL(String aUserImgURL) {
			this.mUserImgURL = aUserImgURL;
		}

		/**
		 * @return the userName
		 */
		public String getUserName() {
			return mUserName;
		}

		/**
		 * @param aUserName the userName to set
		 */
		public void setUserName(String aUserName) {
			this.mUserName = aUserName;
		}
	}

	/**
	 *
	 */
	public class TweetAdapter extends ArrayAdapter<Tweet> {

		private ArrayList<Tweet> mTweets;
		private ImageThreadLoader mImageLoader = new ImageThreadLoader();

		/**
		 *
		 * @param aContext
		 * @param aTextViewResourceId
		 * @param aTweets
		 */
		public TweetAdapter(Context aContext, int aTextViewResourceId,
				ArrayList<Tweet> aTweets) {
			super(aContext, aTextViewResourceId, aTweets);
			this.mTweets = aTweets;
		}

		/**
		 *
		 * @param aPosition
		 * @param aConvertView
		 * @param aParent
		 * @return
		 */
		@Override
		public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
			View lView = aConvertView;
			if (lView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				lView = vi.inflate(R.layout.tweet_row, null);
			}
			Tweet lTweet = mTweets.get(aPosition);
			if (lTweet != null) {
				TextView tweetText = (TextView) lView
						.findViewById(R.id.tweetTxt);
				if (tweetText != null) {
					tweetText.setText(lTweet.getTweet());
				}
				final ImageView lUserImage = (ImageView) lView
						.findViewById(R.id.userImg);
				Bitmap lCachedImage = null;
				try {
					lCachedImage = mImageLoader.loadImage(lTweet.mUserImgURL,
							new ImageLoadedListener() {
								@Override
								public void imageLoaded(Bitmap aImageBitmap) {
									notifyDataSetChanged();
								}
							});
				} catch (MalformedURLException e) {
				}
				if (lCachedImage != null) {
					lUserImage.setImageBitmap(lCachedImage);
				} else {
					lUserImage.setImageBitmap(null);
				}

			}

			return lView;
		}
	}
}
