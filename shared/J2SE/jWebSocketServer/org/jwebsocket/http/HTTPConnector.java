// ---------------------------------------------------------------------------
// jWebSocket - HTTPConnector (Community Edition, CE)
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
package org.jwebsocket.http;

import java.net.InetAddress;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.jwebsocket.api.IConnectorsPacketQueue;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class HTTPConnector extends BaseConnector {

	private final String mId;
	private InetAddress mRemoteHost;
	private final IConnectorsPacketQueue mPacketsQueue;
	public final static String CLOSE_COMMAND = "http.command.close";
	private HttpServletResponse mHttpResponse;

	public HTTPConnector(WebSocketEngine aEngine, String aConnectorId, IConnectorsPacketQueue aPacketsQueue) {
		super(aEngine);
		mId = aConnectorId;
		mPacketsQueue = aPacketsQueue;

		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		setHeader(lHeader);
	}

	public void setHttpResponse(HttpServletResponse aHttpResponse) {
		mHttpResponse = aHttpResponse;
	}

	public HttpServletResponse getHttpResponse() {
		return mHttpResponse;
	}
	
	@Override
	public InetAddress getRemoteHost() {
		return mRemoteHost;
	}

	public void setRemoteHost(InetAddress aRemoteHost) {
		mRemoteHost = aRemoteHost;
	}

	@Override
	public int getRemotePort() {
		return -1;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public Integer getMaxFrameSize() {
		return getEngine().getConfiguration().getMaxFramesize();
	}

	public void setCustomVarsContainer(Map<String, Object> aMap) {
		mCustomVars = aMap;
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
		try {
			if (null == mHttpResponse) {
				mPacketsQueue.enqueue(mId, aDataPacket.getString());
			} else {
				// sending packet directly through the HttpServletResponse instance
				HTTPServlet.sendMessage(200, aDataPacket.getString(), mHttpResponse);
				mHttpResponse = null;
			}
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		sendPacket(aDataPacket);

		return null;
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		try {
			// discarding previous sent packets
			mPacketsQueue.clear(mId);

			// sending close command
			sendPacket(new RawPacket(CLOSE_COMMAND));
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}

		super.stopConnector(aCloseReason);
	}
}
