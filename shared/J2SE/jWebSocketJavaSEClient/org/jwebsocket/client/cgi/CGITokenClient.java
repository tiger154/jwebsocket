//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket CGI Token Client
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.client.cgi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jwebsocket.client.java.ReliabilityOptions;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class CGITokenClient extends BaseTokenClient {

	private final static char START_FRAME = 0x02; // ASCII STX
	private final static char END_FRAME = 0x03; // ASCII ETX
	private boolean mIsRunning = false;
	private Thread mInboundThread;
	private InboundProcess mInboundProcess;
	private InputStream mIn = null;
	private OutputStream mOut = null;
	private OutputStream mError = null;

	/**
	 *
	 * @param aListener
	 */
	public CGITokenClient() {
	}

	public CGITokenClient(ReliabilityOptions aReliabilityOptions) {
		super(aReliabilityOptions);
	}

	@Override
	public void open(String aURL) throws WebSocketException {
		// establish connection to WebSocket Network
		super.open(aURL);

		// assign streams to CGI channels
		mIn = System.in;
		mOut = System.out;
		mError = System.err;

		// instantiate thread to process messages coming from stdIn
		mInboundProcess = new InboundProcess();
		mInboundThread = new Thread(mInboundProcess);
		mInboundThread.start();
	}

	@Override
	public void close() {
		// stop CGI listener
		mIsRunning = false;
		// and close WebSocket connection
		super.close();
	}

	private class InboundProcess implements Runnable {

		@Override
		public void run() {
			mIsRunning = true;

			ByteArrayOutputStream lBuff = new ByteArrayOutputStream();
			boolean lIsFrame = false;

			while (mIsRunning) {
				try {
					int lByte = mIn.read();
					// start of frame
					if (lByte == START_FRAME) {
						lIsFrame = true;
						lBuff.reset();
						// end of frame
					} else if (lByte == END_FRAME) {
						if (lIsFrame) {
							send(lBuff.toByteArray());
						}
						lIsFrame = false;
						lBuff.reset();
						// end of stream
					} else if (lByte < 0) {
						mIsRunning = false;
						// any other byte within or outside a frame
					} else {
						if (lIsFrame) {
							lBuff.write(lByte);
						}
					}
				} catch (Exception lEx) {
					mIsRunning = false;
					// throw new WebSocketException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		}
	}
}
