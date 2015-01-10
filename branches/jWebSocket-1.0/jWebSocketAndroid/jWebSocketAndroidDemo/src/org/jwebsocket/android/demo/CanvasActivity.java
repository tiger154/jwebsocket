//	---------------------------------------------------------------------------
//	jWebSocket - CanvasActivity (Community Edition, CE)
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

import java.util.Timer;
import java.util.TimerTask;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 *
 * @author Alexander Schulze
 */
public class CanvasActivity extends Activity implements
		WebSocketClientTokenListener {

	private RelativeLayout mLayout = null;
	private Canvas lCanvas = null;
	private Paint lPaint = null;
	private String CANVAS_ID = "p_canvas";
	private String NS_CANVAS = "jws.canvas";
	private String mClientId = "";
	private float lSX = 0, lSY = 0;
	private ImageView lImgView = null;
	private ImageView lImgStatus = null;
	private int lWidth;
	private int lHeight;
	private Timer mTimer;
	private volatile boolean mIsDirty = false;
	private int lTitleBarHeight = 0;

	/**
	 * Called when the activity is first created.
	 *
	 * @param icicle
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Create a LinearLayout in which to add the ImageView
		mLayout = new RelativeLayout(this);

		// get the display metric (width and height)
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		lWidth = metrics.widthPixels;
		lHeight = metrics.heightPixels;

		final Bitmap lBmp = Bitmap.createBitmap(lWidth, lHeight,
				Bitmap.Config.ARGB_8888);
		lCanvas = new Canvas(lBmp);
		// final ImageView lImgView = new ImageView(this);
		lImgView = new ImageView(this);

		lImgView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		lImgView.setImageBitmap(lBmp);
		lImgView.setScaleType(ImageView.ScaleType.CENTER);
		lImgView.setPadding(0, 0, 0, 0);

		lImgStatus = new ImageView(this);
		lImgStatus.setAdjustViewBounds(true); // set the ImageView bounds to
		// match the Drawable's
		// dimensions
		if (JWC.isConnected()) {
			lImgStatus.setImageResource(R.drawable.authenticated);
		} else {
			lImgStatus.setImageResource(R.drawable.disconnected);
		}
		// lImgStatus.setAlpha(128);

		final Paint lBck = new Paint();
		lBck.setARGB(0xff, 0xff, 0xff, 0xff);
		lCanvas.drawRect(0, 0, lWidth, lHeight, lBck);

		lPaint = new Paint();
		lPaint.setARGB(0xff, 0x00, 0x00, 0x00);

		mLayout.addView(lImgView);
		mLayout.addView(lImgStatus);
		setContentView(mLayout);

		lImgView.setOnTouchListener(new OnTouchListener() {
			// start and end coordinates for a single line
			float lSX, lSY, lEX, lEY;

			@Override
			public boolean onTouch(View aView, MotionEvent aME) {

				Rect lRect = new Rect();
				Window lWindow = getWindow();
				lWindow.getDecorView().getWindowVisibleDisplayFrame(lRect);
				int lStatusBarHeight = lRect.top;
				int lContentViewTop = lWindow.findViewById(
						Window.ID_ANDROID_CONTENT).getTop();
				lTitleBarHeight = lContentViewTop - lStatusBarHeight;

				float lX = aME.getX();
				float lY = aME.getY();

				switch (aME.getAction()) {
					case MotionEvent.ACTION_DOWN:
						lEX = lX;
						lEY = lY + lTitleBarHeight;
						// Not needed anymore since we draw based in plots
						sendBeginPath(lEX, lEY);
						break;
					case MotionEvent.ACTION_MOVE:
						lSX = lEX;
						lSY = lEY;
						lEX = lX;
						lEY = lY + lTitleBarHeight;
						lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
						// updated by Alex 2010-08-10
						sendLine(lSX, lSY, lEX, lEY);
						lSX = lEX;
						lSY = lEY;
						break;
					case MotionEvent.ACTION_UP:
						sendClosePath();
						break;
					case MotionEvent.ACTION_CANCEL:
						break;
				}
				lImgView.invalidate();
				return true;
			}
		});

		mTimer = new Timer();
		mTimer.schedule(new TimerTask() { // AtFixedRate
			@Override
			public void run() {
				if (mIsDirty) {
					mIsDirty = false;
					// TODO: check why this doesn't work
					// lImgView.postInvalidate();
					try {
						Thread.sleep(50);
					} catch (InterruptedException ex) {
					}
				}
			}
		}, 0, 200);
	}

	// added by Alex: 2010-08-20
	/**
	 *
	 * @param ax
	 * @param ay
	 */
	public void sendBeginPath(float ax, float ay) {
		Token lCanvasToken = TokenFactory.createToken();
		lCanvasToken.setString("x", String.valueOf(ax));
		lCanvasToken.setString("y", String.valueOf(ay));
		lCanvasToken.setString("id", CANVAS_ID);

		publish("beginPath", lCanvasToken);
	}

	/**
	 *
	 * @param aX
	 * @param aY
	 */
	public void sendLine(float aX1, float aY1, float aX2, float aY2) {
		// added by Alex: 2010-08-20
		Token lCanvasToken = TokenFactory.createToken();
		lCanvasToken.setString("x1", String.valueOf(aX1));
		lCanvasToken.setString("y1", String.valueOf(aY1));
		lCanvasToken.setString("x2", String.valueOf(aX2));
		lCanvasToken.setString("y2", String.valueOf(aY2));
		lCanvasToken.setString("color", "#000000");
		lCanvasToken.setString("id", CANVAS_ID);

		publish("line", lCanvasToken);
	}

	// added by Alex: 2010-08-20
	public void sendClosePath() {
		Token lCanvasToken = TokenFactory.createToken();
		lCanvasToken.setString("id", CANVAS_ID);

		publish("closePath", lCanvasToken);
	}

	/**
	 *
	 * @param aMenu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu aMenu) {
		MenuInflater lMenInfl = getMenuInflater();
		lMenInfl.inflate(R.menu.canvas_menu, aMenu);
		return true;
	}

	/**
	 *
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.mniCanvasClear:
				clearCanvas();
				sendClear();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void clearCanvas() {
		final Paint lBck = new Paint();
		lBck.setARGB(0xff, 0xff, 0xff, 0xff);
		lCanvas.drawRect(0, 0, lWidth, lHeight, lBck);
		lImgView.invalidate();
	}

	private void sendClear() {
		Token lCanvasToken = TokenFactory.createToken();
		lCanvasToken.setString("id", CANVAS_ID);

		// Passing the action clear and the canvas id for other clients
		publish("clear", lCanvasToken);
	}

	/**
	 * This method will be used to send any type of data to the server Once the
	 * server receives it, will broadcast it to a certain list of users
	 * previously registered in the server side
	 */
	protected void publish(String aAction, Token aToken) {
		if (null == aToken) {
			aToken = TokenFactory.createToken();
		}
		// Type of publication, when the user receive this message he will know
		// what to do by reading this variable in "aToken.data"
		aToken.setNS(NS_CANVAS);
		aToken.setType("data");
		aToken.setString("data", aAction);
		try {
			JWC.sendToken(aToken);
		} catch (WebSocketException lEx) {
			Toast.makeText(getApplicationContext(),
					"Failed to send a " + aAction + " event.",
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void setAuthenticated(WebSocketClientEvent aEvent) {
		// Once the connection opens we need to send a register token
		Token lRegisterToken = TokenFactory.createToken(NS_CANVAS, "register");
		try {
			JWC.sendToken(lRegisterToken);
		} catch (WebSocketException lEx) {
			Toast.makeText(
					getApplicationContext(),
					"Failed to register to the demo, you may not be able to see incoming drawings from other users.",
					Toast.LENGTH_LONG).show();
		}
		if (lImgStatus != null) {
			// TODO: in fact it is only connected, not yet authenticated!
			lImgStatus.setImageResource(R.drawable.authenticated);
		}
	}

	/**
	 *
	 */
	@Override
	protected void onResume() {
		super.onResume();
		try {
			// get the display metric (width and height)
			DisplayMetrics lMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(lMetrics);
			lWidth = lMetrics.widthPixels;
			lHeight = lMetrics.heightPixels;

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(lWidth - 18, 2, 0, 0);
			lImgStatus.setLayoutParams(lp);

			JWC.addListener(this);
			if (!JWC.isConnected()) {
				JWC.open();
			} else {
				setAuthenticated(null);
			}
		} catch (WebSocketException ex) {
			// TODO: log exception
		}
	}

	/**
	 *
	 */
	@Override
	protected void onPause() {
		super.onPause();
		try {
			Token lUnregisterToken = TokenFactory.createToken(NS_CANVAS,
					"unregister");
			try {
				JWC.sendToken(lUnregisterToken);
			} catch (WebSocketException lEx) {
				// Not needed since in case of an exception the
				// client is automatically unregistered when the connector is
				// stopped
			}
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
		// check if incoming token is targetted to canvas (by name space)
		if (NS_CANVAS.equals(aToken.getNS())) {
			if ("data".equals(aToken.getType())
					&& !aToken.getString("publisher", "").equals(mClientId)) {
				if ("line".equals(aToken.getString("data"))) {
					// TODO: implement multiple canvas, this is what is used
					// for
					// int id = aToken.getInteger("identifier");
					// if (id != getTaskId()) {
					lSX = aToken.getDouble("x1", 0.0).floatValue();
					lSY = aToken.getDouble("y1", 0.0).floatValue();
					float lEX = aToken.getDouble("x2", 0.0).floatValue();
					float lEY = aToken.getDouble("y2", 0.0).floatValue();
					lSY += lTitleBarHeight;
					lEY += lTitleBarHeight;
					// draw the line
					String lColStr = aToken.getString("color");
					if (lColStr != null && lColStr.length() == 7
							&& lColStr.startsWith("#")) {
						int lColVal = Color.BLACK;
						try {
							lColVal = Integer.valueOf(lColStr.substring(1),
									16);
						} catch (Exception lEx) {
							lColVal = Color.BLACK;
						}
						lPaint.setColor(lColVal);
					}
					lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
					// invalidate image view to re-draw the canvas
					mIsDirty = true;
				} else if ("moveTo".equals(aToken.getString("data"))) {
					// keep start position "in mind"
					lSX = aToken.getDouble("x", 0.0).floatValue();
					lSY = aToken.getDouble("y", 0.0).floatValue();
					// check "lineTo" request
				} else if ("lineTo".equals(aToken.getString("data"))) {
					// TODO: implement multiple canvas, this is what is used
					// for
					// int id = aToken.getInteger("identifier");
					// if (id != getTaskId()) {
					float lEX = aToken.getDouble("x", 0.0).floatValue();
					float lEY = aToken.getDouble("y", 0.0).floatValue();
					// draw the line
					lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
					// invalidate image view to re-draw the canvas

					mIsDirty = true;

					lSX = lEX;
					lSY = lEY;
				} else if ("clear".equals(aToken.getString("data"))) {
					clearCanvas();
				}
			}
		} else if ("welcome".equals(aToken.getType())) {
			mClientId = aToken.getString("sourceId", "");
		}
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		setAuthenticated(aEvent);
	}

	/**
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	@Override
	public void processPacket(WebSocketClientEvent aEvent,
			WebSocketPacket aPacket) {
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		// lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
		if (lImgStatus != null) {
			lImgStatus.setImageResource(R.drawable.disconnected);
		}
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
}
