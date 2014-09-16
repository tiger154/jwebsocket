//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket PayPal Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.paypal;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentHistory;
import com.paypal.api.payments.Refund;
import com.paypal.api.payments.Sale;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * This plug-in provides all the PayPal functionality.
 *
 * @author Omar Antonio Díaz Peña
 */
public class PayPalPlugIn extends TokenPlugIn {

    private static final Logger mLog = Logging.getLogger();
    private static final FastMap<String, WebSocketConnector> mClients
            = new FastMap<String, WebSocketConnector>().shared();
    private final static String VERSION = "1.0.0";
    private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
    private final static String LABEL = "jWebSocket PaypalPlugIn";
    private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
    private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
    private final static String DESCRIPTION = "jWebSocket PaypalPlugIn - Community Edition";
    private final static String TT_PP_PAYMENT = "paypal_payment";
    private final static String TT_PP_EXECUTE = "paypal_execute";
    private final static String TT_PP_LIST = "paypal_list";
    private final static String TT_PP_GET = "paypal_get";
    private final static String TT_PP_SALE = "paypal_sale";
    private final static String TT_PP_REFUND = "paypal_refund";
    private static Settings mPayPalSettings;
    private PayPalFacade mFacade;

    /**
     * This PlugIn shows how to easily set up a simple jWebSocket based Paypal
     * system.
     *
     * @param aConfiguration
     */
    public PayPalPlugIn(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        if (mLog.isDebugEnabled()) {
            mLog.debug("Instantiating Paypal plug-in...");
        }

        try {
            ApplicationContext mBeanFactory = getConfigBeanFactory();
            mPayPalSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.paypal.setting");
            mFacade = new PayPalFacade(mPayPalSettings.getClientID(), mPayPalSettings.getClientSecret());
        } catch (BeansException lEx) {
            mLog.error("Failed to instantiate Paypal plug-in " + lEx.getLocalizedMessage());
        }

        // org.jwebsocket.plugins.paypal
        this.setNamespace(aConfiguration.getNamespace());
        if (mLog.isInfoEnabled()) {
            mLog.info("Paypal plug-in successfully instantiated.");
        }
    }

    @Override
    public void processToken(PlugInResponse aResponse,
            WebSocketConnector aConnector, Token aToken) {
        if (getNamespace().equals(aToken.getNS())) {
            if (TT_PP_PAYMENT.equals(aToken.getType())) {
                try {
                    createPayment(aConnector, aToken);
                } catch (Exception lEx) {
                    mLog.error(lEx);
                }
            } else if (TT_PP_EXECUTE.equals(aToken.getType())) {
                try {
                    executePayment(aConnector, aToken);
                } catch (Exception lEx) {
                    mLog.error(lEx);
                }
            } else if (TT_PP_LIST.equals(aToken.getType())) {
                try {
                    listPayments(aConnector, aToken);
                } catch (Exception lEx) {
                    mLog.error(lEx);
                }
            } else if (TT_PP_GET.equals(aToken.getType())) {
                try {
                    getPayment(aConnector, aToken);
                } catch (Exception lEx) {
                    mLog.error(lEx);
                }
            } else if (TT_PP_SALE.equals(aToken.getType())) {
                try {
                    getSale(aConnector, aToken);
                } catch (Exception lEx) {
                    mLog.error(lEx);
                }
            } else if (TT_PP_REFUND.equals(aToken.getType())) {
                try {
                    refundSale(aConnector, aToken);
                } catch (Exception lEx) {
                    mLog.error(lEx);
                }
            }
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
    
    /**
     * Clearing the JSON 
     * 
     * @param aJSON
     * @return String 
     */
    public static String clearJSON(String aJSON){
        return aJSON.replaceAll("(\\r|\\n)", "");
    } 

    /**
     * Creates a payment
     *
     * @param aConnector
     * @param aToken
     * @throws Exception
     */
    public void createPayment(WebSocketConnector aConnector, Token aToken) throws Exception {
        if (! hasAuthority(aConnector, getNamespace() + ".buy")){
            sendToken(aConnector, createAccessDenied(aToken));
            return;
        }
        
        
        aToken.setString("return_url", mPayPalSettings.getReturnUrl());
        aToken.setString("cancel_url", mPayPalSettings.getCancelUrl());
        
        Payment lResult = mFacade.createPayment(aToken);
        Token lRespToken = createResponse(aToken);

        lRespToken.setString("data", clearJSON(lResult.toJSON()));
        sendToken(aConnector, lRespToken);
    }

    /**
     * Execute the payment
     *
     * @param aConnector
     * @param aToken
     * @throws Exception
     */
    public void executePayment(WebSocketConnector aConnector, Token aToken) throws Exception {
        if (! hasAuthority(aConnector, getNamespace() + ".buy")){
            sendToken(aConnector, createAccessDenied(aToken));
            return;
        }
        
        Payment lResult = mFacade.executePayment(aToken);
        Token lRespToken = createResponse(aToken);

        lRespToken.setString("data", clearJSON(lResult.toJSON()));
        sendToken(aConnector, lRespToken);
    }

    /**
     * List payments
     *
     * @param aConnector
     * @param aToken
     * @throws Exception
     */
    public void listPayments(WebSocketConnector aConnector, Token aToken) throws Exception {
        if (! hasAuthority(aConnector, getNamespace() + ".check")){
            sendToken(aConnector, createAccessDenied(aToken));
            return;
        }
        
        PaymentHistory lResult = mFacade.listPayments(aConnector, aToken);

        Token lRespToken = createResponse(aToken);

        lRespToken.setString("data", clearJSON(lResult.toJSON()));
        sendToken(aConnector, lRespToken);
    }

    /**
     * Get a payment
     *
     * @param aConnector
     * @param aToken
     * @throws Exception
     */
    public void getPayment(WebSocketConnector aConnector, Token aToken) throws Exception {
        if (! hasAuthority(aConnector, getNamespace() + ".check")){
            sendToken(aConnector, createAccessDenied(aToken));
            return;
        }
        
        Payment lResult = mFacade.getPayment(aConnector, aToken);

        Token lRespToken = createResponse(aToken);

        lRespToken.setString("data", clearJSON(lResult.toJSON()));
        sendToken(aConnector, lRespToken);
    }

    /**
     * Get a sale
     *
     * @param aConnector
     * @param aToken
     * @throws Exception
     */
    public void getSale(WebSocketConnector aConnector, Token aToken) throws Exception {
        if (! hasAuthority(aConnector, getNamespace() + ".check")){
            sendToken(aConnector, createAccessDenied(aToken));
            return;
        }
        
        Sale lResult = mFacade.getSale(aConnector, aToken);

        Token lRespToken = createResponse(aToken);

        lRespToken.setString("data", clearJSON(lResult.toJSON()));
        sendToken(aConnector, lRespToken);
    }

    /**
     * Refund a sale
     *
     * @param aConnector
     * @param aToken
     * @throws Exception
     */
    public void refundSale(WebSocketConnector aConnector, Token aToken) throws Exception {
        if (! hasAuthority(aConnector, getNamespace() + ".check")){
            sendToken(aConnector, createAccessDenied(aToken));
            return;
        }
        
        Refund lResult = mFacade.getRefundSale(aConnector, aToken);

        Token lRespToken = createResponse(aToken);

        lRespToken.setString("data", clearJSON(lResult.toJSON()));
        sendToken(aConnector, lRespToken);
    }
}
