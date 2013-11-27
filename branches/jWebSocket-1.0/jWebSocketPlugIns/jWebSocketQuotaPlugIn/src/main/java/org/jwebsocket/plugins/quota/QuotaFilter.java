/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.QuotaProvider;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;

/**
 *
 * @author osvaldo
 */
public class QuotaFilter extends TokenFilter {

    private static Logger mLog = Logging.getLogger();
    private IQuotaProvider mQuotaProvider;

    public QuotaFilter(FilterConfiguration configuration) {
        super(configuration);

        mQuotaProvider = (QuotaProvider) JWebSocketBeanFactory.getInstance().getBean("quotaProvider");

        if (mLog.isInfoEnabled()) {
            mLog.info("Filter successfully instantiated.");
        }
    }

    @Override
    public void processTokenIn(FilterResponse aResponse, WebSocketConnector aConnector, Token aToken) {
        super.processTokenIn(aResponse, aConnector, aToken); //To change body of generated methods, choose Tools | Templates.

        String lNS = aToken.getNS();
        String lType = aToken.getType();
        String lUserName = aConnector.getUsername();

        //Only proccess when the token not come for the SystemPlugin
        if (!SystemPlugIn.NS_SYSTEM.equals(lNS) || !isIgnoredUser(lUserName)) {
            Map<String, IQuota> lQuotas = mQuotaProvider.getActiveQuotas();

            for (Map.Entry<String, IQuota> entry : lQuotas.entrySet()) {
                //The same of lQuotaObj.getIdentifier();
                String lIdentifier = entry.getKey();
                IQuota lQuotaObj = entry.getValue();

                /**
                 * This method get a quota for this NamesPace as well as exist a
                 * quota for this user itself, as if he has a quota as part of a
                 * group
                 *
                 */
                IQuotaSingleInstance lQSingle = lQuotaObj.getQuota(lUserName, lNS, "User");

                // if lQSingle is null, there is not a quota for this user 
                if (lQSingle == null) {
                    continue;
                }
                String lActions = lQSingle.getActions();
                
                //if the actual token or action is not limited by the quota pass to the other quotaType
                if (!lActions.equals("*")) {
                    if (lActions.indexOf(lType) == -1) {
                        continue;
                    }
                }

                long lQValue = lQuotaObj.reduceQuota(lUserName, lNS, "User", lQuotaObj.getDefaultReduceValue());

                if (lQValue == -1) {
                    Token lResponse = getServer().createResponse(aToken);
                    lResponse.setCode(-1);
                    lResponse.setString("msg", "Acces not allowed due to quota limmitation exceed");
                    getServer().sendToken(aConnector, lResponse);
                    aResponse.rejectMessage();

                    if (mLog.isDebugEnabled()) {
                        mLog.debug("Quota(" + lQuotaObj.getType() + ") limit exceeded for user: "
                                + lUserName + ", on namespace:" + lNS + ". Access not allowed!");
                    }

                }
            }
        }
    }

    private boolean isIgnoredUser(String aUser) {
        if (QuotaHelper.ignoredUsers().indexOf(aUser) != -1) {
            return true;
        }
        return false;
    }
}
