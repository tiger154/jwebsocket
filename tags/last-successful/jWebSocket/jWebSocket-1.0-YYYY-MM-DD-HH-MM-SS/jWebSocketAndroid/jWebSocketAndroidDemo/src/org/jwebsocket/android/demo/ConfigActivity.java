//	---------------------------------------------------------------------------
//	jWebSocket - ConfigActivity (Community Edition, CE)
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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *
 * @author Alexander Schulze
 */
public class ConfigActivity extends Activity {

	private Button lBtnCancel;
	private Button lBtnSave;
	private EditText lTxfURL;
	private Activity lInstance;

	/**
	 * Called when the activity is first created.
	 *
	 * @param icicle
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.config_hvga_p);

		lBtnCancel = (Button) findViewById(R.id.cfgBtnCancel);
		lBtnSave = (Button) findViewById(R.id.cfgBtnSave);
		lTxfURL = (EditText) findViewById(R.id.cfgTxfURL);
		lInstance = this;

		lBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Toast.makeText(getApplicationContext(), "DISCARDING...",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		});

		lBtnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "SAVING...",
						Toast.LENGTH_SHORT).show();

				JWC.setURL(lTxfURL.getText().toString());
				JWC.saveSettings(lInstance);
				finish();
			}
		});
		JWC.loadSettings(this);
		lTxfURL.setText(JWC.getURL());

	}

	/**
	 *
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 *
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
}
