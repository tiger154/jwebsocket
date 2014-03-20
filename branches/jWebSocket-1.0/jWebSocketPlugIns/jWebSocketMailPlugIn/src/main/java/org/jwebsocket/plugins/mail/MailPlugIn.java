//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMTP Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.mail;

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
public class MailPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	/**
	 *
	 */
	public static final String NS
			= JWebSocketServerConstants.NS_BASE + ".plugins.mail";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket MailPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket MailPlugIn - Community Edition";
	private static final MailStore mMailStore = null;
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	private static MailPlugInService mService;

	/**
	 *
	 * @param aConfiguration
	 */
	public MailPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Mail plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS);

		try {
			mBeanFactory = getConfigBeanFactory(NS);
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for mail plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.mail.settings");
				mService = new MailPlugInService(mSettings);
				if (mLog.isInfoEnabled()) {
					mLog.info("Mail plug-in successfully instantiated"
							+ ", SMTP: " + mSettings.getSmtpHost() + ":" + mSettings.getSmtpPort()
							+ ", POP3: " + mSettings.getPop3Host() + ":" + mSettings.getPop3Port()
							+ ".");
				}
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating mail plug-in"));
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
		return NS;
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// select from database
			if (lType.equals("sendMail")) {
				sendMail(aConnector, aToken);
			} else if (lType.equals("createMail")) {
				createMail(aConnector, aToken);
			} else if (lType.equals("dropMail")) {
				dropMail(aConnector, aToken);
			} else if (lType.equals("addAttachment")) {
				addAttachment(aConnector, aToken);
			} else if (lType.equals("removeAttachment")) {
				removeAttachment(aConnector, aToken);
			} else if (lType.equals("moveMail")) {
				moveMail(aConnector, aToken);
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("sendMail")) {
				mService.sendMail(aConnector, aToken, lResponse, lServer, NS);
				return lResponse;
			} else if (lType.equals("createMail")) {
				mService.createMail(aToken, lResponse);
				return lResponse;
			}
		}
		return null;
	}

	private void sendMail(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.sendMail(aConnector, aToken, lResponse, lServer, NS);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void createMail(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.createMail(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void dropMail(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.dropMail(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void addAttachment(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.addAttachment(aConnector, aToken, lResponse, getServer(), NS);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void removeAttachment(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.removeAttachment(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void moveMail(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.moveMail(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getMail(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.getMail(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getUserMails(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		mService.getUserMails(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
	
	/**
	 * Access to the MailPlugInService to be able to send mails from another PlugIn
	 * @return the mService
	 */
	public static MailPlugInService getService() {
		return mService;
	}
}
