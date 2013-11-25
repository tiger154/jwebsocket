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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.itemstorage.ItemStoragePlugIn;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Allows to clients and internal applications send SMS text messages to mobile
 * phones through WebSocket protocol.
 *
 * @author mayra, aschulze
 */
public class SMSPlugIn extends ActionPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private static final String NS = JWebSocketServerConstants.NS_BASE + ".plugins.sms";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SMSPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SMSPlugIn - Community Edition";
	private final String TT_SEND_SMS = "sendSMS";
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	private ISMSProvider mProvider;
	private IItemCollection mSMSCollection;

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
	public void systemStarted() throws Exception {
		if (!JWebSocketBeanFactory.getInstance(ItemStoragePlugIn.NS_ITEM_STORAGE)
				.containsBean("collectionProvider")) {
			mLog.error("Missing required Item Storage plug-in core components. "
					+ "Some features may not be available!");
			return;
		}

		// setting the SMS item definition
		mSettings.setSMSItemDefinition((IItemDefinition) mBeanFactory.getBean("smsDefinition"));

		// getting the SMS collection
		mSMSCollection = ItemStorageUtils.initialize(mSettings);
	}

	/**
	 * Constructor with plug-in configuration.
	 *
	 * @param aConfiguration the plug-in configuration for this PlugIn
	 */
	public SMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating SMS plug-in...");
		}
		this.setNamespace(NS);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for SMS plug-in, "
						+ "some features may not be available.");
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals(TT_SEND_SMS)) {
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
	 * attributes:
	 * <p>
	 * <ul>
	 * <li>
	 * message: SMS message text
	 * </li>
	 * <li>
	 * to: Receiver of SMS
	 * </li>
	 * <li>
	 * from: Source identifier
	 * </li>
	 * </ul>
	 * </p>
	 * @return a map with the response code from the SMS provider
	 */
	private Token send(WebSocketConnector aConnector, Token aToken) {
		String lFrom = aToken.getString("from");
		String lTo = aToken.getString("to");
		String lMessage = aToken.getString("message");
		String lState = aToken.getString("state");

		Assert.notNull(lFrom, "The 'from' argument can't be null!");
		Assert.notNull(lTo, "The 'to' argument can't be null!");
		Assert.notNull(lMessage, "The 'message' argument can't be null!");
		Assert.notNull(lState, "The 'state' argument can't be null!");

		// sending SMS
		Token lRes = mProvider.sendSms(aToken);
		if (mLog.isInfoEnabled()) {
			mLog.info("Provider returned: " + lRes.toString());
		}
		getServer().setResponseFields(aToken, lRes);

		// if SMS was sent successfully
		if (lRes.getCode() == 0) {
			// save the SMS using itemstorage collection
			try {
				String lUser = "root";
				if (null != aConnector) {
					lUser = aConnector.getUsername();
				}
				ItemStorageUtils.saveSMS(mSMSCollection, lUser, lMessage, lFrom, lTo, lState);

				// notify processing
				notifyProcessed(lUser, aToken, lRes.getCode());
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "saving SMS on itemstorage collection."
						+ "Please check that ItemStorage plug-in is properly working"));
			}
		}

		return lRes;
	}

	@Role(name = NS + ".generateReport")
	public void generateReportAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lUsername = aConnector.getUsername();
		// supporting administrative audit
		if (hasAuthority(aConnector, NS + ".auditReports")) {
			lUsername = aToken.getString("username", lUsername);
		}
		List<IItem> lSMSList = mSMSCollection.getItemStorage().find("user", lUsername);

		List<Map> lFields = new ArrayList<Map>(lSMSList.size());
		for (IItem lSMS : lSMSList) {
			lFields.add(lSMS.getAttributes());
		}

		// creating request for reporting plugin
		Token lRequest = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.reporting", "generateReport");
		lRequest.setString("reportName", "UserSMSReport");
		lRequest.setList("reportFields", lFields);
		lRequest.setString("reportOutputType", "pdf");

		// creating the response
		Token lResponse = createResponse(aToken);
		// calling the reporting plug-in and filling the response token
		lResponse.setString("path", invokePlugIn("jws.reporting", aConnector, lRequest).getString("path"));

		// sending the response
		sendToken(aConnector, lResponse);
	}

	/**
	 * Sends an SMS
	 *
	 * @param aConnector the client connector
	 * @param aToken the request token object
	 */
	@Role(name = NS + ".sendSMS")
	public void sendSMSAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		Token lResponse = send(aConnector, aToken);

		sendToken(aConnector, lResponse);
	}

}
