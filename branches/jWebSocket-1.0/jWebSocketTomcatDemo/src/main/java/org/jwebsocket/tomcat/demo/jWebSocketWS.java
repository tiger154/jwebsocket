//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2012, Innotrade GmbH jwebsocket.org
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
package org.jwebsocket.tomcat.demo;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.tomcat.TomcatWrapper;

/**
 *
 * @author aschulze
 */
public class jWebSocketWS extends WebSocketServlet {

	private HttpServletRequest mRequest;

	@Override
	protected StreamInbound createWebSocketInbound(String aSubProtocol) {
		if (null == aSubProtocol) {
			aSubProtocol = JWebSocketCommonConstants.WS_SUBPROT_JSON;
		}
		return new TomcatWrapper(mRequest, aSubProtocol);
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected String selectSubProtocol(List<String> aSubProtocols) {
		// super.selectSubProtocol(aSubProtocols);
		return TomcatWrapper.selectSubProtocol(aSubProtocols);
	}

	@Override
	protected boolean verifyOrigin(String aOrigin) {
		// super.verifyOrigin(aOrigin);
		return TomcatWrapper.verifyOrigin(aOrigin);
	}

	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		// save the request, since this is not available anymore in the createWebSocketInbound method
		mRequest = aRequest;
		super.service(aRequest, aResponse);
	}
}
