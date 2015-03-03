//	---------------------------------------------------------------------------
//	jWebSocket TTS Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.tts;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 */
public class TTSPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_TTS = JWebSocketServerConstants.NS_BASE + ".plugins.tts";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket TTS PlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket TTS Plug-in - Community Edition";
	/**
	 *
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 *
	 */
	protected Settings mSettings;

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public TTSPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating TTS plug-in...");
		}
		// specify default name space for file system plugin
		this.setNamespace(NS_TTS);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for TTS plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.tts.settings");
				// [:]
				if (mLog.isInfoEnabled()) {
					mLog.info("TTS plug-in successfully instantiated.");
				}
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating TTS plug-in"));
			throw lEx;
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_TTS;
	}

	@Override
	public synchronized void engineStarted(WebSocketEngine aEngine) {
//		if (mLog.isDebugEnabled()) {
//			mLog.debug("Engine started.");
//		}
	}

	@Override
	public synchronized void engineStopped(WebSocketEngine aEngine) {
//		if (mLog.isDebugEnabled()) {
//			mLog.debug("Engine stopped.");
//		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
//		if (mLog.isDebugEnabled()) {
//			mLog.debug("Connector started.");
//		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
//		if (mLog.isDebugEnabled()) {
//			mLog.debug("Connector stopped.");
//		}
	}

	@Override
	public void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession) {
//		if (mLog.isDebugEnabled()) {
//			mLog.debug("Session '" + aSession.getSessionId() + "' started.");
//		}
	}

	@Override
	public void sessionStopped(WebSocketSession aSession) {
//		if (mLog.isDebugEnabled()) {
//			mLog.debug("Session '" + aSession.getSessionId() + "' stopped.");
//		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("speak")) {
				speak(aConnector, aToken);
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("speak")) {
				return doSpeak(aConnector, aToken);
			}
		}

		return null;
	}

	private Token doSpeak(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		String lText = aToken.getString("text");
		if (null == lText) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "No text argument passed.");
		}

		ITTSProvider lProvider = mSettings.getTTSProvider();
		if (null != lProvider) {
			byte[] aAudio = lProvider.generateAudioFromString(lText, "", "", "");
			String lData = Tools.base64Encode(aAudio);
			lResponse.setInteger("code", 0);
			lResponse.setString("msg", "TTS successful");
			lResponse.setString("audio", lData);
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "TTS failure");
		}

		return lResponse;
	}

	private void speak(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = doSpeak(aConnector, aToken);
		lServer.sendToken(aConnector, lResponse);
	}
}
