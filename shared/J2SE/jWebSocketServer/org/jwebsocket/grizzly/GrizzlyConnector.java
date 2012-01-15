//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Connector
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
package org.jwebsocket.grizzly;

import java.net.InetAddress;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.websockets.WebSocket;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author ashulze, vbarzana
 */
public class GrizzlyConnector extends BaseConnector {

    private static Logger mLog = Logging.getLogger(GrizzlyConnector.class);
    private boolean mIsRunning = false;
    private CloseReason mCloseReason = CloseReason.TIMEOUT;
    private WebSocket mConnection;
    private HttpRequestPacket mRequest = null;
    private String mProtocol = null;

    /**
     * creates a new Grizzly connector for the passed engine using the passed client
     * socket. Usually connectors are instantiated by their engine only, not by
     * the application.
     *
     * @param aEngine
     * @param aHttpRequestPacket
     * @param aProtocol
     * @param aClientSocket
     */
    public GrizzlyConnector(WebSocketEngine aEngine, HttpRequestPacket aRequest,
            String aProtocol, WebSocket socket) {
        super(aEngine);
        mConnection = socket;
        mRequest = aRequest;
        mProtocol = aProtocol;

        RequestHeader lHeader = new RequestHeader();
        
        // Iterating through URL args
        Map<String, String> lArgs = new FastMap<String, String>();
        // TODO: ITERATE THROUGH URL ARGS
//        Set<String> lAtNames = aRequest.getAttributeNames();
        
//        for (String lArgName : lAtNames) {
        // TODO: Pass arbitrary attributes to see if works
//            String[] lArgVals = (String[])aRequest.getAttribute(lArgName);
//            if (lArgVals != null && lArgVals.length > 0) {
//                lArgs.put(lArgName, lArgVals[0]);
//            }
//        }
        
        lHeader.put(RequestHeader.URL_ARGS, lArgs);

        // set default sub protocol if none passed
        if (aProtocol == null) {
            aProtocol = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
        }
        lHeader.put(RequestHeader.WS_PROTOCOL, aProtocol);
        lHeader.put(RequestHeader.WS_PATH, aRequest.getRequestURI());

        // iterate throught header params
        MimeHeaders lHeaderNames = aRequest.getHeaders();
        for (String lHeaderName : lHeaderNames.names()) {
            if (lHeaderName != null) {
                lHeaderName = lHeaderName.toLowerCase();
                lHeader.put(lHeaderName, aRequest.getHeader(lHeaderName));
            }
        }

        lHeader.put(RequestHeader.WS_SEARCHSTRING, aRequest.getQueryString());
        setHeader(lHeader);
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public void startConnector() {
        if (mLog.isDebugEnabled()) {
            mLog.debug("-------------------------------------------------------------");
            mLog.debug("Starting Grizzly connector at port " + getRemotePort() + "...");
            mLog.debug("-------------------------------------------------------------");
        }
        mIsRunning = true;

        if (mLog.isInfoEnabled()) {
            mLog.info("Started Grizzly connector at port " + getRemotePort());
        }

        // call connectorStarted method of engine
        WebSocketEngine lEngine = getEngine();
        if (lEngine != null) {
            lEngine.connectorStarted(this);
        }
    }

    @Override
    public void stopConnector(CloseReason aCloseReason) {
        if (mIsRunning) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("Stopping Grizzly connector ("
                        + aCloseReason.name() + ") at port "
                        + getRemotePort() + "...");
            }
            // call client stopped method of engine
            // (e.g. to release client from streams)
            WebSocketEngine lEngine = getEngine();
            if (lEngine != null) {
                lEngine.connectorStopped(this, aCloseReason);
            }
            //TODO: add proper reason
            mConnection.close();
            mCloseReason = aCloseReason;
            mIsRunning = false;
            if (mLog.isInfoEnabled()) {
                mLog.info("Stopped Grizzly connector ("
                        + aCloseReason.name() + ") at port "
                        + getRemotePort() + ".");
            }
        }
    }

    @Override
    public void processPacket(WebSocketPacket aDataPacket) {
        // forward the data packet to the engine
        // the engine forwards the packet to all connected servers
        getEngine().processPacket(this, aDataPacket);
    }

    @Override
    public synchronized void sendPacket(WebSocketPacket aDataPacket) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Sending packet '" + aDataPacket.getUTF8() + "'...");
        }

        try {
            if (aDataPacket.getFrameType() == WebSocketFrameType.BINARY) {
                mConnection.send(aDataPacket.getByteArray());
                // mOutbound.sendMessage((byte) 0, );
//                byte[] lBA = aDataPacket.getByteArray();
//                mConnection.send(lBA);
            } else {
                mConnection.send(aDataPacket.getUTF8());
            }
        } catch (Exception lEx) {
            mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
        }
        if (mLog.isDebugEnabled()) {
            mLog.debug("Packet '" + aDataPacket.getUTF8() + "' sent.");
        }
    }

    @Override
    public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
        throw new UnsupportedOperationException("Underlying connector:"
                + getClass().getName() + " doesn't support asynchronous send operation");
    }

    @Override
    public String generateUID() {
        String lUID = getRemoteHost() + "@" + getRemotePort();
        return lUID;
    }

    @Override
    public int getRemotePort() {
        return mRequest.getRemotePort();
    }

    @Override
    public InetAddress getRemoteHost() {
        InetAddress lAddr;
        try {
            lAddr = InetAddress.getByName(mRequest.getRemoteAddress());
        } catch (Exception lEx) {
            lAddr = null;
        }
        return lAddr;
    }

    @Override
    public String toString() {
        // TODO: weird results like... '0:0:0:0:0:0:0:1:61130'... on JDK 1.6u19
        // Windows 7 64bit
        String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
        // TODO: don't hard code. At least use JWebSocketConstants field here.
        String lUsername = getString("org.jwebsocket.plugins.system.username");
        if (lUsername != null) {
            lRes += " (" + lUsername + ")";
        }
        return lRes;
    }
}
