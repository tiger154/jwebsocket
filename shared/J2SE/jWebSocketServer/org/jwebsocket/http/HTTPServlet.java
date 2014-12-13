// ---------------------------------------------------------------------------
// jWebSocket - HTTPServlet (Community Edition, CE)
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.tcp.EngineUtils;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class HTTPServlet extends HttpServlet {

	private static Logger mLog;
	private HTTPEngine mEngine;
	private boolean mServerStartupTimeoutConsumed = false;
	private IConnectorsManager mConnectorsManager;

	/**
	 *
	 * @throws ServletException
	 */
	@Override
	public void init() throws ServletException {
		if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
			mLog = Logging.getLogger();

			String lEngineId = getInitParameter(ServletUtils.SERVLET_ENGINE_CONFIG_KEY);
			if (null == lEngineId) {
				lEngineId = "http0";
			}
			mEngine = (HTTPEngine) JWebSocketFactory.getEngine(lEngineId);
			mConnectorsManager = mEngine.getConnectorsManager();

			super.init();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Servlet successfully initialized.");
			}
		} else {
			if (!mServerStartupTimeoutConsumed) {
				try {
					// waiting 3 seconds for the server startup
					log("The request has been paused because the jWebSocket server is not started."
							+ " Waiting for 3 seconds the jWebSocket server startup...");
					Thread.sleep(3000);
				} catch (InterruptedException lEx) {
					throw new ServletException(lEx);
				}
				mServerStartupTimeoutConsumed = true;
				init();
			}

			String lErrMsg = "The jWebSocket server startup is taking too long or has failed. "
					+ "Request cannot be processed in this moment!";
			log(lErrMsg);
			throw new ServletException(lErrMsg);
		}
	}

	String getSessionId(HttpServletRequest aReq) {
		String lSessionId = aReq.getParameter("sessionId");
		if (null == lSessionId) {
			lSessionId = aReq.getSession().getId();
		}

		return Tools.getMD5(lSessionId);
	}

	String getConnectorId(HttpServletRequest aReq, String aSessionId) {
		String lConnectionId = aReq.getParameter("connectionId");
		if (null == lConnectionId) {
			lConnectionId = aSessionId;
		}

		return Tools.getMD5(lConnectionId + aReq.getRemoteAddr());
	}

	void updateSessionTimeout(String aSessionId) {
		mConnectorsManager.getSessionManager().getReconnectionManager()
				.getSessionIdsTrash()
				.put(aSessionId, System.currentTimeMillis()
						+ (mEngine.getConfiguration().getTimeout() * 60 * 1000));
	}

	@Override
	protected void doGet(HttpServletRequest aReq, HttpServletResponse aResp) throws ServletException, IOException {
		String lAction = aReq.getParameter("action");
		String lSessionId = getSessionId(aReq);
		String lConnectorId = getConnectorId(aReq, lSessionId);

		if ("sync".equals(lAction)) {
			processSync(lConnectorId, aReq, aResp);
		} else if ("open".equals(lAction)) {
			if (mConnectorsManager.connectorExists(lConnectorId)) {
				sendMessage(400, "Invalid connection state!", aResp);
				return;
			}

			processOpen(lSessionId, lConnectorId, aReq, aResp);
		} else {
			if (!mConnectorsManager.connectorExists(lConnectorId)) {
				sendMessage(400, "Invalid connection state!", aResp);
				return;
			}

			if ("login".equals(lAction)) {
				processLogin(lConnectorId, aReq, aResp);
			} else if ("logout".equals(lAction)) {
				processLogout(lConnectorId, aReq, aResp);
			} else if ("send".equals(lAction)) {
				processSend(lConnectorId, aReq, aResp);
			} else if ("download".equals(lAction)) {
				processDownload(lConnectorId, aReq, aResp);
			} else if ("close".equals(lAction)) {
				processClose(lConnectorId, aReq, aResp);
			} else {
				sendMessage(404, "Invalid request!", aResp);
			}
		}

		// session management 
		updateSessionTimeout(lSessionId);
	}

	@Override
	protected void doPost(HttpServletRequest aReq, HttpServletResponse aResp) throws ServletException, IOException {
		String lAction = aReq.getParameter("action");
		String lSessionId = getSessionId(aReq);
		String lConnectorId = getConnectorId(aReq, lSessionId);

		if (!mConnectorsManager.connectorExists(lConnectorId)) {
			sendMessage(411, "Invalid connection state!", aResp);
			return;
		}

		if ("send".equals(lAction)) {
			processSend(lConnectorId, aReq, aResp);
		} else {
			sendMessage(404, "Invalid request!", aResp);
		}

		// session management 
		updateSessionTimeout(lSessionId);
	}

	protected static void sendMessage(int aStatusCode, String aMessage, HttpServletResponse aResp) throws IOException {
		aResp.setStatus(aStatusCode);
		aResp.getWriter().print(aMessage);
		aResp.flushBuffer();
		aResp.getWriter().close();
	}

	HTTPConnector getConnector(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws Exception {
		return getConnector(aConnectorId, aReq, aResp, false);
	}

	HTTPConnector getConnector(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp,
			boolean aStartupConnection) throws Exception {
		HTTPConnector lConnector = (HTTPConnector) mConnectorsManager.getConnectorById(aConnectorId, aStartupConnection);
		lConnector.setRemoteHost(InetAddress.getByName(aReq.getRemoteHost()));
		lConnector.setSSL(aReq.getScheme().equals("https"));
		lConnector.setHttpResponse(aResp);

		return lConnector;
	}

	private void processOpen(String aSessionId, String aConnectorId,
			HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		try {
			String lOrigin = aReq.getScheme() + "://" + aReq.getRemoteHost();
			if (!EngineUtils.isOriginValid(lOrigin, mEngine.getConfiguration().getDomains())) {
				sendMessage(401, "Origin not allowed!", aResp);
				return;
			}

			mConnectorsManager.add(aSessionId, aConnectorId);
			HTTPConnector lConnector = getConnector(aConnectorId, aReq, aResp);
			lConnector.setRemoteHost(InetAddress.getByName(aReq.getRemoteHost()));

			// starting connector on request thread intentionally
			lConnector.startConnector();
		} catch (Exception lEx) {
			sendMessage(500, lEx.getLocalizedMessage(), aResp);
		}
	}

	private void processDownload(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		String lFilename = aReq.getParameter("filename");
		String lAlias = aReq.getParameter("alias");

		if (null == lFilename) {
			sendMessage(400, "Missing filename parameter!", aResp);
			return;
		}

		// building login request
		Token lReqToken = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.filesystem", "load");
		lReqToken.setString("filename", lFilename);
		lReqToken.setString("alias", lAlias);
		final WebSocketPacket lPacket = JSONProcessor.tokenToPacket(lReqToken);

		send(aConnectorId, lPacket.getString(), aReq, aResp);
	}

	private void processLogin(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		String lUsername = aReq.getParameter("username");
		String lPassword = aReq.getParameter("password");

		if (null == lUsername || null == lPassword) {
			sendMessage(400, "Invalid request parameters!", aResp);
			return;
		}

		// building login request
		Token lReqToken = TokenFactory.createToken(SystemPlugIn.NS_SYSTEM, "login");
		lReqToken.setString("username", lUsername);
		lReqToken.setString("password", lPassword);
		lReqToken.setInteger("utid", getUTID(aReq));
		final WebSocketPacket lPacket = JSONProcessor.tokenToPacket(lReqToken);

		send(aConnectorId, lPacket.getString(), aReq, aResp);
	}

	private void processLogout(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		try {
			// building logout request
			Token lReqToken = TokenFactory.createToken(SystemPlugIn.NS_SYSTEM, "logout");
			lReqToken.setInteger("utid", getUTID(aReq));
			final WebSocketPacket lPacket = JSONProcessor.tokenToPacket(lReqToken);

			send(aConnectorId, lPacket.getString(), aReq, aResp);
		} catch (Exception lEx) {
			sendMessage(500, lEx.getLocalizedMessage(), aResp);
		}
	}

	private void processClose(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		try {
			final HTTPConnector lConnector = getConnector(aConnectorId, aReq, aResp);
			lConnector.stopConnector(CloseReason.CLIENT);
		} catch (Exception lEx) {
			sendMessage(500, lEx.getLocalizedMessage(), aResp);
		}
	}

	private void processSend(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		final String lData;

		if (aReq.getParameterMap().containsKey("data")) {
			lData = aReq.getParameter("data");
		} else {
			byte[] lBuffer = new byte[mEngine.getConfiguration().getMaxFramesize()];
			ServletInputStream aIS = aReq.getInputStream();

			int lBytesRead = aIS.read(lBuffer);
			lData = new String(lBuffer, 0, lBytesRead);
		}

		if (null == lData || "".equals(lData)) {
			sendMessage(400, "Invalid request parameters!", aResp);
			return;
		}

		send(aConnectorId, lData, aReq, aResp);
	}

	private void send(final String aConnectorId, final String aData, HttpServletRequest aReq,
			HttpServletResponse aResp) throws IOException {
		try {
			final HTTPConnector lConnector = getConnector(aConnectorId, aReq, aResp);
			mEngine.processPacket(lConnector, new RawPacket(aData));
		} catch (Exception lEx) {
			sendMessage(500, lEx.getLocalizedMessage(), aResp);
		}
	}

	private void processSync(String aConnectorId, HttpServletRequest aReq, HttpServletResponse aResp) throws IOException {
		try {
			String lOrigin = aReq.getScheme() + "://" + aReq.getRemoteHost();
			if (!EngineUtils.isOriginValid(lOrigin, mEngine.getConfiguration().getDomains())) {
				sendMessage(401, "Origin not allowed!", aResp);
				return;
			}

			List<String> lPackets = mConnectorsManager.getPacketsQueue().dequeue(aConnectorId);

			sendMessage(200, JSONProcessor.objectToJSONString(lPackets), aResp);
		} catch (Exception lEx) {
			sendMessage(500, lEx.getLocalizedMessage(), aResp);
		}
	}

	public Integer getUTID(HttpServletRequest aReq) {
		String lUTID = aReq.getParameter("utid");
		if (null != lUTID) {
			return Integer.valueOf(lUTID);
		}

		return 0;
	}
}
