//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Services (Community Edition, CE)
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.QuotaProvider;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaServices {

    private static final Logger mLog = Logging.getLogger();
    private final String mNSPluging;
    private static ApplicationContext mSpringAppContext;
    private final IQuotaProvider mQuotaProvider;
    //private LogsManager mLogsManager;

    /**
     *
     * @param mNSPluging
     * @param aSpringAppContent
     */
    public QuotaServices(String mNSPluging, ApplicationContext aSpringAppContent) {
        this.mNSPluging = mNSPluging;

        mSpringAppContext = aSpringAppContent;
        mQuotaProvider = (QuotaProvider) mSpringAppContext.getBean("quotaProvider");
        //mLogsManager = (LogsManager) mSpringAppContext.getBean("LogsManager");
    }

    /**
     *
     * @return
     */
    public String getNamespace() {
        return mNSPluging;
    }

    /**
     * Create one quota
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token createQuotaAction(Token aToken, WebSocketConnector aConnector) {

        Token lResult = TokenFactory.createToken(aToken.getMap());
        String lNS = "";
        String lInstance;
        String lQuotaIdentifier = "";
        try {
            lNS = aToken.getString("namespace");
            lInstance = aToken.getString("instance");
            String lInstanceType = aToken.getString("instance_type");
            lQuotaIdentifier = aToken.getString("identifier");
            String lQuotActions = aToken.getString("actions");
            //This parameter is optional
            String lUuid = null;
            IQuota lQuota = quotaByIdentifier(lQuotaIdentifier);
            if (null != aToken.getString("uuid")) {
                lUuid = aToken.getString("uuid");
            } else {
                try {
                    lUuid = lQuota.getQuotaUuid(lQuotaIdentifier, lNS, lInstance, lInstanceType, lQuotActions);
                } catch (Exception lEx) {
                    // Nothing happens
                }
                if (null == lUuid) {
                    lUuid = QuotaHelper.generateQuotaUUID();
                }
            }

            String lQuotaType = lQuota.getType();
            Integer lValue = aToken.getInteger("value");

            if (lValue == null) {
                lValue = Integer.parseInt(aToken.getString("value"));
            }
            // We now always send to the user the Uuid he tried to create the 
            // quota with so he can reuse it
            lResult.setString("uuid", lUuid);
            lQuota.create(lInstance, lNS, lUuid, lValue,
                    lInstanceType, lQuotaType, lQuotaIdentifier,
                    lQuotActions);
            lResult.setString("message", "Quota created succesfully");
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (ExceptionQuotaAlreadyExist aExpAlreadyExist) {
            mLog.error("Error creating the " + lQuotaIdentifier
                    + " quota, for namespace: " + lNS + ", this quota already exists.");
            return getErrorToken("Error creating the " + lQuotaIdentifier
                    + " quota, for namespace: " + lNS + ", this quota already exists.",
                    lResult, aConnector);
        } catch (Exception aExp) {
            mLog.error("Error creating the " + lQuotaIdentifier
                    + " quota, for namespace: " + lNS);
            return getErrorToken("Error creating the " + lQuotaIdentifier
                    + " quota, for namespace: " + lNS,
                    lResult, aConnector);
        }
    }

    /**
     * Register a new instance to an existent quota
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token registerQuotaAction(Token aToken, WebSocketConnector aConnector) {
        String lInstance = "";
        String lUuid = "";
        String lQuotaIdentifier = "";
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {
            lUuid = aToken.getString("uuid");
            lInstance = aToken.getString("instance");
            String lInstanceType = aToken.getString("instance_type");
            lQuotaIdentifier = aToken.getString("identifier");
            IQuota lQuota = quotaByIdentifier(lQuotaIdentifier);
            lQuota.register(lUuid, lInstance, lInstanceType);
            lResult.setString("message", "Quota register succesfully");
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;
        } catch (ExceptionQuotaAlreadyExist aExpAlreadyExist) {
            mLog.error("Error registering the instance: " + lInstance
                    + " to the " + lQuotaIdentifier + " quota with uuid: "
                    + lUuid + " this instance is already register to "
                    + "this quota");
            return getErrorToken("Error registering the instance: "
                    + lInstance + " to the " + lQuotaIdentifier + " quota with uuid: " + lUuid,
                    aToken, aConnector);
        } catch (Exception aException) {
            mLog.error("Error registering quota for user: "
                    + lInstance + " to the quota with uuid: " + lUuid);
            return getErrorToken("Error registering quota for user: "
                    + lInstance + " to the quota with uuid: " + lUuid,
                    aToken, aConnector);
        }
    }

    private Token getErrorToken(String aMsg, Token aToken, WebSocketConnector aConnector) {

        aToken.setCode(-1);
        aToken.setString("msg", aMsg);
        ////mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), aToken.getCode().toString());
        return aToken;
    }

    private IQuota quotaByIdentifier(String aIdentifier) {
        IQuota lQuota;
        try {
            lQuota = (IQuota) mQuotaProvider.getQuotaByIdentifier(aIdentifier);
        } catch (Exception exp) {
            lQuota = (IQuota) mSpringAppContext.getBean(aIdentifier);
        }
        return lQuota;
    }

    /**
     * Unregister it is used as to delete a quota as to unregister an instance
     * to an existent quota.
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token unregisterQuotaAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {
            String lUuid = aToken.getString("uuid");
            String lQuotaIdentifier = aToken.getString("identifier");
            String lInstance = aToken.getString("instance").trim();
            IQuota lQuota;
            lQuota = quotaByIdentifier(lQuotaIdentifier);
            try {
                lQuota.unregister(lInstance, lUuid);
                lResult.setString("message", "Quota removed succesfully");
                lResult.setCode(0);
                //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
                return lResult;
            } catch (Exception aException) {
                return getErrorToken("Error removing the quota "
                        + lQuotaIdentifier + " for instance: " + lInstance,
                        aToken, aConnector);
            }
        } catch (Exception aException) {
            mLog.error("Error unregistering the quota with uuid:"
                    + aException.getMessage());
            return getErrorToken("Error removing the quota, "
                    + aException.getMessage(), aToken, aConnector);
        }

    }

    /**
     * Private method to add registered quota to a parent quota.
     *
     * @param aRefList
     * @param aPartialList
     */
    private void addAllQSIList(FastList<IQuotaSingleInstance> aRefList,
            FastList<IQuotaSingleInstance> aPartialList) {

        for (Iterator<IQuotaSingleInstance> it = aPartialList.iterator(); it.hasNext();) {
            IQuotaSingleInstance iQuotaSingleInstance = it.next();
            aRefList.add(iQuotaSingleInstance);
        }
    }

    /**
     * Allow make query over quota by severals attributes.
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token queryAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {
            FastList<Token> lResultList = new FastList<Token>();

            String lNS = aToken.getString("namespace").trim();
            String lInstance = aToken.getString("instance").trim();
            String lQuotaType = aToken.getString("quotaType").trim();
            String lQuotaIdentifier = aToken.getString("identifier", null);
            FastList<IQuotaSingleInstance> lQinstanceList = new FastList<IQuotaSingleInstance>();

            if (lQuotaIdentifier != null) {
                IQuota lQuota;
                lQuota = quotaByIdentifier(lQuotaIdentifier);
                addAllQSIList(lQinstanceList, (FastList<IQuotaSingleInstance>) lQuota.getStorage().getQuotasByIdentifier(lQuotaIdentifier));
            } else {
                Map<String, IQuotaStorage> lActiveStorage = mQuotaProvider.getActiveStorages();
                for (Map.Entry<String, IQuotaStorage> entry : lActiveStorage.entrySet()) {
                    String lKey = entry.getKey();
                    IQuotaStorage lQuotaStorage = entry.getValue();
                    if (lInstance.equals("") && lNS.equals("")) {
                        lQinstanceList.addAll((FastList<IQuotaSingleInstance>) lQuotaStorage.getQuotas(lQuotaType));
                    } else {
                        if (!lInstance.equals("") && !lNS.equals("")) {
                            lQinstanceList.addAll((List<IQuotaSingleInstance>) lQuotaStorage.getQuotas(lQuotaType, lNS, lInstance));
                        } else {
                            if (!lInstance.equals("")) {
                                lQinstanceList.addAll((List<IQuotaSingleInstance>) lQuotaStorage.getQuotasByInstance(lQuotaType, lInstance));
                            } else {
                                lQinstanceList.addAll((List<IQuotaSingleInstance>) lQuotaStorage.getQuotasByNs(lQuotaType, lNS));
                            }
                        }
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
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (Exception aExcep) {
            return getErrorToken("The following "
                    + "error was captured in the server, " + aExcep.getMessage(), aToken, aConnector);
        }

    }

    /**
     * Get a quota object
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token getQuotaAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {

            String lQuotaIdentifier = aToken.getString("identifier").trim();
            String lNS = aToken.getString("namespace");
            String lInstance = aToken.getString("instance");
            String lInstanceType = aToken.getString("instance_type");
            String lActions = aToken.getString("actions");

            IQuota lQuota;
            lQuota = quotaByIdentifier(lQuotaIdentifier);
            String lUuid = aToken.getString("uuid");

            IQuotaSingleInstance lQuotaSingleInstance;

            if ((null != lNS || !lUuid.equals("")) && (null != lInstance || !lInstance.equals(""))
                    && (null != lInstanceType || !lInstanceType.equals(""))
                    && (null != lActions || !lActions.equals(""))) {

                lQuotaSingleInstance = lQuota.getQuota(lInstance, lNS, lInstanceType, lActions);

            } else {
                lQuotaSingleInstance = lQuota.getQuota(lUuid);
            }
            Token lAuxToken = TokenFactory.createToken();

            if (lQuotaSingleInstance == null) {

                String lMessage = "There is not a quota with the identifier ";
                // + lQuotaIdentifier + " for the instance " + lInstance;

                lResult.setCode(-1);
                lResult.setString("msg", lMessage);

                //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
                return lResult;
            }

            lQuotaSingleInstance.writeToToken(lAuxToken);

            lResult.setLong("value", lQuotaSingleInstance.getvalue());
            lResult.setString("uuid", lQuotaSingleInstance.getUuid());
            lResult.setToken("quota", lAuxToken);

            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (Exception aExcep) {
            return getErrorToken("The following "
                    + "error was captured in the server, " + aExcep.getMessage(), aToken, aConnector);
        }
    }

    /**
     * Get all active quotas
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token getActivesQuotaAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {
            FastList<Token> lResultList = new FastList<Token>();

            Object[] lActivesQuota = mQuotaProvider.getActiveQuotas().keySet().toArray();
            for (int i = 0; i < lActivesQuota.length; i++) {
                String lIdentifier = lActivesQuota[i].toString();
                Token lToken = TokenFactory.createToken();
                lToken.setString("name", lIdentifier);
                lResultList.add(lToken);
            }

            lResult.setList("data", lResultList);
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (Exception aExcep) {
            return getErrorToken("The following "
                    + "error was captured in the server, " + aExcep.getMessage(), aToken, aConnector);
        }

    }

    /**
     * Reduce the value to one quota.
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token reduceQuotaAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {
            String lQuotaIdentifier = aToken.getString("identifier").trim();

            Integer lReduce = aToken.getInteger("value");

            if (lReduce == null) {
                lReduce = Integer.parseInt(aToken.getString("value"));
            }

            String lNS = aToken.getString("namespace");
            String lInstance = aToken.getString("instance");
            String lInstanceType = aToken.getString("instance_type");
            String lActions = aToken.getString("actions");
            String lUuid = aToken.getString("uuid", null);

            IQuota lQuota;
            lQuota = quotaByIdentifier(lQuotaIdentifier);
            long lValue;

            //reduce value by uuid and instance
            if (null != lUuid && null != lInstance) {

                IQuotaSingleInstance lSingleInstance = lQuota.getQuota(lUuid, lInstance);

                lValue = lQuota.reduceQuota(lSingleInstance.getInstance(),
                        lSingleInstance.getNamespace(),
                        lSingleInstance.getInstanceType(),
                        lSingleInstance.getActions(), lReduce);

                //reduce value searching for all quota parameters    
            } else if ((null != lNS || !lNS.equals("")) && (null != lInstance
                    || !lInstance.equals(""))
                    && (null != lInstanceType || !lInstanceType.equals(""))
                    && (null != lActions || !lActions.equals(""))) {

                lValue = lQuota.reduceQuota(lInstance, lNS, lInstanceType,
                        lActions, lReduce);

            } else {
                //reduce value for the parent quota
                lValue = lQuota.reduceQuota(lUuid, lReduce);
            }

            if (lValue == -1) {
                return getErrorToken("Acces not allowed due to quota limitation exceed", aToken, aConnector);
            }

            lResult.setLong("value", lValue);
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (Exception aExcep) {
            return getErrorToken("The following "
                    + "error was captured in the server, " + aExcep.getMessage(), aToken, aConnector);
        }

    }

    /**
     * Set the value to one quota.
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token setQuotaAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {

            String lQuotaIdentifier = aToken.getString("identifier");

            Integer lSetValue = aToken.getInteger("value");

            if (lSetValue == null) {
                lSetValue = Integer.parseInt(aToken.getString("value"));
            }

            String lNS = aToken.getString("namespace");
            String lInstance = aToken.getString("instance");
            String lInstanceType = aToken.getString("instance_type");
            String lActions = aToken.getString("actions");
            String lUuid = aToken.getString("uuid", null);

            IQuota lQuota;
            lQuota = quotaByIdentifier(lQuotaIdentifier);
            long lValue;

            //set value by uuid and instance
            if (null != lUuid && null != lInstance) {

                IQuotaSingleInstance lSingleInstance = lQuota.getQuota(lUuid, lInstance);

                lValue = lQuota.setQuota(lSingleInstance.getInstance(),
                        lSingleInstance.getNamespace(),
                        lSingleInstance.getInstanceType(),
                        lSingleInstance.getActions(), lSetValue);

                //set value for all quota parameters    
            } else if ((null != lNS || !lNS.equals("")) && (null != lInstance
                    || !lInstance.equals(""))
                    && (null != lInstanceType || !lInstanceType.equals(""))
                    && (null != lActions || !lActions.equals(""))) {

                lValue = lQuota.setQuota(lInstance, lNS, lInstanceType,
                        lActions, lSetValue);

            } else {
                //set value for the parent quota
                lValue = lQuota.setQuota(lUuid, lSetValue);
            }
            lResult.setLong("value", lValue);
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (Exception aExcep) {
            return getErrorToken("The following "
                    + "error was captured in the server, " + aExcep.getMessage(), aToken, aConnector);
        }

    }

    /**
     * Increase de quota value.
     *
     * @param aToken
     * @param aConnector
     * @return
     */
    public Token increaseQuotaAction(Token aToken, WebSocketConnector aConnector) {
        Token lResult = TokenFactory.createToken(aToken.getMap());
        try {
            String lQuotaIdentifier = aToken.getString("identifier").trim();
            Integer lIncrease = aToken.getInteger("value");

            if (lIncrease == null) {
                lIncrease = Integer.parseInt(aToken.getString("value"));
            }
            String lNS = aToken.getString("namespace");
            String lInstance = aToken.getString("instance");
            String lInstanceType = aToken.getString("instance_type");
            String lActions = aToken.getString("actions");
            String lUuid = aToken.getString("uuid", null);

            IQuota lQuota;
            lQuota = quotaByIdentifier(lQuotaIdentifier);
            long lValue;

            //increase value by uuid and instance
            if (null != lUuid && null != lInstance) {

                IQuotaSingleInstance lSingleInstance = lQuota.getQuota(lUuid, lInstance);

                lValue = lQuota.increaseQuota(lSingleInstance.getInstance(),
                        lSingleInstance.getNamespace(),
                        lSingleInstance.getInstanceType(),
                        lSingleInstance.getActions(), lIncrease);

                //increase value searching for all quota parameters    
            } else if ((null != lNS || !lNS.equals("")) && (null != lInstance
                    || !lInstance.equals(""))
                    && (null != lInstanceType || !lInstanceType.equals(""))
                    && (null != lActions || !lActions.equals(""))) {

                lValue = lQuota.increaseQuota(lInstance, lNS, lInstanceType,
                        lActions, lIncrease);

            } else {
                //increase value for the parent quota
                lValue = lQuota.increaseQuota(lUuid, lIncrease);
            }
            lResult.setLong("value", lValue);
            lResult.setCode(0);
            //mLogsManager.Save(getConnectorUser(aConnector), aToken.getNS(), aToken.getType(), String.valueOf(lResult.getCode()));
            return lResult;

        } catch (Exception aExcep) {
            return getErrorToken("The following "
                    + "error was captured in the server, " + aExcep.getMessage(), aToken, aConnector);
        }

    }

    private String getConnectorUser(WebSocketConnector aConnector) {
        if (null == aConnector) {
            return "invoke";
        }
        return aConnector.getUsername();
    }
}
