//	---------------------------------------------------------------------------
//	jWebSocket - Paypal Plug-in (Community Edition, CE)
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

import java.util.Collection;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;

/**
 * This plug-in provides all the Paypal functionality.
 *
 * @author Victor Antonio Barzana Crespo
 */
public class PaypalPlugIn extends TokenPlugIn {

    private static final Logger mLog = Logging.getLogger();
    private static final FastMap<String, WebSocketConnector> mClients
            = new FastMap<String, WebSocketConnector>().shared();
//    public static final String NS_CHAT
//            = JWebSocketServerConstants.NS_BASE + ".plugins.paypal";
    private final static String VERSION = "1.0.0";
    private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
    private final static String LABEL = "jWebSocket PaypalPlugIn";
    private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
    private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
    private final static String DESCRIPTION = "jWebSocket PaypalPlugIn - Community Edition";
    private static Setting mPayPalSettings;
    private Paypal facade;

    /**
     * This PlugIn shows how to easily set up a simple jWebSocket based Paypal
     * system.
     *
     * @param aConfiguration
     */
    public PaypalPlugIn(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        if (mLog.isDebugEnabled()) {
            mLog.debug("Instantiating Paypal plug-in...");
        }

        try {
            ApplicationContext mBeanFactory = getConfigBeanFactory();
            mPayPalSettings = (Setting) mBeanFactory.getBean("org.jwebsocket.plugins.paypal.setting");
            facade = new Paypal(mPayPalSettings.getClientID(), mPayPalSettings.getClientSecret());
        } catch (Exception lEx) {
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
            Token lResponseToken = createResponse(aToken);
            lResponseToken.setString("msg", "Received Properly, thanks!");

            getServer().sendToken(aConnector, lResponseToken);

            Token lBroadcastToken = TokenFactory.createToken(getNamespace(), "broadcast");
            lBroadcastToken.setString("msg", "Connector: " + aConnector.getId() + " connected as: " + aConnector.getUsername());
            Collection<WebSocketConnector> lConnectors = getServer().getAllConnectors().values();
            for (WebSocketConnector lConn : lConnectors) {
                if (!lConn.getId().equals(aConnector.getId())) {
                    getServer().sendToken(lConn, lBroadcastToken);
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
}
