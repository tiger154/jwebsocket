//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMS Plug-In
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.sms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author mayra
 */
public class SMSPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private static final String NS_SMS = JWebSocketServerConstants.NS_BASE + ".plugins.sms";
	private static Collection<WebSocketConnector> mClients = new FastList<WebSocketConnector>().shared();
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	private ISMSProvider mProvider;

	public SMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating SMS plug-in...");
		}
		this.setNamespace(aConfiguration.getNamespace());

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for SMS plug-in, some features may not be available.");
			} else {
				mBeanFactory = getConfigBeanFactory();
				mSettings = (Settings) mBeanFactory.getBean("settings");
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
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if (aToken.getType().equals("sms")) {
				try {
					sendSms(aConnector, aToken);
				} catch (Exception ex) {
					mLog.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
			if (aToken.getType().equals("smsList")) {
				mClients.remove(aConnector);
			}
		}
	}

	public void sendSms(WebSocketConnector aConnector, Token aToken) throws MalformedURLException, IOException {
		Token lRes = mProvider.sendSms(aToken);

		mLog.info("Provider returned: " + lRes.toString());

		getServer().setResponseFields(aToken, lRes);
		sendToken(aConnector, aConnector, lRes);
	}
	//public boolean[] sendSmsList(Token aToken){
	//     
	// }
}
