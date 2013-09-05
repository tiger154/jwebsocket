/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota;

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.QuotaProvider;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author osvaldo
 */
public class QuotaPlugin extends ActionPlugIn {

    private static Logger mLog = Logging.getLogger();
    public static final String NS =
            JWebSocketServerConstants.NS_BASE + ".plugins.quota";
    private static ApplicationContext mSpringAppContext;
    private IQuotaProvider mQuotaProvider;
    private QuotaHelper mQuotaHelper;

    @Override
    public String getNamespace() {
        return NS;
    }

    public QuotaPlugin(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        setNamespace(NS);

        if (mLog.isDebugEnabled()) {
            mLog.debug("QuotaPlugin successfully instantiated!");
        }

        mSpringAppContext = getConfigBeanFactory();
        mQuotaProvider = (QuotaProvider) mSpringAppContext.getBean("quotaProv"
                + "ider");
    }

    @Role(name = NS + ".quota_create")
    public void registerQuotaAction(WebSocketConnector aConnector, Token aToken) {

        try {
            boolean lHasUuid = aToken.getMap().containsKey("q_uuid");
            String lUuid = "";
            if (lHasUuid) {
                lUuid = aToken.getString("q_uuid");
            }

            String lNS = aToken.getString("q_namespace");
            String lInstance = aToken.getString("q_instance");
            String lInstanceType = aToken.getString("q_instance_type");
            String lQuotaType = aToken.getString("q_type");

            Token lResult = TokenFactory.createToken(getNamespace(),
                    aToken.getType());
            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);

            if (!lHasUuid) {
                lUuid = QuotaHelper.generateQuotaUUID();
                long lValue = Long.parseLong(aToken.getString("q_value"));
                try {
                    lQuota.register(lInstance, lNS, lUuid, lValue,
                            lInstanceType);
                    lResult.setString("message", "Quota created succesfully");
                    getServer().sendToken(aConnector, lResult);
                } catch (Exception aException) {
                    getServer().sendErrorToken(aConnector, aToken, -1,
                        "Error creating the quota: " + aException.getMessage());
                }
            } else {
                try {
                    lQuota.register(lUuid, lInstance, lInstanceType);
                    lResult.setString("message", "Quota created succesfully");
                    getServer().sendToken(aConnector, lResult);
                } catch (Exception aException) {
                    getServer().sendErrorToken(aConnector, aToken, -1,
                            "Error creating the quota: " + aException.getMessage());
                }
            }
        } catch (Exception aException) {
            mLog.error("Error creating"
                    + "Quota" + aException.getMessage());
        }
    }

    @Role(name = NS + ".quota_remove")
    public void unregisterQuotaAction(WebSocketConnector aConnector, Token aToken) {
        try {

            String lUuid = aToken.getString("q_uuid");
            String lQuotaType = aToken.getString("q_type");
            String lInstance = aToken.getString("q_instance").trim();

            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);

            Token lResult = createResponse(aToken);

            try {
                lQuota.unregister(lInstance, lUuid);
                lResult.setString("message", "Quota removed succesfully");
                getServer().sendToken(aConnector, lResult);
                
            } catch (Exception aException) {
                getServer().sendErrorToken(aConnector, aToken, -1,
                        "Error removing the quota: " + aException.getMessage());
            }
        } catch (Exception aException) {
            mLog.error("Error when unregister the quota"
                    + aException.getMessage());
        }

    }

    @Role(name = NS + ".quota_query")
    public void queryAction(WebSocketConnector aConnector, Token aToken) {
        try {
            Token lResult = createResponse(aToken);
            FastList<Token> lResultList = new FastList<Token>();

            String lNS = aToken.getString("q_namespace").trim();
            String lInstance = aToken.getString("q_instance").trim();
            String lQuotaType = aToken.getString("q_type").trim();

            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);

            List<IQuotaSingleInstance> lQinstanceList = null;

            if (lInstance.equals("") && lNS.equals("")) {
                lQinstanceList = lQuota.getStorage().getQuotas(lQuotaType);
            } else {
                if (!lInstance.equals("") && !lNS.equals("")) {
                    lQinstanceList = lQuota.getStorage().getQuotas(lQuotaType, lNS, lInstance);
                } else {
                    if (!lInstance.equals("")) {
                        lQinstanceList = lQuota.getStorage().getQuotasByInstance(lQuotaType, lInstance);
                    } else {
                        lQinstanceList = lQuota.getStorage().getQuotasByNs(lQuotaType, lNS);
                    }
                }
            }

            for (Iterator<IQuotaSingleInstance> lQuotaIt = lQinstanceList.iterator();
                    lQuotaIt.hasNext();) {

                IQuotaSingleInstance lQuotaSingleInstance = lQuotaIt.next();
                Token lAuxToken = TokenFactory.createToken();

                lQuotaSingleInstance.writeToToken(lAuxToken);
                lResultList.add(lAuxToken);
            }

            lResult.setInteger("totalCount", lQinstanceList.size());
            lResult.setList("data", lResultList);

            getServer().sendToken(aConnector, lResult);

        } catch (Exception aExcep) {
            System.out.println(aExcep.getMessage());
            getServer().sendErrorToken(aConnector, aToken, -1, "The following "
                    + "error was captured in the server: " + aExcep.getMessage());
        }

    }

    @Role(name = NS + ".quota_query")
    public void getQuotaAction(WebSocketConnector aConnector, Token aToken) {
        try {
            Token lResult = createResponse(aToken);
            String lUuid = aToken.getString("q_uuid").trim();
            String lQuotaType = aToken.getString("q_type").trim();
            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);
            long lValue = lQuota.getQuota(lUuid);

            lResult.setLong("value", lValue);
            lResult.setBoolean("success", true);

            getServer().sendToken(aConnector, lResult);

        } catch (Exception aExcep) {
            getServer().sendErrorToken(aConnector, aToken, -1, "The following "
                    + "error was captured in the server: " + aExcep.getMessage());
        }

    }

    @Role(name = NS + ".quota_update")
    public void reduceQuotaAction(WebSocketConnector aConnector, Token aToken) {
        try {
            Token lResult = createResponse(aToken);

            String lUuid = aToken.getString("q_uuid").trim();
            String lQuotaType = aToken.getString("q_type").trim();
            long lReduce = Long.parseLong(aToken.getString("q_value"));

            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);
            long lValue = lQuota.reduceQuota(lUuid, lReduce);
            lResult.setLong("value", lValue);

            getServer().sendToken(aConnector, lResult);

        } catch (Exception aExcep) {
            getServer().sendErrorToken(aConnector, aToken, -1, "The following "
                    + "error was captured in the server: " + aExcep.getMessage());
        }

    }

    @Role(name = NS + ".quota_update")
    public void setQuotaAction(WebSocketConnector aConnector, Token aToken) {
        try {
            Token lResult = createResponse(aToken);

            String lUuid = aToken.getString("q_uuid").trim();
            String lQuotaType = aToken.getString("q_type").trim();
            long lValue = Long.parseLong(aToken.getString("q_value"));

            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);
            lValue = lQuota.setQuota(lUuid, lValue);

            lResult.setLong("value", lValue);

            getServer().sendToken(aConnector, lResult);

        } catch (Exception aExcep) {
            System.out.println(aExcep.getMessage());
            getServer().sendErrorToken(aConnector, aToken, -1, "The following "
                    + "error was captured in the server: " + aExcep.getMessage());
        }

    }

    @Role(name = NS + ".quota_update")
    public void increaseQuotaAction(WebSocketConnector aConnector, Token aToken) {
        try {
            Token lResult = createResponse(aToken);

            String lUuid = aToken.getString("q_uuid").trim();
            String lQuotaType = aToken.getString("q_type").trim();
            long lIncrease = Long.parseLong(aToken.getString("q_value"));

            IQuota lQuota = (IQuota) mQuotaProvider.getQuotaByType(lQuotaType);
            long lValue = lQuota.increaseQuota(lUuid, lIncrease);

            lResult.setLong("value", lValue);
            getServer().sendToken(aConnector, lResult);

        } catch (Exception aExcep) {
            System.out.println(aExcep.getMessage());
            getServer().sendErrorToken(aConnector, aToken, -1, "The following "
                    + "error was captured in the server: " + aExcep.getMessage());
        }

    }
}
