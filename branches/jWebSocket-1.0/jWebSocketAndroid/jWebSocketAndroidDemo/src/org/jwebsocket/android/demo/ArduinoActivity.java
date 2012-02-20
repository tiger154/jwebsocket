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
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author aschulze
 */
public class ArduinoActivity extends Activity implements WebSocketClientTokenListener {

	private ImageView lBlue = null;
	private ImageView lRed = null;
	private ImageView lGreen = null;
	private ImageView lYellow = null;
	private ImageView lBlueOff = null;
	private ImageView lRedOff = null;
	private ImageView lGreenOff = null;
	private ImageView lYellowOff = null;
	private boolean lBlueOn = true;
	private boolean lRedOn = true;
	private boolean lGreenOn = true;
	private boolean lYellowOn = true;

	private void updateLEDs(int aCmd) {

		Token lToken = TokenFactory.createToken("rc", "rc.command");
		lToken.setInteger("cmd", aCmd);

		/*
		Token lToken = TokenFactory.createToken("rc","s2c.en");
		
		// pass namespace and type
		// for client's canvas "command"
		lToken.setBoolean("blue", lBlueOn);
		lToken.setBoolean("red", lRedOn);
		lToken.setBoolean("green", lGreenOn);
		lToken.setBoolean("yellow", lYellowOn);
		
		lToken.setString("_e", "ledState");
		lToken.setString("_p", "rc");
		lToken.setString("_rt", "void");
		lToken.setBoolean("hc", false);
		 */
		try {
			JWC.sendToken(lToken);
		} catch (WebSocketException ex) {
			// handle exception
		}
	}
	// {ns=rc, type=s2c.en, blue=false, red=false, green=false, yellow=false, _e=ledState, _p=rc, _rt=void, uid=72, hc=false}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.arduino_hvga_p);

		lBlue = (ImageView) findViewById(R.id.imgBlue);
		lBlueOff = (ImageView) findViewById(R.id.imgBlue_off);
		lRed = (ImageView) findViewById(R.id.imgRed);
		lRedOff = (ImageView) findViewById(R.id.imgRed_off);
		lGreen = (ImageView) findViewById(R.id.imgGreen);
		lGreenOff = (ImageView) findViewById(R.id.imgGreen_off);
		lYellow = (ImageView) findViewById(R.id.imgYellow);
		lYellowOff = (ImageView) findViewById(R.id.imgYellow_off);

		OnClickListener lBlueListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				lBlueOn = !lBlueOn;
				updateLEDs(49);
			}
		};

		lBlue.setOnClickListener(lBlueListener);
		lBlueOff.setOnClickListener(lBlueListener);

		OnClickListener lRedListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				lRedOn = !lRedOn;
				updateLEDs(50);
			}
		};

		lRed.setOnClickListener(lRedListener);
		lRedOff.setOnClickListener(lRedListener);

		OnClickListener lGreenListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				lGreenOn = !lGreenOn;
				updateLEDs(51);
			}
		};

		lGreen.setOnClickListener(lGreenListener);
		lGreenOff.setOnClickListener(lGreenListener);

		OnClickListener lYellowListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				lYellowOn = !lYellowOn;
				updateLEDs(52);
			}
		};

		lYellow.setOnClickListener(lYellowListener);
		lYellowOff.setOnClickListener(lYellowListener);
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
		if ("rc".equals(aToken.getNS())
				&& "s2c.en".equals(aToken.getType())) {
			if (aToken.getBoolean("blue", false)) {
				lBlue.setVisibility(View.VISIBLE);
				lBlueOff.setVisibility(View.INVISIBLE);
			} else {
				lBlue.setVisibility(View.INVISIBLE);
				lBlueOff.setVisibility(View.VISIBLE);
			}
			if (aToken.getBoolean("red", false)) {
				lRed.setVisibility(View.VISIBLE);
				lRedOff.setVisibility(View.INVISIBLE);
			} else {
				lRed.setVisibility(View.INVISIBLE);
				lRedOff.setVisibility(View.VISIBLE);
			}
			if (aToken.getBoolean("green", false)) {
				lGreen.setVisibility(View.VISIBLE);
				lGreenOff.setVisibility(View.INVISIBLE);
			} else {
				lGreen.setVisibility(View.INVISIBLE);
				lGreenOff.setVisibility(View.VISIBLE);
			}
			if (aToken.getBoolean("yellow", false)) {
				lYellow.setVisibility(View.VISIBLE);
				lYellowOff.setVisibility(View.INVISIBLE);
			} else {
				lYellow.setVisibility(View.INVISIBLE);
				lYellowOff.setVisibility(View.VISIBLE);
			}
		}
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
