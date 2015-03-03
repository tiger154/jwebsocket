//	---------------------------------------------------------------------------
//	jWebSocket - TwitterStreamSettingsActivity (Community Edition, CE)
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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 * @author Prashant
 */
public class TwitterStreamSettingsActivity extends Dialog {

	private TwitterSettingsListener mListener;
	private EditText mKeywordsText;
	private Button mSetBtn;
	private String defaultKeywords;

	/**
	 *
	 * @param context
	 * @param listener
	 * @param defaultKeywords
	 */
	public TwitterStreamSettingsActivity(Context context,
			TwitterSettingsListener listener, String defaultKeywords) {
		super(context);
		this.mListener = listener;
		this.defaultKeywords = defaultKeywords;
	}

	/**
	 *
	 * @param icicle
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.twitter_stream_settings);
		mKeywordsText = (EditText) findViewById(R.id.keywordsTxt);
		mKeywordsText.setText(defaultKeywords);
		mSetBtn = (Button) findViewById(R.id.setButton);
		mSetBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.setSettings(mKeywordsText.getText().toString());
				TwitterStreamSettingsActivity.this.dismiss();
			}
		});

	}

	/**
	 *
	 */
	public interface TwitterSettingsListener {

		/**
		 *
		 * @param keywords
		 */
		public void setSettings(String keywords);
	}
}
