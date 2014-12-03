//	---------------------------------------------------------------------------
//	jWebSocket JMS Demo Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.jmsdemo;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Alexander Schulze
 */
public class JMSDemoPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_JMSDEMO = JWebSocketServerConstants.NS_BASE + ".plugins.jmsdemo";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JMSDemoPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket JMSDemoPlugIn - Community Edition";
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
	public JMSDemoPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMS Demo plug-in...");
		}
		// specify default name space for JMS demo plugin
		this.setNamespace(NS_JMSDEMO);

		try {
			ApplicationContext lBeanFactory = getConfigBeanFactory(NS_JMSDEMO);
			mSettings = (Settings) lBeanFactory.getBean("org.jwebsocket.plugins.jmsdemo.settings");

			if (mLog.isInfoEnabled()) {
				mLog.info("JMS Demo plug-in successfully instantiated.");
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"instantiating JMS Demo Plug-in."));
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
		return NS_JMSDEMO;
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("echo")) {
				echo(aConnector, aToken);
			} else if (lType.equals("decryptDemo")) {
				decryptDemo(aConnector, aToken);
			}
		}
	}

	private void echo(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		Token lResponse = createResponse(aToken);

		String lEcho = aToken.getString("echo");
		if (null == lEcho) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "No echo message passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		lServer.sendToken(aConnector, lResponse);
	}

	@SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
	private void decryptDemo(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		Token lResponse = createResponse(aToken);

		String lMessage = aToken.getString("message");
		if (null == lMessage) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "No ensrypted message passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}
		if (null == mSettings.getAESPassPhrase()) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "No AESPassphrase configured for JMS demo plug-in.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		try {
			SecretKeySpec lSecKeySpec = new SecretKeySpec(mSettings.getAESPassPhrase().getBytes(), "AES");
			Cipher lCipher = Cipher.getInstance("AES");
			lCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(lSecKeySpec.getEncoded(), "AES"));
			byte[] lDecrypted = lCipher.doFinal(lMessage.getBytes());
			lResponse.setString("decrypted", new String(lDecrypted));
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "decrypting message"));
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		
		lServer.sendToken(aConnector, lResponse);
	}

}
