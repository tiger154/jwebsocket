//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Client for Java Standard Edition (J2SE)
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
package org.jwebsocket.client.me;

import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.java.BaseClient;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketHandshake;
import org.jwebsocket.listener.WebSocketClientEvent;

/**
 *
 * @author aschulze
 */
public class BaseClientJ2ME extends BaseClient {

	private boolean isRunning = false;
	private Thread inboundThread;
	private InboundProcess inboundProcess;
	private SocketConnection socket = null;
	private InputStream is = null;
	private OutputStream os = null;

	public BaseClientJ2ME() {
	}

	public void open(String aURL) throws WebSocketException {
		try {
			socket = (SocketConnection) Connector.open(aURL, Connector.READ_WRITE); // "socket://localhost:8787"

			is = socket.openInputStream();
			os = socket.openOutputStream();

			// send handshake to server
			byte[] lReq = WebSocketHandshake.generateC2SRequest("localhost:8787", "/");
			os.write(lReq);
			os.flush();

			// wait on handshake response
			byte[] lBuff = WebSocketHandshake.readS2CResponse(is);

			// parse handshake response from server
			// Map lResp = WebSocketHandshake.parseS2CResponse(lBuff);

			inboundProcess = new InboundProcess();
			inboundThread = new Thread(inboundProcess);
			inboundThread.start();

		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName() + " when opening WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void send(String aData, String aEncoding) throws WebSocketException {
		try {
			os.write(0x00);
			os.write(aData.getBytes(aEncoding));
			os.write(0xff);
			os.flush();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName() + " when sending via WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void send(byte[] aData) throws WebSocketException {
		try {
			os.write(0x00);
			os.write(aData);
			os.write(0xff);
			os.flush();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName() + " when sending via WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void received(String aData, String aEncoding) throws WebSocketException {
	}

	public void received(byte[] aData) throws WebSocketException {
	}

	public void close() throws WebSocketException {
		isRunning = false;
		try {
			os.close();
			is.close();
			socket.close();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName()
					+ " when closing WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	private class InboundProcess implements Runnable {

		public void run() {
			isRunning = true;
			byte[] lBuff = new byte[MAX_FRAME_SIZE];
			int lPos = -1;
			int lStart = -1;

			WebSocketClientEvent lEvent = new WebSocketClientEvent();
			notifyOpened(lEvent);

			while (isRunning) {
				try {
					int b = is.read();
					// start of frame
					if (b == 0x00) {
						lPos = 0;
						lStart = 0;
						// end of frame
					} else if (b == 0xff) {
						if (lStart >= 0) {
							byte[] lBA = new byte[lPos];
							System.arraycopy(lBuff, 0, lBA, 0, lPos);
							received(lBA);
							lEvent = new WebSocketClientEvent();
							WebSocketPacket lPacket = new RawPacket(lBA);
							notifyPacket(lEvent, lPacket);
						}
						lStart = -1;
						// end of stream
					} else if (b < 0) {
						isRunning = false;
						// any other byte within or outside a frame
					} else {
						if (lStart >= 0) {
							lBuff[lPos] = (byte) b;
						}
						lPos++;
					}
				} catch (Exception ex) {
					isRunning = false;
					// throw new WebSocketException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}

			lEvent = new WebSocketClientEvent();
			notifyClosed(lEvent);
		}
	}

	public boolean isConnected() {
		return isRunning;
	}
}
