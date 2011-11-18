//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket CGI Client
//	Copyright (c) 2010, 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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

import java.io.InputStream;
import java.io.OutputStream;

import org.jwebsocket.client.java.BaseWebSocketClient;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.WebSocketException;

/**
 * The jWebSocket CGI client receives its data from the stdIn channel to
 * the linked application and sends its messages to either the stdOut or
 * stdErr channel to the linked application.
 * @author aschulze
 */
public class CGIClient extends BaseWebSocketClient {

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
    public CGIClient() {
        // assign listener
        super();
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
/*
    @Override
    public void received(byte[] aData) {
        try {
            // is called when a message comes in from the websocket network
            // forward this message to the CGI client
            os.write(aData);
            os.flush();
        } catch (IOException ex) {
            // TODO: handle exception
            //
        }
    }
*/
	
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
            byte[] lBuff = new byte[JWebSocketCommonConstants.DEFAULT_MAX_FRAME_SIZE];
            int lIdx = -1;
            int lStart = -1;

            while (mIsRunning) {
                try {
                    int b = mIn.read();
                    // start of frame
                    if (b == 0x00) {
                        lIdx = 0;
                        lStart = 0;
                        // end of frame
                    } else if (b == 0xff) {
                        if (lStart >= 0) {
                            byte[] lBA = new byte[lIdx];
                            System.arraycopy(lBuff, 0, lBA, 0, lIdx);
                            // Arrays class is not supported in Android
                            // byte[] lBA = Arrays.copyOf(lBuff, pos);
                            mOut.write(lBA);
                        }
                        lStart = -1;
                        // end of stream
                    } else if (b < 0) {
                        mIsRunning = false;
                        // any other byte within or outside a frame
                    } else {
                        if (lStart >= 0) {
                            lBuff[lIdx] = (byte) b;
                        }
                        lIdx++;
                    }
                } catch (Exception ex) {
                    mIsRunning = false;
                    // throw new WebSocketException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    // System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                }
            }

        }
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }
/*
    @Override
    public void received(String aData, String aEncoding) throws WebSocketException {
        // TODO Auto-generated method stub
    }
*/
    @Override
    public void send(String aData, String aEncoding) throws WebSocketException {
        // TODO Auto-generated method stub
    }

    @Override
    public void send(byte[] aData) throws WebSocketException {
        // TODO Auto-generated method stub
    }
}
