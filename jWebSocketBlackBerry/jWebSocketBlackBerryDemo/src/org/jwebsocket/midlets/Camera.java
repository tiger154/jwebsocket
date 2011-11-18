//	---------------------------------------------------------------------------
//	jWebSocket - Camera Form
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.midlets;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;
import org.jwebsocket.token.TokenClient;

/**
 *
 * @author aschulze
 */
public class Camera implements CommandListener {

	private static int lImgCounter = 0;
	private Player mPlayer;
	private VideoControl mVideoControl;
	private Video mVideo;
	private TokenClient mClient;
	private MIDlet mMIDlet;
	private Display mDisplay;
	private Form mForm = null;
	private Form mPrevForm = null;
	private Command mCmdBack = new Command("Back", Command.BACK, 0);
	private Command mCmdSnapshot = new Command("Snap!", Command.OK, 0);

	public Camera(TokenClient aClient, MIDlet aMIDlet, Form aPrevForm) {
		// save WebSocket client
		mClient = aClient;
		// save midlet
		mMIDlet = aMIDlet;
		// save display for quicker access
		mDisplay = Display.getDisplay(aMIDlet);
		// create new form for camera
		mForm = new Form("Camera", new Item[]{});
		mPrevForm = aPrevForm;

		mStart();
	}

	private void mStart() {
		mPlayer = null;
		try {
			// "capture://image" is used for Series 40 devices
			mPlayer = Manager.createPlayer("capture://image");
		} catch (Exception ex) {
		}
		if (mPlayer == null) {
			try {
				// "capture://video" is used for Series 60 devices
				mPlayer = Manager.createPlayer("capture://video");
			} catch (Exception ex) {
			}
		}
		if (mPlayer == null) {
			mForm.setTitle("Video unavailable.");
			return;
		}
		try {
			mPlayer.realize();

			mVideoControl = (VideoControl) mPlayer.getControl("VideoControl");
			Canvas lCanvas = new VideoCanvas(mMIDlet, mVideoControl);
			lCanvas.addCommand(mCmdBack);
			lCanvas.addCommand(mCmdSnapshot);
			lCanvas.setCommandListener(this);
			mDisplay.setCurrent(lCanvas);
			mPlayer.start();
		} catch (MediaException me) {
		}
	}

	private void mStop() {
		mPlayer.close();
		mPlayer = null;
		mVideoControl = null;
	}

	private void mTakeSnapshot() {
		// take and send snap shot in a separate thread
		mVideo = new Video();
		mVideo.start();
	}

	class Video extends Thread {

		public Video() {
		}

		public void run() {
			try {
				byte[] lRAW = mVideoControl.getSnapshot("encoding=jpeg");
				// &width=160&height=120

				if (mClient.isConnected()) {
					lImgCounter++;
					mClient.saveFile(lRAW, "img_" + lImgCounter + ".jpg",
							"public", Boolean.TRUE);
				}
				Image lImage = Image.createImage(lRAW, 0, lRAW.length);
				ImageItem lImgItem = new ImageItem("", lImage, 0, "");
				mPrevForm.append(lImgItem);
			} catch (Exception ex) {
				mForm.setTitle(ex.getMessage());
			}
		}
	}

	public void commandAction(Command aCommand, Displayable aDisplayable) {
		if (aCommand == mCmdBack) {
			mStop();
			mDisplay.setCurrent(mPrevForm);
		} else if (aCommand == mCmdSnapshot) {
			mTakeSnapshot();
		}
	}
}
