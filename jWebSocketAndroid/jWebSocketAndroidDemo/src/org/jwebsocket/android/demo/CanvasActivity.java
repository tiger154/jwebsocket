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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.Timer;
import java.util.TimerTask;
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
public class CanvasActivity extends Activity implements WebSocketClientTokenListener {

	private RelativeLayout mLayout = null;
	private Canvas lCanvas = null;
	private Paint lPaint = null;
	private String CANVAS_ID = "c1";
	private float lSX = 0, lSY = 0;
	private ImageView lImgView = null;
	private ImageView lImgStatus = null;
	private int lWidth;
	private int lHeight;
	private Timer mTimer;
	private volatile boolean mIsDirty = false;

	/** Called when the activity is first created. */
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

		final Bitmap lBmp = Bitmap.createBitmap(lWidth, lHeight, Bitmap.Config.ARGB_8888);
		lCanvas = new Canvas(lBmp);
		// final ImageView lImgView = new ImageView(this);
		lImgView = new ImageView(this);

		lImgView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		lImgView.setImageBitmap(lBmp);
		lImgView.setScaleType(ImageView.ScaleType.CENTER);
		lImgView.setPadding(0, 0, 0, 0);

		lImgStatus = new ImageView(this);
		lImgStatus.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
		lImgStatus.setImageResource(R.drawable.disconnected);
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

