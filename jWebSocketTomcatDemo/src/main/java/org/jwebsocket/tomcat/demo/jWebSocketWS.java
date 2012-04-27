/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.tomcat.demo;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.jwebsocket.tomcat.TomcatWrapper;

/**
 *
 * @author aschulze
 */
public class jWebSocketWS extends WebSocketServlet {

	private HttpServletRequest mRequest;

	@Override
	protected StreamInbound createWebSocketInbound(String aSubProtocol) {
		return new TomcatWrapper(mRequest, aSubProtocol);
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected String selectSubProtocol(List<String> subProtocols) {
		return super.selectSubProtocol(subProtocols);
	}

	@Override
	protected boolean verifyOrigin(String origin) {
		return super.verifyOrigin(origin);
	}

	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		// save the request, since this is not available anymore in the createWebSocketInbound method
		mRequest = aRequest;
		super.service(aRequest, aResponse);
	}
}
