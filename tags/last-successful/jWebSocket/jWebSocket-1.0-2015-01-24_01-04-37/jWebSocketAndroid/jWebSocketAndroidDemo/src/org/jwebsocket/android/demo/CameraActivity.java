//	---------------------------------------------------------------------------
//	jWebSocket - CameraActivity (Community Edition, CE)
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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Alexander Schulze
 */
public class CameraActivity extends Activity implements
		WebSocketClientTokenListener, SurfaceHolder.Callback {

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera = null;
	private boolean mPreviewRunning = false;
	private Camera.PictureCallback mPictureCallback;
	private static int mImgId = 1;

	/**
	 * Called when the activity is first created.
	 *
	 * @param icicle
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Window lWin = getWindow();
		lWin.setFormat(PixelFormat.TRANSLUCENT);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// needs to be called before setContentView to be applied
		lWin.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.camera_hvga_p);

		mSurfaceView = (SurfaceView) findViewById(R.id.sfvCamera);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		// mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Instantiate an ImageView and define its properties
		ImageView lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
		if (lImgStatus != null) {
			lImgStatus.bringToFront();
		}
		mPictureCallback = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] aImageData, Camera aCamera) {
				try {
					/*
					 * test code: byte [] lBA = new byte[1000000];
					 * JWC.saveFile(lBA, "ba_" + Tools.intToString(mImgId, 4) +
					 * ".null", JWebSocketCommonConstants.SCOPE_PUBLIC, true);
					 */

					// save file in public area and send notification
					JWC.saveFile(aImageData,
							"img_" + Tools.intToString(mImgId, 4) + ".jpg",
							JWebSocketCommonConstants.SCOPE_PUBLIC, true);
					mImgId++;
				} catch (WebSocketException ex) {
					// TODO: handle exception
				}
				Toast.makeText(getApplicationContext(),
						"Photo has been taken!", Toast.LENGTH_SHORT).show();
			}
		};

		mSurfaceView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View aView) {
				mCamera.autoFocus(new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean arg0, Camera arg1) {
						mCamera.takePicture(null, null, mPictureCallback);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException ex) {
						}
						mCamera.startPreview();
						mPreviewRunning = true;
					}
				});

			}
		});
	}

	/**
	 *
	 */
	@Override
	protected void onResume() {
		super.onResume();

		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
		}
	}

	/**
	 *
	 */
	@Override
	protected void onPause() {
		try {
			JWC.close();
			JWC.removeListener(this);
		} catch (WebSocketException ex) {
		}
		super.onPause();
	}

	/**
	 *
	 * @param aSurfaceHolder
	 */
	@Override
	public void surfaceCreated(SurfaceHolder aSurfaceHolder) {
		mCamera = Camera.open();
	}

	/**
	 *
	 * @param aSurfaceHolder
	 * @param aFormat
	 * @param aWidth
	 * @param aHeight
	 */
	@Override
	public void surfaceChanged(SurfaceHolder aSurfaceHolder, int aFormat,
			int aWidth, int aHeight) {
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		Camera.Parameters lParms = mCamera.getParameters();
		lParms.setPreviewSize(aWidth, aHeight);
		mCamera.setParameters(lParms);
		try {
			mCamera.setPreviewDisplay(aSurfaceHolder);
		} catch (IOException e) {
			// TODO: exception handling
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	/**
	 *
	 * @param aSurfaceHolder
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder aSurfaceHolder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();

	}

	/**
	 *
	 * @param aEvent
	 * @param aToken
	 */
	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		ImageView lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
		if (lImgStatus != null) {
			// TODO: in fact it is only connected, not yet authenticated!
			lImgStatus.setImageResource(R.drawable.authenticated);
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
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		ImageView lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
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
