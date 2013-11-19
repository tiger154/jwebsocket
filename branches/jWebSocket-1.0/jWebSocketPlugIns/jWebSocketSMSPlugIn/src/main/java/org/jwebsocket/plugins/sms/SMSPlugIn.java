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
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.TokenPlugIn;
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
public class SMSPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	private static final String NS_SMS = JWebSocketServerConstants.NS_BASE + ".plugins.sms";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SMSPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SMSPlugIn - Community Edition";
	private final String QUOTA_PLUGIN_ID = "jws.quota";
	private final String SMS_QUOTA_ID = "jws.quota.plugins.sms";
	private final String TT_TOTAL_SMS = "total_sms";
	private final String TT_SEND_SMS = "sendSMS";
	private boolean mSMSQuotaCreated = false;
	private long mTotalSMSQuota = 200;
	private long mTotalUserQuota = 5;
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	private ISMSProvider mProvider;
	private ActionPlugIn mQuotaPlugIn;

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
	 * Constructor with plug-in configuration.
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
	public void systemStarted() throws Exception {
		super.systemStarted(); //To change body of generated methods, choose Tools | Templates.
		// Getting the QuotaPlugIn
		mQuotaPlugIn = (ActionPlugIn) getPlugInChain().getPlugIn(QUOTA_PLUGIN_ID);
	}

	@Override
	public void processLogon(WebSocketConnector aConnector) {
		super.processLogon(aConnector);
		if (!mSMSQuotaCreated) {
			createJWebSocketSMSQuota(aConnector);
			mSMSQuotaCreated = true;
		}
		long lpendingQuota = createUserQuota(aConnector);
		Token lQuotaToken = TokenFactory.createToken(NS_SMS, TT_TOTAL_SMS);
		if (lpendingQuota == -1) {
			lpendingQuota = mTotalUserQuota;
		}
		lQuotaToken.setLong("pendingQuota", lpendingQuota);
		lQuotaToken.setLong("totalQuota", mTotalUserQuota);
		getServer().sendToken(aConnector, lQuotaToken);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if (aToken.getType().equals(TT_SEND_SMS)) {
				sendSMS(aConnector, aToken);
			}
		}
	}

	private long createUserQuota(WebSocketConnector aConnector) {
		try {
			Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");

			Token lTokenCreateQuota = TokenFactory.createToken(
					JWebSocketServerConstants.NS_BASE + ".plugins.quota", "registerQuota");

			lTokenCreateQuota.setString("q_identifier", "CountDown");
			lTokenCreateQuota.setString("q_value", String.valueOf(mTotalUserQuota));
			lTokenCreateQuota.setString("q_instance", aConnector.getUsername());
			lTokenCreateQuota.setString("q_instance_type", "User");
			lTokenCreateQuota.setString("q_namespace", getNamespace());
			lTokenCreateQuota.setString("q_actions", "create");

			Token lQuota = mQuotaPlugIn.invoke(aConnector, lTokenCreateQuota);
			if (lQuota.getCode() != -1) {
				return lQuota.getLong("value");
			}
		} catch (Exception exp) {
			mLog.error("Error creating the quota to the " + aConnector.getUsername() + " user");
			mLog.error(exp.getMessage());
		}
		return -1;
	}

	private long createJWebSocketSMSQuota(WebSocketConnector aConnector) {
		try {
			Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");

			Token lTokenCreateQuota = TokenFactory.createToken(
					mQuotaPlugIn.getNamespace(), "registerQuota");

			lTokenCreateQuota.setString("q_value", String.valueOf(mTotalSMSQuota));
			lTokenCreateQuota.setString("q_instance", "SMSPlugIn");
			lTokenCreateQuota.setString("q_identifier", "CountDown");
			lTokenCreateQuota.setString("q_uuid", SMS_QUOTA_ID);
			lTokenCreateQuota.setString("q_instance_type", "Application");
			lTokenCreateQuota.setString("q_namespace", getNamespace());
			lTokenCreateQuota.setString("q_actions", "create");

			Token lQuota = mQuotaPlugIn.invoke(aConnector, lTokenCreateQuota);
			if (lQuota.getCode() != -1) {
				return lQuota.getLong("value");
			}
		} catch (Exception aException) {
			mLog.error(aException.getMessage());
		}
		return -1;
	}

	private long countDownUserQuota(WebSocketConnector aConnector) {
		try {
			Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");

			Token lTokenDestroyQuota = TokenFactory.createToken(
					JWebSocketServerConstants.NS_BASE + ".plugins.quota", "reduceQuota");
			lTokenDestroyQuota.setString("q_identifier", "CountDown");
			lTokenDestroyQuota.setString("q_value", "1");
			lTokenDestroyQuota.setString("q_instance", aConnector.getUsername());
			lTokenDestroyQuota.setString("q_instance_type", "User");
			lTokenDestroyQuota.setString("q_namespace", getNamespace());

			Token lToken = mQuotaPlugIn.invoke(aConnector, lTokenDestroyQuota);
			if (lToken.getCode() != -1) {
				return lToken.getLong("value");
			}
		} catch (Exception exp) {
			mLog.error("Error creating the quota to the destroy action");
			mLog.error(exp.getMessage());
		}
		return -1;
	}

	private long queryUserQuota(WebSocketConnector aConnector) {
		try {
			Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");

			Token lTokenDestroyQuota = TokenFactory.createToken(
					JWebSocketServerConstants.NS_BASE + ".plugins.quota", "getQuota");
			lTokenDestroyQuota.setString("q_identifier", "CountDown");
			lTokenDestroyQuota.setString("q_value", "1");
			lTokenDestroyQuota.setString("q_instance", aConnector.getUsername());
			lTokenDestroyQuota.setString("q_instance_type", "User");
			lTokenDestroyQuota.setString("q_namespace", getNamespace());

			Token lToken = mQuotaPlugIn.invoke(aConnector, lTokenDestroyQuota);
			if (lToken.getCode() != -1) {
				return lToken.getLong("value");
			}
		} catch (Exception exp) {
			mLog.error("Error creating the quota to the destroy action");
			mLog.error(exp.getMessage());
		}
		return -1;
	}

	private long countDownSMSPlugInQuota(WebSocketConnector aConnector) {
		try {
			Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");
			
			Token lTokenDestroyQuota = TokenFactory.createToken(
					JWebSocketServerConstants.NS_BASE + ".plugins.quota", "reduceQuota");
			lTokenDestroyQuota.setString("q_identifier", "CountDown");
			lTokenDestroyQuota.setString("q_value", "1");
			lTokenDestroyQuota.setString("q_instance", "SMSPlugIn");
			lTokenDestroyQuota.setString("q_instance_type", "Application");
			lTokenDestroyQuota.setString("q_namespace", getNamespace());

			Token lToken = mQuotaPlugIn.invoke(aConnector, lTokenDestroyQuota);
			if (lToken.getCode() != -1) {
				return lToken.getLong("value");
			}
		} catch (Exception exp) {
			mLog.error("Error creating the quota to the destroy action");
			mLog.error(exp.getMessage());
		}
		return -1;
	}

	private long querySMSPlugInQuota() {
		try {
			Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");

			Token lTokenQuota = TokenFactory.createToken(mQuotaPlugIn.getNamespace(), "getQuota");
			lTokenQuota.setString("q_identifier", "CountDown");
			lTokenQuota.setString("q_uuid", SMS_QUOTA_ID);

			Token lToken = mQuotaPlugIn.invoke(null, lTokenQuota);
			if (lToken.getCode() != -1) {
				return lToken.getLong("value");
			}
		} catch (Exception exp) {
			mLog.error("Error creating the quota to the destroy action");
			mLog.error(exp.getMessage());
		}
		return -1;
	}

	private void destroyUserQuota(WebSocketConnector aConnector) {
		Assert.notNull(mQuotaPlugIn, "Quota plug-in is not running!");

		Token lTokenDestroyQuota = TokenFactory.createToken(mQuotaPlugIn.getNamespace(), "destroyQuota");
		lTokenDestroyQuota.setString("q_identifier", "CountDown");
		lTokenDestroyQuota.setString("q_instance", aConnector.getUsername());
		lTokenDestroyQuota.setString("q_instance_type", "User");
		lTokenDestroyQuota.setString("q_namespace", getNamespace());
		lTokenDestroyQuota.setString("q_actions", "destroy");

		Token lToken = mQuotaPlugIn.invoke(aConnector, lTokenDestroyQuota);
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
		// This is not working, check with Osvaldo
		// long lTotalQuota = querySMSPlugInQuota();
		Token lRes;
		lRes = createResponse(aToken);

		long lQuota = countDownUserQuota(aConnector);
		if (lQuota != -1) {
			mTotalSMSQuota = countDownSMSPlugInQuota(aConnector);
			if (mTotalSMSQuota != -1) {
				// Calling send action
				lRes = send(aConnector, aToken);
				// Check if the send action was successfull, then decrease the quotas
				if (lRes.getCode() != -1) {
				}
				lRes.setLong("pendingQuota", lQuota);
			} else {
				lRes.setString("msg", "Sorry, there are not more SMS availables for the moment!");
				lRes.setLong("pendingQuota", lQuota);
				lRes.setCode(-1);
			}
		} else {
			// Your quota exceeded, you can't send more sms
			lRes.setString("msg", "Sorry, you have exceeded your quota!");
			lRes.setInteger("pendingQuota", 0);
			lRes.setCode(-1);
//			destroyUserQuota(aConnector);
		}
		lRes.setLong("totalQuota", mTotalUserQuota);
		sendToken(aConnector, aConnector, lRes);
	}
}
