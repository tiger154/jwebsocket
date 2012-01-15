//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly WebSocket Servlet Wrapper
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

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;

/**
 * 
 * @author vbarzana
 */
public class GrizzlyWebSocketApplication extends WebSocketApplication {

    private static Logger mLog = Logging.getLogger(GrizzlyWebSocketApplication.class);
    private WebSocketConnector mConnector = null;
    private WebSocketEngine mEngine = null;
    private HttpRequestPacket mRequest = null;
    private String mProtocol = null;

    public GrizzlyWebSocketApplication(WebSocketEngine aEngine) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Instantiating Grizzly Wrapper with subprotocol '" + mProtocol + "'...");
        }
        mEngine = aEngine;
    }

    @Override
    public boolean isApplicationRequest(HttpRequestPacket request) {
        mRequest = request;

        String context = mEngine.getConfiguration().getContext();
        String servlet = mEngine.getConfiguration().getServlet();

        return (context + servlet).equals(request.getRequestURI());
    }

    @Override
    public void onConnect(WebSocket socket) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Connecting Grizzly Client...");
        }

        mConnector = new GrizzlyConnector(mEngine, mRequest, mProtocol, socket);
        mEngine.addConnector(mConnector);
        // inherited BaseConnector.startConnector
        // calls mEngine connector started

        mConnector.startConnector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(WebSocket websocket, DataFrame frame) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Disconnecting Grizzly Client...");
        }
        if (mConnector != null) {
            // inherited BaseConnector.stopConnector
            // calls mEngine connector stopped
            mConnector.stopConnector(CloseReason.CLIENT);
            mEngine.removeConnector(mConnector);
        }
    }

    @Override
    public void onFragment(WebSocket socket, String fragment, boolean last) {
        super.onFragment(socket, fragment, last);
        if (mConnector != null) {
            mEngine.processPacket(mConnector, new RawPacket(fragment));
        }
    }

    @Override
    public void onFragment(WebSocket socket, byte[] fragment, boolean last) {
        super.onFragment(socket, fragment, last);
        if (mConnector != null) {
            mEngine.processPacket(mConnector, new RawPacket(fragment));
        }
    }

    @Override
    public void onMessage(WebSocket websocket, String data) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Message (text) from Grizzly client...");
        }
        if (mConnector != null) {
            mEngine.processPacket(mConnector, new RawPacket(data));
        }
    }

    @Override
    public void onMessage(WebSocket socket, byte[] bytes) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Message (binary) from Grizzly client...");
        }
        if (mConnector != null) {
            mEngine.processPacket(mConnector, new RawPacket(bytes));
        }
    }

    @Override
    public void onPing(WebSocket socket, byte[] bytes) {
        super.onPing(socket, bytes);
        if (mConnector != null) {
            mConnector.processPing(new RawPacket(bytes));
        }
    }

    @Override
    public void onPong(WebSocket socket, byte[] bytes) {
        super.onPong(socket, bytes);
        if (mConnector != null) {
            mConnector.processPong(new RawPacket(bytes));
        }
    }
}