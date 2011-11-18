// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
// for more details.
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.android.demo;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
	TwitterStreamSettingsActivity lSettingsDialog;
	private final int CAPACITY = 20;
	private SharedPreferences prefs;
	private String lKeywords;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.twitter_stream_activity);
		this.mTweets = new ArrayList<Tweet>();
		this.mTweetAdapter = new TweetAdapter(this, R.layout.tweet_row, mTweets);
		setListAdapter(mTweetAdapter);
		prefs = getPreferences(MODE_PRIVATE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu aMenu) {
		MenuInflater lMenInfl = getMenuInflater();
		lMenInfl.inflate(R.menu.twitter_stream_menu, aMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem aItem) {
		// Handle item selection
		switch (aItem.getItemId()) {
			case R.id.twitterStreamSettings:
				if (lSettingsDialog == null) {
					lSettingsDialog =
							new TwitterStreamSettingsActivity(this,
							new TwitterStreamSettingsActivity.TwitterSettingsListener() {

								public void setSettings(String keywords) {


									savePreference(keywords);
									setKeywords(keywords);

								}
							}, lKeywords);
				}
				lSettingsDialog.show();
				return true;
			default:
				return super.onOptionsItemSelected(aItem);
		}
	}

	private void setKeywords(String aKeywords) {
		Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.twitter", "setStream");
		lToken.setString("keywords", aKeywords);
		try {
			JWC.sendToken(lToken);
		} catch (WebSocketException ex) {
			Logger.getLogger(TwitterStreamActivity.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		connect();
		lKeywords = prefs.getString("keywords", "");
		if (!lKeywords.equals("")) {
			setKeywords(lKeywords);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		disConnect();

	}

	private void savePreference(String aKeyWords) {
		SharedPreferences.Editor lEditor = prefs.edit();
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

	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		if (aToken.getNS().equals("org.jwebsocket.plugins.twitter")
				&& aToken.getType().equals("event")
				&& aToken.getString("name").equals("status")) {
			mTweets.add(0, new Tweet(aToken.getString("status"),
					aToken.getString("userImgURL"), aToken.getString("userName")));
			if (mTweets.size() > CAPACITY) {
				mTweets.remove(CAPACITY);
			}

			mTweetAdapter.notifyDataSetChanged();
		}
		//fillDemoTweets();
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public void processOpened(WebSocketClientEvent aEvent) {
		//throw new UnsupportedOperationException("Not supported yet.");
		int test = 1;
	}

	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public void processClosed(WebSocketClientEvent aEvent) {
		//throw new UnsupportedOperationException("Not supported yet.");
		int test = 1;
	}

	public void processOpening(WebSocketClientEvent aEvent) {
	}

	public void processReconnecting(WebSocketClientEvent aEvent) {
	}

	public class Tweet {

		private String mTweet;
		private String mUserImgURL;
		private String mUserName;

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

	public class TweetAdapter extends ArrayAdapter<Tweet> {

		private ArrayList<Tweet> mTweets;
		private ImageThreadLoader mImageLoader = new ImageThreadLoader();

		public TweetAdapter(Context aContext, int aTextViewResourceId,
				ArrayList<Tweet> aTweets) {
			super(aContext, aTextViewResourceId, aTweets);
			this.mTweets = aTweets;
		}

		@Override
		public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
			View lView = aConvertView;
			if (lView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				lView = vi.inflate(R.layout.tweet_row, null);
			}
			Tweet lTweet = mTweets.get(aPosition);
			if (lTweet != null) {
				TextView tweetText = (TextView) lView.findViewById(R.id.tweetTxt);
				if (tweetText != null) {
					tweetText.setText(lTweet.getTweet());
				}
				final ImageView lUserImage = (ImageView) lView.findViewById(R.id.userImg);
				Bitmap lCachedImage = null;
				try {
					lCachedImage = mImageLoader.loadImage(lTweet.mUserImgURL, new ImageLoadedListener() {

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