			public boolean onTouch(View aView, MotionEvent aME) {

				Rect lRect = new Rect();
				Window lWindow = getWindow();
				lWindow.getDecorView().getWindowVisibleDisplayFrame(lRect);
				int lStatusBarHeight = lRect.top;
				int lContentViewTop =
						lWindow.findViewById(Window.ID_ANDROID_CONTENT).getTop();
				final int lTitleBarHeight = lContentViewTop - lStatusBarHeight;

				int lAction = aME.getAction();

				float lX = aME.getX();
				float lY = aME.getY();

				switch (lAction) {
					case MotionEvent.ACTION_DOWN:
						lEX = lX;
						lEY = lY + lTitleBarHeight;
						sendBeginPath(lEX, lEY);
						break;
					case MotionEvent.ACTION_MOVE:
						lSX = lEX;
						lSY = lEY;
						lEX = lX;
						lEY = lY + lTitleBarHeight;
						lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
						// updated by Alex 2010-08-10
						sendLineTo(lEX, lEY);
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						sendClosePath();
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
					lImgView.postInvalidate();
					try {
						Thread.sleep(50);
					} catch (InterruptedException ex) {
					}
				}
			}
		}, 0, 200);


	}

	// added by Alex: 2010-08-20
	public void sendBeginPath(float ax, float ay) {
		// use broadcast of system plug-in
		// use namespace and type for server's broadcast "command"
		Token lCanvasToken = TokenFactory.createToken(
				"org.jwebsocket.plugins.system",
				"broadcast");

		// pass namespace and type
		// for client's canvas "command"
		lCanvasToken.setString("reqNS", "org.jwebsocket.plugins.canvas");
		lCanvasToken.setString("reqType", "beginPath");
		lCanvasToken.setDouble("x", ax);
		lCanvasToken.setDouble("y", ay);
		lCanvasToken.setString("id", CANVAS_ID);

		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException e) {
			//TODO: log exception
		}
	}

	public void sendLineTo(float aX, float aY) {
		// added by Alex: 2010-08-20
		// use broadcast of system plug-in
		Token lCanvasToken = TokenFactory.createToken(
				"org.jwebsocket.plugins.system",
				"broadcast");

		lCanvasToken.setString("reqNS", "org.jwebsocket.plugins.canvas");
		lCanvasToken.setString("reqType", "lineTo");
		lCanvasToken.setDouble("x", aX);
		lCanvasToken.setDouble("y", aY);
		lCanvasToken.setString("id", CANVAS_ID);
		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException ex) {
			//TODO: log exception
		}
	}

	// added by Alex: 2010-08-20
	public void sendClosePath() {
		// use broadcast of system plug-in
		// use namespace and type for server's broadcast "command"
		Token lCanvasToken = TokenFactory.createToken(
				"org.jwebsocket.plugins.system",
				"broadcast");
		lCanvasToken.setString("id", CANVAS_ID);

		// pass namespace and type
		// for client's canvas "command"
		lCanvasToken.setString("reqNS", "org.jwebsocket.plugins.canvas");
		lCanvasToken.setString("reqType", "closePath");

		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException e) {
			//TODO: log exception
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu aMenu) {
		MenuInflater lMenInfl = getMenuInflater();
		lMenInfl.inflate(R.menu.canvas_menu, aMenu);
		return true;
	}

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
		Token lCanvasToken = TokenFactory.createToken(
				"org.jwebsocket.plugins.system",
				"broadcast");
		lCanvasToken.setString("id", CANVAS_ID);

		// pass namespace and type
		// for client's canvas "command"
		lCanvasToken.setString("reqNS", "org.jwebsocket.plugins.canvas");
		lCanvasToken.setString("reqType", "clear");

		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException e) {
			//TODO: log exception
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			// get the display metric (width and height)
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			lWidth = metrics.widthPixels;
			lHeight = metrics.heightPixels;

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(lWidth - 18, 2, 0, 0);
			lImgStatus.setLayoutParams(lp);

			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
			//TODO: log exception
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			JWC.removeListener(this);
			JWC.close();
		} catch (WebSocketException ex) {
			//TODO: log exception
		}

	}

	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		// check if incoming token is targetted to canvas (by name space)
		if ("org.jwebsocket.plugins.canvas".equals(aToken.getString("reqNS"))) {
			// check "beginPath" request
			if ("beginPath".equals(aToken.getString("reqType"))) {
				// nothing to do here
			} else if ("moveTo".equals(aToken.getString("reqType"))) {
				// keep start position "in mind"
				lSX = new Float(aToken.getDouble("x", 0.0));
				lSY = new Float(aToken.getDouble("y", 0.0));
				// check "lineTo" request
			} else if ("lineTo".equals(aToken.getString("reqType"))) {
				// TODO: implement multiple canvas, this is what is used for
				// int id = aToken.getInteger("identifier");
				// if (id != getTaskId()) {
				float lEX = new Float(aToken.getDouble("x", 0.0));
				float lEY = new Float(aToken.getDouble("y", 0.0));
				// draw the line
				lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
				// invalidate image view to re-draw the canvas

				mIsDirty = true;

				lSX = lEX;
				lSY = lEY;
			} else if ("line".equals(aToken.getString("reqType"))) {
				// TODO: implement multiple canvas, this is what is used for
				// int id = aToken.getInteger("identifier");
				// if (id != getTaskId()) {
				lSX = new Float(aToken.getDouble("x1", 0.0));
				lSY = new Float(aToken.getDouble("y1", 0.0));
				float lEX = new Float(aToken.getDouble("x2", 0.0));
				float lEY = new Float(aToken.getDouble("y2", 0.0));
				// draw the line
				String lColStr = aToken.getString("color");
				if (lColStr != null
						&& lColStr.length() == 7
						&& lColStr.startsWith("#")) {
					int lColVal;
					try {
						lColVal = Integer.valueOf(lColStr.substring(1), 16);
					} catch (Exception lEx) {
						lColVal = 0;
					}
					lPaint.setColor(0xFF000000 | lColVal);
				}
				lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
				// invalidate image view to re-draw the canvas
				mIsDirty = true;

				lSX = lEX;
				lSY = lEY;
			} else if ("closePath".equals(aToken.getString("reqType"))) {
				// nothing to do here
			} else if ("clear".equals(aToken.getString("reqType"))) {
				clearCanvas();
			}

		}
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		// lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
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
		// lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
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
