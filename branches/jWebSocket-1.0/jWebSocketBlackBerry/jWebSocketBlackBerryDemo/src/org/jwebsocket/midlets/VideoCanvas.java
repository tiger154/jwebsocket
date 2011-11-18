//	---------------------------------------------------------------------------
//	jWebSocket - VideoCanvas
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

import javax.microedition.lcdui.*;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;

/**
 *
 * @author  Alexander Schulze
 * @version
 */
public class VideoCanvas extends Canvas {

	private MIDlet mMIDlet;

	public VideoCanvas(MIDlet aMIDlet, VideoControl aVideoControl) {
		int lWidth = getWidth();
		int lHeight = getHeight();

		mMIDlet = aMIDlet;

		aVideoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
		try {
			aVideoControl.setDisplayLocation(0, 0);
			aVideoControl.setDisplaySize(lWidth, lHeight - 16);
		} catch (MediaException me) {
		}
		aVideoControl.setVisible(true);
	}

	public void paint(Graphics g) {
		int lWidth = getWidth();
		int lHeight = getHeight();

		// g.setColor(0x00ff00);
		// g.drawRect(0, 0, lWidth - 1, lHeight - 1);
		// g.drawRect(1, 1, lWidth - 3, lHeight - 3);
	}

}
