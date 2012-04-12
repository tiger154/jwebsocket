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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.*;
import android.widget.ImageView;
import java.io.IOException;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 */
public class VideoActivity extends Activity implements WebSocketClientTokenListener, SurfaceHolder.Callback {

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera = null;
	private boolean mPreviewRunning = false;
	private Camera.PictureCallback mPictureCallback;
	private static int mFrameId = 1;

	/** Called when the activity is first created. */
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
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Instantiate an ImageView and define its properties
		ImageView lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
		if (lImgStatus != null) {
			lImgStatus.bringToFront();
		}
		mPictureCallback = new Camera.PictureCallback() {

			@Override
			public void onPictureTaken(byte[] aImageData, Camera aCamera) {
				try {
					// save file in public area and send notification
					/*
					Bitmap lImg = BitmapFactory.decodeByteArray( aImageData, 0, aImageData.length );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					lImg.compress(CompressFormat.PNG, 100, baos);
					JWC.sendFile("data:image/png;base64,", baos.toByteArray(), "frame_" + Tools.intToString(mFrameId, 4) + ".jpg", "target2");
					 */
					JWC.sendFile("data:image/jpeg;base64,", aImageData, "frame_" + Tools.intToString(mFrameId, 4) + ".jpg", "target2");
					mFrameId++;
				} catch (WebSocketException ex) {
					// TODO: handle exception
				}
			}
		};

		mSurfaceView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View aView) {
				mCamera.takePicture(null, null, mPictureCallback);
				mCamera.startPreview();
				mPreviewRunning = true;
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
	public void surfaceCreated(SurfaceHolder aSurfaceHolder) {
		mCamera = Camera.open();
	}

	@Override
	public void surfaceChanged(SurfaceHolder aSurfaceHolder, int aFormat, int aWidth, int aHeight) {
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

	@Override
	public void surfaceDestroyed(SurfaceHolder aSurfaceHolder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();

	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		ImageView lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
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
		ImageView lImgStatus = (ImageView) findViewById(R.id.cameraImgStatus);
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
