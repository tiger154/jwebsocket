// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2012 Innotrade GmbH
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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class ArduinoActivity extends Activity implements WebSocketClientTokenListener {

	private ImageView lBlue = null;
	private ImageView lRed = null;
	private ImageView lGreen = null;
	private ImageView lYellow = null;
	
	private boolean lBlueOn = true;
	private boolean lRedOn = true;
	private boolean lGreenOn = true;
	private boolean lYellowOn = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.arduino_hvga_p);

		lBlue = (ImageView) findViewById(R.id.imgBlue);
		lRed = (ImageView) findViewById(R.id.imgRed);
		lGreen = (ImageView) findViewById(R.id.imgGreen);
		lYellow = (ImageView) findViewById(R.id.imgYellow);

		lBlue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Blue!",
						Toast.LENGTH_SHORT).show();
			}
		});
		lRed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Red!",
						Toast.LENGTH_SHORT).show();
			}
		});
		lGreen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Green!",
						Toast.LENGTH_SHORT).show();
			}
		});
		lYellow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Yellow!",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
		}
	}

	@Override
	protected void onPause() {
		try {
			JWC.close();
			JWC.removeListener(this);
		} catch (WebSocketException ex) {
		}
		super.onPause();
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		ImageView lImgStatus = (ImageView) findViewById(R.id.arduinoImgStatus);
		if (lImgStatus != null) {
			// TODO: in fact it is only connected, not yet authenticated!
			lImgStatus.setImageResource(R.drawable.authenticated);
		}
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		ImageView lImgStatus = (ImageView) findViewById(R.id.arduinoImgStatus);
		if (lImgStatus != null) {
			lImgStatus.setImageResource(R.drawable.disconnected);
		}
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
	}
}
