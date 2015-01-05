//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.quota;

import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.utils.QuotaProvider;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JMSManager;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaPlugIn extends ActionPlugIn {

    private static final Logger mLog = Logging.getLogger();
    private static ApplicationContext mSpringAppContext;
    private IQuotaProvider mQuotaProvider;
    private QuotaServices mQuotaService;
    private JMSManager mMessageHub;

	/**
	 *
	 */
	public static final String NS = JWebSocketServerConstants.NS_BASE + ".plugins.quota";
    private final static String VERSION = "1.0.0";
    private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
    private final static String LABEL = "jWebSocket QuotaPlugIn";
    private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
    private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
    private final static String DESCRIPTION = "jWebSocket QuotaPlugIn - Community Edition";

    @Override
    public String getNamespace() {
        return NS;
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

    /**
     * Constructor
     * 
     * @param aConfiguration
     */
    public QuotaPlugIn(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        setNamespace(NS);

        if (mLog.isDebugEnabled()) {
            mLog.debug("QuotaPlugin successfully instantiated!");
        }

        mSpringAppContext = getConfigBeanFactory();
    }

    /**
     * Creating the listener to get the tokens or packet successfully 
     * processed using JMSManager
     * 
     * @throws Exception 
     */
    @Override
    public void systemStarted() throws Exception {
        super.systemStarted();

        mQuotaProvider = (QuotaProvider) mSpringAppContext.getBean("quotaProv"
                + "ider");
        mQuotaService = new QuotaServices(NS, mSpringAppContext);

        //listening the sent message
        try {
            // The initialization of the messageHub must be in a try because 
            // getJMSManager throws exception
            mMessageHub = getServer().getJMSManager();
            mMessageHub.subscribe(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    MapMessage lMessage = (MapMessage) msg;

                    try {
                        String lUsername = lMessage.getStringProperty("username");
                        String lNS = lMessage.getStringProperty("tokenNS");
                        String lType = lMessage.getStringProperty("tokenType");
                        int lCode = lMessage.getIntProperty("code");

                        //exit if and error ocours precessing the action
                        if (lCode == -1) {
                            return;
                        }

                        Map<String, IQuota> lQuotas = mQuotaProvider.getActiveQuotas();

                        for (Map.Entry<String, IQuota> entry : lQuotas.entrySet()) {
                            //The same of lQuotaObj.getIdentifier();
                            String lIdentifier = entry.getKey();
                            IQuota lQuotaObj = entry.getValue();


                            /* This method get a quota for this NamesPace as
                             * well as exist a quota for this user itself,
                             * as if he has a quota as part of a group*/
                            List<IQuotaSingleInstance> lQuotaList = lQuotaObj.getQuotas(lUsername, lNS, "User");

                            for (IQuotaSingleInstance lQSingle : lQuotaList) {

                                // if lQSingle is null, there is not a quota for this user 
                                if (lQSingle == null) {
                                    continue;
                                }
                                String lActions = lQSingle.getActions();

                                //if the actual token or action is not limited by the quota pass to the other quotaType
                                if (!lActions.equals("*")) {
                                    if (-1 == lActions.indexOf(lType)) {
                                        continue;
                                    }
                                }

                                long lQValue = lQuotaObj.reduceQuota(lQSingle.getInstance(),
                                        lQSingle.getNamespace(), lQSingle.getInstanceType(),
                                        lQSingle.getActions(),
                                        lQuotaObj.getDefaultReduceValue());
                            }

                        }
                    } catch (JMSException lEx) {
                        mLog.error(Logging.getSimpleExceptionMessage(lEx,
                                "Error processing post-execution quota checks"));
                    }
                }
            }, "ns = 'org.jwebsocket.plugins' AND msgType='tokenProcessed'");

        } catch (Exception lEx) {
            mLog.error(Logging.getSimpleExceptionMessage(lEx, "Error getting JMSManager, please check that"
                    + "JMSManager has been successfully loaded"));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return Token
     */
    @Override
    public Token invoke(WebSocketConnector aConnector, Token aToken) {
        String lType = aToken.getType();
        if (lType.equals("createQuota")) {
            return mQuotaService.createQuotaAction(aToken,aConnector);
        } else if (lType.equals("registerQuota")) {
            return mQuotaService.registerQuotaAction(aToken,aConnector);
        } else if (lType.equals("unregisterQuota")) {
            return mQuotaService.unregisterQuotaAction(aToken,aConnector);
        } else if (lType.equals("query")) {
            return mQuotaService.queryAction(aToken,aConnector);
        } else if (lType.equals("getQuota")) {
            return mQuotaService.getQuotaAction(aToken,aConnector);
        } else if (lType.equals("getActivesQuota")) {
            return mQuotaService.getActivesQuotaAction(aToken,aConnector);
        } else if (lType.equals("reduceQuota")) {
            return mQuotaService.reduceQuotaAction(aToken,aConnector);
        } else if (lType.equals("setQuota")) {
            return mQuotaService.setQuotaAction(aToken,aConnector);
        } else if (lType.equals("increaseQuota")) {
            return mQuotaService.increaseQuotaAction(aToken,aConnector);
        }
        return null;
    }

    /**
     *Create one quota
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_create")
    public void createQuotaAction(WebSocketConnector aConnector, Token aToken) {
        
        Token lResult = mQuotaService.createQuotaAction(aToken , aConnector);
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Register a new instance to an existent quota
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_create")
    public void registerQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = mQuotaService.registerQuotaAction(aToken,aConnector);
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Unregister it is used as to delete a quota as to unregister an instance 
     * to an existent quota. 
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_remove")
    public void unregisterQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = mQuotaService.unregisterQuotaAction(aToken,aConnector);
        getServer().sendToken(aConnector, lResult);

    }

    /**
     * Allow make query over quota by severals attributes. 
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_query")
    public void queryAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.queryAction(aToken,aConnector);
        lResult.setInteger("totalCount", lToken.getInteger("totalCount"));
        lResult.setList("data", lToken.getList("data"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Get a quota object
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_query")
    public void getQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = mQuotaService.getQuotaAction(aToken,aConnector);
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Get all active quotas
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_query")
    public void getActivesQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.getActivesQuotaAction(aToken,aConnector);
        lResult.setList("data", lToken.getList("data"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Reduce the value to one quota.
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_update")
    public void reduceQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.reduceQuotaAction(aToken,aConnector);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Set the value to one quota.
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_update")
    public void setQuotaAction(WebSocketConnector aConnector, Token aToken) {

        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.setQuotaAction(aToken,aConnector);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    /**
     * Increase de quota value.
     * 
     * @param aConnector
     * @param aToken
     */
    @Role(name = NS + ".quota_update")
    public void increaseQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.increaseQuotaAction(aToken,aConnector);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }
}
