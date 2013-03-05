//	---------------------------------------------------------------------------
//	jWebSocket - Servlet Connector (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.appserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServletRequest;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;

/**
 *
 * @author aschulze
 */
public class ServletConnector extends BaseConnector {

	private HttpServletRequest mRequest = null;
	// private HttpServletResponse mResponse = null;
	private String mPlainResponse = "";

	/**
	 *
	 */
	public ServletConnector() {
		// no "engine" available here
		super(null);
		// TODO: Overhaul this hardcoded reference! See TokenServer class!
		setBoolean("org.jwebsocket.tokenserver.isTS", true);
		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, JWebSocketCommonConstants.WS_SUBPROT_DEFAULT);
		setHeader(lHeader);
	}

	@Override
	public void startConnector() {
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
	}

	@Override
	public void processPacket(WebSocketPacket aDataPacket) {
	}

	@Override
	public void sendPacket(WebSocketPacket aDataPacket) {
		mPlainResponse = aDataPacket.getUTF8();
	}

	@Override
	public WebSocketEngine getEngine() {
		return null;
	}

	@Override
	public String generateUID() {
		return getRequest().getSession().getId();
	}

	@Override
	public int getRemotePort() {
		return getRequest().getRemotePort();
	}

	@Override
	public InetAddress getRemoteHost() {
		try {
			return InetAddress.getByName(getRequest().getRemoteAddr());
		} catch (UnknownHostException ex) {
			return null;
		}
	}

	@Override
	public String getId() {
		return String.valueOf(getRemotePort());
	}

	/**
	 *
	 * @return
	 */
	public String getPlainResponse() {
		return mPlainResponse;
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return mRequest;
	}

	/**
	 * @param aRequest the request to set
	 */
	public void setRequest(HttpServletRequest aRequest) {
		this.mRequest = aRequest;
	}
}
