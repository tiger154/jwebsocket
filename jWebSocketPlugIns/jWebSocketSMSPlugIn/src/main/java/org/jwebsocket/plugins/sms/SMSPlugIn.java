//	---------------------------------------------------------------------------
//	jWebSocket SMS Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.springframework.context.ApplicationContext;

/**
 * Allows to clients and internal applications send SMS text messages to mobile
 * phones through WebSocket protocol.
 *
 * @author mayra, aschulze
 */
public class SMSPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	private static final String NS_SMS =
			JWebSocketServerConstants.NS_BASE + ".plugins.sms";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SMSPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SMSPlugIn - Community Edition";
	/**
	 * PlugIn dependencies
	 */
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	private ISMSProvider mProvider;

	/**
	 * Constructor with plug-in configuration
	 *
	 * @param aConfiguration the plug-in configuration for this PlugIn
	 */
	public SMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating SMS plug-in...");
		}
		this.setNamespace(NS_SMS);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for SMS plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.sms.settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("SMS plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating SMS plug-in"));
		}

		if (null != mSettings) {
			// just for developers convenience
			mProvider = mSettings.getProvider();
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
		return NS_SMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if (aToken.getType().equals("sendSMS")) {
				try {
					sendSMS(aConnector, aToken);
				} catch (Exception ex) {
					mLog.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("sendSMS")) {
				return send(aConnector, aToken);
			}
		}
		return null;
	}

	/**
	 * Allows to send a SMS through a defined provider. Returns a map with the 
	 * response code from the SMS provider. This method allows to others 
	 * plug-ins send SMS text messages through the
	 * {@link invoke(WebSocketConnector, Token)} method.
	 * 
	 * @param aConnector the client connector
	 * @param aToken the request token object that should contain the followings 
	 *				 attributes:
	 *				 <p>
	 *				 <ul>
	 *				 <li>
	 *					message: SMS message text
	 *				 </li>
	 *				 <li>
	 *					to: Receiver of SMS
	 *				 </li>
	 *				 <li>
	 *					from: Source identifier
	 *				 </li>
	 *				 </ul>
	 *				 </p>
	 * @return a map with the response code from the SMS provider 
	 */
	private Token send(WebSocketConnector aConnector, Token aToken) {
		Token lRes = mProvider.sendSms(aToken);
		if (mLog.isInfoEnabled()) {
			mLog.info("Provider returned: " + lRes.toString());
		}
		getServer().setResponseFields(aToken, lRes);
		return lRes;
	}

	/**
	 * Sends to the client connector that request it a map with the response 
	 * code from the SMS provider. This method use the 
	 * {@link send(WebSocketConnector, Token)} method to send the text message.
	 * 
	 * @param aConnector the client connector
	 * @param aToken the request token object
	 */
	private void sendSMS(WebSocketConnector aConnector, Token aToken) {
		Token lRes = send(aConnector, aToken);
		sendToken(aConnector, aConnector, lRes);
	}
}