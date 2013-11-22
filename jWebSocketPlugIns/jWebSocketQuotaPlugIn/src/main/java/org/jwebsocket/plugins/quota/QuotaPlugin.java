/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
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
    public static final String NS = JWebSocketServerConstants.NS_BASE + ".plugins.quota";
    private static ApplicationContext mSpringAppContext;
    private IQuotaProvider mQuotaProvider;
    private QuotaHelper mQuotaHelper;
    private QuotaServices mQuotaService;

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

        mQuotaService = new QuotaServices(getNamespace(), mSpringAppContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token invoke(WebSocketConnector aConnector, Token aToken) {
        String lType = aToken.getType();

        if (lType.equals("registerQuota")) {
            return mQuotaService.registerQuotaAction(aToken);
        } else if (lType.equals("unregisterQuota")) {
            return mQuotaService.unregisterQuotaAction(aToken);
        } else if (lType.equals("query")) {
            return mQuotaService.queryAction(aToken);
        } else if (lType.equals("getQuota")) {
            return mQuotaService.getQuotaAction(aToken);
        } else if (lType.equals("getActivesQuota")) {
            return mQuotaService.getActivesQuotaAction(aToken);
        } else if (lType.equals("reduceQuota")) {
            return mQuotaService.reduceQuotaAction(aToken);
        } else if (lType.equals("setQuota")) {
            return mQuotaService.setQuotaAction(aToken);
        } else if (lType.equals("increaseQuota")) {
            return mQuotaService.increaseQuotaAction(aToken);
        }
        return null;
    }

    @Role(name = NS + ".quota_create")
    public void registerQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = mQuotaService.registerQuotaAction(aToken);
        getServer().sendToken(aConnector, lResult);
    }

    @Role(name = NS + ".quota_remove")
    public void unregisterQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = mQuotaService.unregisterQuotaAction(aToken);
        getServer().sendToken(aConnector, lResult);

    }

    @Role(name = NS + ".quota_query")
    public void queryAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.queryAction(aToken);
        lResult.setInteger("totalCount", lToken.getInteger("totalCount"));
        lResult.setList("data", lToken.getList("data"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    @Role(name = NS + ".quota_query")
    public void getQuotaAction(WebSocketConnector aConnector, Token aToken) {

        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.getQuotaAction(aToken);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setBoolean("success", lToken.getBoolean("success"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    @Role(name = NS + ".quota_query")
    public void getActivesQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.getActivesQuotaAction(aToken);
        lResult.setList("data", lToken.getList("data"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    @Role(name = NS + ".quota_update")
    public void reduceQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.reduceQuotaAction(aToken);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    @Role(name = NS + ".quota_update")
    public void setQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.setQuotaAction(aToken);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }

    @Role(name = NS + ".quota_update")
    public void increaseQuotaAction(WebSocketConnector aConnector, Token aToken) {
        Token lResult = createResponse(aToken);
        Token lToken = mQuotaService.increaseQuotaAction(aToken);
        lResult.setLong("value", lToken.getLong("value"));
        lResult.setCode(lToken.getCode());
        getServer().sendToken(aConnector, lResult);
    }
}
