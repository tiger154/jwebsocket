//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket ItemStorage Quota  (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.storage;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaChildSI;
import org.jwebsocket.plugins.itemstorage.ItemStoragePlugIn;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollectionProvider;
import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollectionUtils;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.util.MapAppender;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class ItemStorageQuota implements IQuotaStorage {

    private String mCollectionQuotaName;
    private String mCollectionQuotaInstanceName;
    private IItemDefinition mQuotaDefinition;
    private IItemDefinition mquotaInstanceDefinition;
    private String mCollectionAccessPassword;
    private String mCollectionSecretPassword;
    private String mRootUser;
    private IItemCollection mCollectionQuota = null;
    private IItemCollection mCollectionQuotaInstance = null;
    private static final Logger mLog = Logging.getLogger();

    /**
     *
     * @param mCollectionQuotaName
     */
    public void setCollectionQuotaName(String mCollectionQuotaName) {
        this.mCollectionQuotaName = mCollectionQuotaName;
    }

    /**
     *
     * @param mRootUser
     */
    public void setRootUser(String mRootUser) {
        this.mRootUser = mRootUser;
    }

    /**
     *
     * @param mCollectionQuotaInstanceName
     */
    public void setCollectionQuotaInstanceName(String mCollectionQuotaInstanceName) {
        this.mCollectionQuotaInstanceName = mCollectionQuotaInstanceName;
    }

    /**
     *
     * @param mQuotaDefinition
     */
    public void setQuotaDefinition(IItemDefinition mQuotaDefinition) {
        this.mQuotaDefinition = mQuotaDefinition;
    }

    /**
     *
     * @param mquotaInstanceDefinition
     */
    public void setquotaInstanceDefinition(IItemDefinition mquotaInstanceDefinition) {
        this.mquotaInstanceDefinition = mquotaInstanceDefinition;
    }

    /**
     *
     * @param mCollectionAccessPassword
     */
    public void setCollectionAccessPassword(String mCollectionAccessPassword) {
        this.mCollectionAccessPassword = mCollectionAccessPassword;
    }

    /**
     *
     * @param mCollectionSecretPassword
     */
    public void setCollectionSecretPassword(String mCollectionSecretPassword) {
        this.mCollectionSecretPassword = mCollectionSecretPassword;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    public void initialize() throws Exception {

        // getting the collection provider
        IItemCollectionProvider lCollectionProvider = (IItemCollectionProvider) JWebSocketBeanFactory
                .getInstance(ItemStoragePlugIn.NS_ITEM_STORAGE).getBean("collectionProvider");
        // getting the item definitions factory
        IItemFactory lItemFactory = lCollectionProvider.getItemStorageProvider().getItemFactory();
        // checking if quota collection already exists
        if (!lCollectionProvider.collectionExists(mCollectionQuotaName)) {
            // check if definition already exists
            if (!lItemFactory.supportsType(mQuotaDefinition.getType())) {
                // creating definition
                lItemFactory.registerDefinition(mQuotaDefinition);
            }
            // creating the collection
            IItemCollection lQuotaCollection = null;
            try {
                lQuotaCollection = lCollectionProvider
                        .getCollection(mCollectionQuotaName, mQuotaDefinition.getType());
            } catch (Exception aExp) {
                mLog.error("Error getting the " + mCollectionQuotaName + " collection "
                        + "the server return the following error:" + aExp.getMessage());
            }

            lQuotaCollection.setAccessPassword(Tools.getMD5(mCollectionAccessPassword));
            lQuotaCollection.setSecretPassword(Tools.getMD5(mCollectionSecretPassword));
            lQuotaCollection.setOwner(mRootUser);
            // saving changes
            lCollectionProvider.saveCollection(lQuotaCollection);
        }

        // checking if quotaInstance collection already exists
        if (!lCollectionProvider.collectionExists(mCollectionQuotaInstanceName)) {
            // check if definition already exists
            if (!lItemFactory.supportsType(mquotaInstanceDefinition.getType())) {
                // creating definition
                lItemFactory.registerDefinition(mquotaInstanceDefinition);
            }
            // creating the collection
            IItemCollection lQuotaInstCollection = null;
            try {
                lQuotaInstCollection = lCollectionProvider
                        .getCollection(mCollectionQuotaInstanceName,
                                mquotaInstanceDefinition.getType());
            } catch (Exception aExp) {
                mLog.error("Error getting the " + mCollectionQuotaInstanceName + " collection "
                        + "the server return the following error:" + aExp.getMessage());
            }

            lQuotaInstCollection.setAccessPassword(Tools.getMD5(mCollectionAccessPassword));
            lQuotaInstCollection.setSecretPassword(Tools.getMD5(mCollectionSecretPassword));
            lQuotaInstCollection.setOwner(mRootUser);
            // saving changes
            lCollectionProvider.saveCollection(lQuotaInstCollection);
        }

        mCollectionQuota
                = lCollectionProvider.getCollection(mCollectionQuotaName);
        mCollectionQuotaInstance
                = lCollectionProvider.getCollection(mCollectionQuotaInstanceName);
    }

    /**
     *
     * @param aQuota
     * @return
     */
    @Override
    public boolean save(IQuotaSingleInstance aQuota) {
        try {
            ItemCollectionUtils.saveItem(mRootUser, mCollectionQuota,
                    new MapAppender()
                    .append("uuid", aQuota.getUuid())
                    .append("ns", aQuota.getNamespace())
                    .append("quotaType", aQuota.getQuotaType())
                    .append("quotaIdentifier", aQuota.getQuotaIdentifier())
                    .append("value", aQuota.getvalue())
                    .append("actions", aQuota.getActions())
                    .append("instance", aQuota.getInstance())
                    .append("instanceType", aQuota.getInstanceType())
                    .getMap());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param aQuota
     * @return
     */
    @Override
    public boolean save(QuotaChildSI aQuota) {
        try {
            
            MapAppender lTemMap;
            
            lTemMap = new MapAppender()
                    .append("uuidquota", aQuota.getUuid())
                    .append("instance", aQuota.getInstance())
                    .append("instanceType", aQuota.getInstanceType())
                    .append("value", aQuota.getValue());
                    

            ItemCollectionUtils.saveItem(mRootUser, mCollectionQuotaInstance,
                    lTemMap.getMap());

        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *
     * @param aUuid
     * @param aInstance
     */
    @Override
    public void remove(String aUuid, String aInstance) {

        try {
            List<IItem> listItem = mCollectionQuota.getItemStorage()
                    .find("uuid", aUuid);

            if (!listItem.isEmpty()) {
                IItem lItem = listItem.get(0);
                if (lItem.get("instance").equals(aInstance)) {
                    ItemCollectionUtils.removeItem(mRootUser, mCollectionQuota,
                            lItem.getPK());
                }

                List<IItem> lChildQuotaistItem = findAll(mCollectionQuotaInstance,
                        "uuidquota", lItem.get("uuid"));

                for (IItem lChildItem : lChildQuotaistItem) {
                    ItemCollectionUtils.removeItem(mRootUser,
                            mCollectionQuotaInstance, lChildItem.getPK());
                }
            }

        } catch (Exception ex) {
            mLog.error("Error deleting quota with the message: " + ex.getMessage());
        }
    }

    /**
     *
     * @param aQuotaChild
     */
    @Override
    public void remove(QuotaChildSI aQuotaChild) {

        try {
            List<IItem> lChildQuotaistItem = findAll(mCollectionQuotaInstance,
                    "uuidquota", aQuotaChild.getUuid());

            for (IItem lItem : lChildQuotaistItem) {
                if (lItem.get("instance").equals(aQuotaChild.getInstance())) {
                    ItemCollectionUtils.removeItem(mRootUser,
                            mCollectionQuotaInstance, lItem.getPK());
                }
            }

        } catch (Exception ex) {
            mLog.error("Error deleting quota with the message: " + ex.getMessage());
        }
    }

    /**
     *
     * @param aUuid
     * @param aValue
     * @return
     */
    @Override
    public long update(String aUuid, Long aValue) {

        try {
            List<IItem> listItem = mCollectionQuota.getItemStorage()
                    .find("uuid", aUuid);

            if (!listItem.isEmpty()) {
                IItem lItem = listItem.get(0);
                lItem.set("value", aValue);

                ItemCollectionUtils.saveItem(mRootUser, mCollectionQuota,
                        new FastMap<String, Object>(lItem.getAttributes()));
            }

        } catch (Exception ex) {
            mLog.error("Error updating quota with the message: " + ex.getMessage());
        }
        return aValue;
    }

    /**
     *
     * @param aQuotaChild
     * @return
     */
    @Override
    public long update(QuotaChildSI aQuotaChild) {

        try {
            List<IItem> listItem = mCollectionQuotaInstance.getItemStorage()
                    .find("uuidquota", aQuotaChild.getUuid());

            for (IItem lItem : listItem) {
                if (lItem.get("instance").equals(aQuotaChild.getInstance())) {

                    lItem.set("value", aQuotaChild.getValue());
                    ItemCollectionUtils.saveItem(mRootUser, mCollectionQuotaInstance,
                            new FastMap<String, Object>(lItem.getAttributes()));
                }
            }
        } catch (Exception ex) {
            mLog.error("Error updating quota with the message: " + ex.getMessage());
        }

        return aQuotaChild.getValue();
    }

    /**
     *
     * @param aUuid
     * @return
     */
    @Override
    public boolean quotaExist(String aUuid) {

        try {
            List<IItem> listItem = mCollectionQuota.getItemStorage().find("uuid", aUuid);
            boolean flag = false;

            if (!listItem.isEmpty()) {
                flag = true;
            }

            return flag;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     *
     * @param aNameSpace
     * @param aQuotaIdentifier
     * @param aInstance
     * @param aActions
     * @return
     */
    @Override
    public boolean quotaExist(String aNameSpace, String aQuotaIdentifier,
            String aInstance, String aActions) {

        try {
            List<IItem> lListItem = findAll(mCollectionQuota, "ns", aNameSpace);
            List<IItem> lMachItem = new FastList<IItem>();
            if (lListItem.isEmpty()) {
                return false;
            }

            for (IItem lItem : lListItem) {
                if (lItem.get("quotaIdentifier").equals(aQuotaIdentifier)
                        && lItem.get("instance").equals(aInstance)) {
                    lMachItem.add(lItem);
                }
            }
            if (lMachItem.isEmpty()) {
                return false;
            }
            for (IItem lItem : lMachItem) {
                String lactions = (String) lItem.get("actions");
                String[] lArray = lactions.split(",");
                for (int i = 0; i < lArray.length; i++) {
                    if (aActions.indexOf(lArray[i]) != -1) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception ex) {
            mLog.debug("There is not a quota with namespace=" + aNameSpace);
            return true;
        }

    }

    /**
     *
     * @param aUuid
     * @return
     */
    @Override
    public String getActions(String aUuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param aQuotaType
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType) {

        FastList<IQuotaSingleInstance> lResult = null;

        try {
            List<IItem> lCur = mCollectionQuota.getItemStorage()
                    .find("quotaType", aQuotaType);

            lResult = getListInstance(lCur);
            return lResult;
        } catch (Exception ex) {
            return lResult;
        }
    }

    /**
     *
     * @param aIdentifier
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier) {

        FastList<IQuotaSingleInstance> lResult = null;
        try {

            List<IItem> lCur = mCollectionQuota.getItemStorage()
                    .find("quotaIdentifier", aIdentifier);

            lResult = getListInstance(lCur);

            return lResult;
        } catch (Exception ex) {
            return lResult;
        }
    }

    /**
     *
     * @param aIdentifier
     * @param aNameSpace
     * @param aInstanceType
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifierNSInstanceType(String aIdentifier, String aNameSpace, String aInstanceType) {

        FastList<IQuotaSingleInstance> lResult = null;
        try {
            List<IItem> lQuotaList = mCollectionQuota.getItemStorage()
                    .find("quotaIdentifier", aIdentifier);

            List<IItem> lMachItem = new FastList<IItem>();

            for (IItem lItem : lQuotaList) {
                if (lItem.get("instanceType").equals(aInstanceType)
                        && lItem.get("ns").equals(aNameSpace)) {
                    lMachItem.add(lItem);
                }

            }
            lResult = getListInstance(lMachItem);
            return lResult;
        } catch (Exception ex) {
            return lResult;
        }
    }

    /**
     *
     * @param aQuotaType
     * @param aNs
     * @param aInstance
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs, String aInstance) {

        FastList<IQuotaSingleInstance> lResult = null;
        try {
            List<IItem> lQuotaList = mCollectionQuota.getItemStorage()
                    .find("quotaType", aQuotaType);

            List<IItem> lMachItem = new FastList<IItem>();

            for (IItem lItem : lQuotaList) {
                if (lItem.get("instance").equals(aInstance)
                        && lItem.get("ns").equals(aNs)) {
                    lMachItem.add(lItem);
                }
            }
            lResult = getListInstance(lMachItem);
            return lResult;
        } catch (Exception ex) {
            return lResult;
        }
    }

    /**
     *
     * @param aQuotaIdentifier
     * @param aNs
     * @param aInstance
     * @param aInstanceType
     * @param aActions
     * @return
     * @throws ExceptionQuotaNotFound
     */
    @Override
    public String getUuid(String aQuotaIdentifier, String aNs, String aInstance,
            String aInstanceType, String aActions) throws ExceptionQuotaNotFound {

        List<IItem> lQuotaList = new FastList<IItem>();
        try {
            lQuotaList = mCollectionQuota.getItemStorage()
                    .find("quotaIdentifier", aQuotaIdentifier);
        } catch (Exception ex) {
            mLog.debug("quota not found with identifier=" + aQuotaIdentifier);
        }

        IItem lResponse = null;
        if (!lQuotaList.isEmpty()) {

            for (IItem lItem : lQuotaList) {
                if (lItem.get("instance").equals(aInstance)
                        && lItem.get("ns").equals(aNs)
                        && lItem.get("instanceType").equals(aInstanceType)
                        && lItem.get("actions").equals(aActions)) {
                    lResponse = lItem;
                }
            }
        }

        if (lResponse == null) {
            throw new ExceptionQuotaNotFound("not found");
        }

        String lUuid;
        lUuid = lResponse.get("uuid").toString();

        if (null != lUuid) {
            lUuid = lResponse.get("uuid").toString();
        } else {
            throw new ExceptionQuotaNotFound("not found");
        }
        return lUuid;
    }

    /**
     *
     * @param aQuotaType
     * @param aInstance
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType, String aInstance) {
        FastList<IQuotaSingleInstance> lResult = null;
        try {
            List<IItem> lQuotaList = mCollectionQuota.getItemStorage()
                    .find("quotaType", aQuotaType);

            List<IItem> lMachItem = new FastList<IItem>();

            for (IItem lItem : lQuotaList) {
                if (lItem.get("instance").equals(aInstance)) {
                    lMachItem.add(lItem);
                }
            }
            lResult = getListInstance(lMachItem);
            return lResult;
        } catch (Exception ex) {
            return lResult;
        }
    }

    /**
     *
     * @param aQuotaType
     * @param aNs
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType, String aNs) {

        FastList<IQuotaSingleInstance> lResult = null;
        try {
            List<IItem> lQuotaList = mCollectionQuota.getItemStorage()
                    .find("quotaType", aQuotaType);

            List<IItem> lMachItem = new FastList<IItem>();

            for (IItem lItem : lQuotaList) {
                if (lItem.get("ns").equals(aNs)) {
                    lMachItem.add(lItem);
                }
            }
            lResult = getListInstance(lMachItem);
            return lResult;
        } catch (Exception ex) {
            return lResult;
        }
    }

    /**
     *
     * @param aUuid
     * @return
     */
    @Override
    public IQuotaSingleInstance getQuotaByUuid(String aUuid) {

        IQuotaSingleInstance lSingle = null;
        if (quotaExist(aUuid)) {
            try {
                IItem lObj = mCollectionQuota.getItemStorage()
                        .find("uuid", aUuid).get(0);
                lSingle = getSingleInstance(lObj);

            } catch (Exception e) {
                return lSingle;
            }
        }
        return lSingle;
    }

    /**
     *
     * @param aUuid
     * @param aInstance
     * @return
     */
    @Override
    public Map<String, Object> getRawQuota(String aUuid, String aInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param aUuid
     * @param aResetDate
     */
    @Override
    public void updateIntervalResetDate(String aUuid, String aResetDate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<IItem> findAll(IItemCollection aCollection, String aAttribute, Object aValue) throws Exception {
        IItemCollection lCollection = aCollection;
        List<IItem> lResult = null;
        lResult = lCollection.getItemStorage()
                .find(aAttribute, aValue);

        return lResult;
    }

    private FastList<IQuotaSingleInstance> getListInstance(List<IItem> aQuotas) {

        FastList<IQuotaSingleInstance> lResult = new FastList<IQuotaSingleInstance>();

        for (IItem lObjInstance : aQuotas) {
            IQuotaSingleInstance lQuota = getSingleInstance(lObjInstance);
            lResult.add(lQuota);
        }

        return lResult;
    }

    private IQuotaSingleInstance getSingleInstance(IItem aObjQuota) {

        IQuotaSingleInstance lQuota;

        lQuota = QuotaHelper.factorySingleInstance(
                Long.parseLong(aObjQuota.get("value").toString()),
                aObjQuota.get("instance").toString(),
                aObjQuota.get("uuid").toString(),
                aObjQuota.get("ns").toString(),
                aObjQuota.get("quotaType").toString(),
                aObjQuota.get("quotaIdentifier").toString(),
                aObjQuota.get("instanceType").toString(),
                aObjQuota.get("actions").toString());
        //Getting child quota.
        try {
            List<IItem> lQChilds = mCollectionQuotaInstance.
                    getItemStorage().find("uuidquota",
                            aObjQuota.get("uuid"));

            for (IItem lObjInstance : lQChilds) {
                //adding quota Child of this quota.
                QuotaChildSI lChild = new QuotaChildSI(
                        lObjInstance.get("instance").toString(),
                        aObjQuota.get("uuid").toString(),
                        lObjInstance.get("instanceType").toString());

                lChild.setValue(Long.parseLong(
                        lObjInstance.get("value").toString()));
                lQuota.addChildQuota(lChild);
            }
        } catch (Exception e) {
            return lQuota;
        }
        return lQuota;
    }
}
