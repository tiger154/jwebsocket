//	---------------------------------------------------------------------------
//	jWebSocket - Fundamentals (Community Edition, CE)
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
import android.widget.*;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class Fundamentals extends Activity implements
		WebSocketClientTokenListener {

	private Button lBtnSend;
	private Button lBtnBroadcast;
	private Button lBtnClearLog;
	private EditText lMessage;
	private EditText lTarget;
	private TextView lLog;

	// private SamplePlugIn lSamplePlugIn = null;
	/**
	 * Called when the activity is first created.
	 *
	 * @param icicle
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.fundamentals_hvga_p);
		lBtnSend = (Button) findViewById(R.id.btnFundSend);
		lBtnBroadcast = (Button) findViewById(R.id.btnFundBroadcast);
		lBtnClearLog = (Button) findViewById(R.id.btnFundClearLog);
		lMessage = (EditText) findViewById(R.id.txfFundMessage);
		lTarget = (EditText) findViewById(R.id.txfFundTarget);
		lLog = (EditText) findViewById(R.id.lblFundLog);

		// lSamplePlugIn = new SamplePlugIn(JWC.getClient());
		lBtnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View aView) {
				try {
					// lSamplePlugIn.getRandom();
					JWC.sendText(lTarget.getText().toString(), lMessage
							.getText().toString());
				} catch (WebSocketException ex) {
					// TODO: handle exception
				}
			}
		});

		lBtnBroadcast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View aView) {
				try {
					JWC.broadcastText(lMessage.getText().toString());
				} catch (WebSocketException ex) {
					// TODO: handle exception
				}

			}
		});

		lBtnClearLog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View aView) {
				lLog.setText("");
			}
		});
	}

	/**
	 *
	 */
	@Override
	protected void onResume() {
		super.onResume();
		log("* opening... ");
		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
			log("* exception: " + ex.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	protected void onPause() {
		log("* closing... ");
		try {
			JWC.close();
			JWC.removeListener(this);
		} catch (WebSocketException ex) {
			log("* exception: " + ex.getMessage());
		}
		super.onPause();
	}

	private void log(CharSequence aString) {
		try {
			lLog.append(aString);
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(),
					ex.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		log("opened\n");
		ImageView lImgView = (ImageView) findViewById(R.id.fundImgStatus);
		if (lImgView != null) {
			// TODO: in fact it is only connected, not yet authenticated!
			lImgView.setImageResource(R.drawable.authenticated);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	@Override
	public void processPacket(WebSocketClientEvent aEvent,
			WebSocketPacket aPacket) {
		log("> " + aPacket.getUTF8() + "\n");
	}

	/**
	 *
	 * @param aEvent
	 * @param aToken
	 */
	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		// log("> " + aToken.toString() + "\n");
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		log("closed\n");
		ImageView lImgView = (ImageView) findViewById(R.id.fundImgStatus);
		if (lImgView != null) {
			lImgView.setImageResource(R.drawable.disconnected);
		}
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
		log("* opening... ");
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
		log("* reconnecting... ");
	}
}
