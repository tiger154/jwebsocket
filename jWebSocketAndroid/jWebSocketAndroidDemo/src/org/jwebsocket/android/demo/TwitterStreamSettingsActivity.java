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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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

	public TwitterStreamSettingsActivity(Context context, TwitterSettingsListener listener, String defaultKeywords) {
		super(context);
		this.mListener = listener;
                this.defaultKeywords = defaultKeywords;
	}



	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
                this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.twitter_stream_settings);		
		mKeywordsText = (EditText) findViewById(R.id.keywordsTxt);
                mKeywordsText.setText(defaultKeywords);
		mSetBtn = (Button) findViewById(R.id.setButton);
		mSetBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				mListener.setSettings(mKeywordsText.getText().toString());
				TwitterStreamSettingsActivity.this.dismiss();
			}
		});
                
	}

	public interface TwitterSettingsListener {

		public void setSettings(String keywords);
	}
}
