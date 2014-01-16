/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.storage;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
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
 * @author osvaldo
 */
public class ItemStorageQuota implements IQuotaStorage {

    private String mCollectionQuotaName;
    private String mCollectionQuotaInstanceName;
    private IItemDefinition mQuotaDefinition;
    private IItemDefinition mquotaInstanceDefinition;
    private String mCollectionAccessPassword;
    private String mCollectionSecretPassword;
    private String mRootUser = "root";
    private IItemCollection mCollectionQuota = null;
    private IItemCollection mCollectionQuotaInstance = null;
    private static final Logger mLog = Logging.getLogger();

    public void setCollectionQuotaName(String mCollectionQuotaName) {
        this.mCollectionQuotaName = mCollectionQuotaName;
    }

    public void setCollectionQuotaInstanceName(String mCollectionQuotaInstanceName) {
        this.mCollectionQuotaInstanceName = mCollectionQuotaInstanceName;
    }

    public void setQuotaDefinition(IItemDefinition mQuotaDefinition) {
        this.mQuotaDefinition = mQuotaDefinition;
    }

    public void setquotaInstanceDefinition(IItemDefinition mquotaInstanceDefinition) {
        this.mquotaInstanceDefinition = mquotaInstanceDefinition;
    }

    public void setCollectionAccessPassword(String mCollectionAccessPassword) {
        this.mCollectionAccessPassword = mCollectionAccessPassword;
    }

    public void setCollectionSecretPassword(String mCollectionSecretPassword) {
        this.mCollectionSecretPassword = mCollectionSecretPassword;
    }

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
            IItemCollection lQuotaCollection = lCollectionProvider
                    .getCollection(mCollectionQuotaName, mQuotaDefinition.getType());
            lQuotaCollection.setAccessPassword(mCollectionAccessPassword);
            lQuotaCollection.setSecretPassword(mCollectionSecretPassword);
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
            IItemCollection lQuotaInstCollection = lCollectionProvider
                    .getCollection(mCollectionQuotaInstanceName,
                    mquotaInstanceDefinition.getType());

            lQuotaInstCollection.setAccessPassword(mCollectionAccessPassword);
            lQuotaInstCollection.setSecretPassword(mCollectionSecretPassword);
            lQuotaInstCollection.setOwner(mRootUser);
            // saving changes
            lCollectionProvider.saveCollection(lQuotaInstCollection);
        }

        mCollectionQuota = lCollectionProvider.getCollection(mCollectionQuotaName);
        mCollectionQuotaInstance = lCollectionProvider.getCollection(mCollectionQuotaInstanceName);
    }

    @Override
    public boolean save(IQuotaSingleInstance aQuota) {
        try {
            ItemCollectionUtils.saveItem(mRootUser, mCollectionQuota, new MapAppender()
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

    @Override
    public boolean save(QuotaChildSI aQuota) {
        try {
            ItemCollectionUtils.saveItem(mRootUser, mCollectionQuotaInstance, new MapAppender()
                    .append("uuidQuota", aQuota.getUuid())
                    .append("instance", aQuota.getInstance())
                    .append("instanceType", aQuota.getInstanceType())
                    .append("value", aQuota.getValue())
                    .getMap());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

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
                        "uuidQuota", lItem.get("uuid"));

                for (IItem lChildItem : lChildQuotaistItem) {
                    ItemCollectionUtils.removeItem(mRootUser,
                            mCollectionQuotaInstance, lChildItem.getPK());
                }
            }

        } catch (Exception ex) {
            mLog.error("Error deleting quota with the message: " + ex.getMessage());
        }
    }

    @Override
    public void remove(QuotaChildSI aQuotaChild) {

        try {
            List<IItem> lChildQuotaistItem = findAll(mCollectionQuotaInstance,
                    "uuidQuota", aQuotaChild.getUuid());

            for (IItem lItem : lChildQuotaistItem) {
                if (lItem.get("instance").equals(aQuotaChild.getInstance())){
                    ItemCollectionUtils.removeItem(mRootUser,
                        mCollectionQuotaInstance, lItem.getPK());
                }
            }

        } catch (Exception ex) {
            mLog.error("Error deleting quota with the message: " + ex.getMessage());
        }
    }

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

    @Override
    public long update(QuotaChildSI aQuotaChild) {

        try {
            List<IItem> listItem = mCollectionQuotaInstance.getItemStorage()
                    .find("uuidQuota", aQuotaChild.getUuid());

            for (IItem lItem : listItem) {
                if (lItem.get("instance").equals(aQuotaChild.getInstance())){
                    
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
            mLog.error("quota not found in in quota exist method");
            return true;
        }

    }

    @Override
    public String getActions(String aUuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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

    @Override
    public String getUuid(String aQuotaIdentifier, String aNs, String aInstance,
            String aInstanceType,String aActions ) throws ExceptionQuotaNotFound {

        List<IItem> lQuotaList = new FastList<IItem>();
        try {
            lQuotaList = mCollectionQuota.getItemStorage()
                    .find("quotaIdentifier", aQuotaIdentifier);
        } catch (Exception ex) {
            mLog.error("An error occur in the getUiuid method of the quota Storage ");
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

    @Override
    public Map<String, Object> getRawQuota(String aUuid, String aInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
                    getItemStorage().find("uuidQuota",
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
