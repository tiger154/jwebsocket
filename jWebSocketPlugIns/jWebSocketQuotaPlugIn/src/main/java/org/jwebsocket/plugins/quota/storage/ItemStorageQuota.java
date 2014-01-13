/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private IItemCollection mCollectionQuota = null;
    private IItemCollection mCollectionQuotaInstance = null;

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
            lQuotaCollection.setOwner("root");
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
            lQuotaInstCollection.setOwner("root");
            // saving changes
            lCollectionProvider.saveCollection(lQuotaInstCollection);
        }

        mCollectionQuota = lCollectionProvider.getCollection(mCollectionQuotaName);
        mCollectionQuotaInstance = lCollectionProvider.getCollection(mCollectionQuotaInstanceName);

    }

    @Override
    public boolean save(IQuotaSingleInstance aQuota) {
        try {
            ItemCollectionUtils.saveItem("root", mCollectionQuota, new MapAppender()
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
            ItemCollectionUtils.saveItem("root", mCollectionQuotaInstance, new MapAppender()
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
    public void remove(String aInstance, String aUuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(QuotaChildSI aQuotaChild) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long update(String aUuid, Long aValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long update(QuotaChildSI aQuotaChild) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean quotaExist(String aUuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean quotaExist(String aNameSpace, String aQuotaIdentifier, String aInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getActions(String aUuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifierNSInstanceType(String aIdentifier, String aNameSpace, String aInstanceType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs, String aInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUuid(String aQuotaIdentifier, String aNs, String aInstance, String aInstanceType) throws ExceptionQuotaNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType, String aInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType, String aNs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IQuotaSingleInstance getQuotaByUuid(String aUuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, Object> getRawQuota(String aUuid, String aInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateIntervalResetDate(String aUuid, String aResetDate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
