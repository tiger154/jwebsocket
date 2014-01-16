/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.api;

import java.util.List;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public interface IQuota {

    /**
     *
     * @return The quota type
     */
    public String getType();

    /**
     *
     * @return
     */
    public String getIdentifier();

    /**
     * specifies the storage engine for this quota.
     *
     * @param aQuotaStorage
     */
    public void setStorage(IQuotaStorage aQuotaStorage);

    /**
     *
     * @return
     */
    public IQuotaStorage getStorage();


    /**
     * Return the quota Object
     * @param aInstance
     * @param aNameSpace
     * @param aInstanceType
     * @param aActions
     * @return 
     */
    public IQuotaSingleInstance getQuota(String aInstance, String aNameSpace, String aInstanceType, String aActions);
    
    
    /**
     * Return the quota Object
     * @param aInstance
     * @param aNameSpace
     * @param aInstanceType
     * @param aActions
     * @return 
     */
    public List<IQuotaSingleInstance> getQuotas(String aInstance, String aNameSpace,
            String aInstanceType );
    
    /**
     *
     * @return
     */
    public long getDefaultReduceValue();

    /**
     *
     * @param aUuid
     * @return
     */
    public IQuotaSingleInstance getQuota(String aUuid);

    /**
     *
     * tries to reduce the current quota by the given amount, if the quota still
     * contains (i.e. allows) the request amount the result is the new quota
     * value, otherwise the result is less than zero, indicating that the quota
     * is exceeded by the returned amount.
     *
     * @param aInstance
     * @param aNameSpace
     * @param aUuid
     * @param aAmount
     * @return
     */
    public long reduceQuota(String aInstance, String aNameSpace,
            String aInstanceType, String aActions, long aAmount);

    /**
     * This tries to reduce the current quota by the default reduce value given
     * by the administrator
     *
     * @return
     */
    public long reduceQuota(String aUuid);

    /**
     *
     * @param aUuid
     * @param aAmount
     * @return
     */
    public long reduceQuota(String aUuid, long aAmount);

    /**
     * adds the given amount to the given quota, the result is the new quota
     * value.
     *
     * @param aInstance
     * @param aNameSpace
     * @param aUuid
     * @param aAmount
     * @return
     */
    public long increaseQuota(String aInstance, String aNameSpace,
            String aInstanceType,String aActions, long aAmount);

    /**
     *
     * @param aUuid
     * @param aAmount
     * @return
     */
    public long increaseQuota(String aUuid, long aAmount);

    /**
     * sets the given quota to the given value. The result is the old/previous
     * quota value.
     *
     * @param aInstance
     * @param aNameSpace
     * @param aUuid
     * @param aAmount
     * @return
     */
    public long setQuota(String aInstance, String aNameSpace,
            String aInstanceType,String aActions, long aAmount);

    /**
     *
     * @param aUuid
     * @param aAmount
     * @return
     */
    public long setQuota(String aUuid, long aAmount);

    /**
     * registers an instance at the quota class, this is required to e.g. log
     * volume in filter quotas or limit timed access.
     *
     * @param aInstance
     * @param aNameSpace
     * @param aUuid
     * @param aAmount
     */
    public void register(String aUuid, String aInstance, String aInstanceType)
            throws Exception;

    /**
     * the same as the above register method but an instance type could be
     * specified
     *
     * aInstanceType atr could take the following values that make reference to
     * an InstanceType:
     *
     * U > user : this is the default value taken for the above register method
     * G > group A > app M > modulate
     *
     * @param aInstance
     * @param aNameSpace
     * @param aUuid
     * @param aAmount
     * @param aInstanceType
     * @param aQuotaIdentifier
     */
    public void create(String aInstance, String aNameSpace, String aUuid,
            long aAmount, String aInstanceType, String aQuotaType,
            String aQuotaIdentifier, String aActions) throws Exception;

    /**
     * Return an String value with the tokens or actions for the quota
     *
     * @return
     */
    public String getActions(String aUuid);

    /**
     * unregisters an instance from the quota class, this is required to e.g.
     * log volume or times of access.
     *
     * @param aInstance
     * @param aNameSpace
     * @param aUuid
     * @param aAmount
     */
    public void unregister(String aInstance, String aUuid)
            throws ExceptionQuotaNotFound;

    /**
     * the same as the above register method but an instance type could be
     * specified
     *
     * aInstanceType atr could take the following values that make reference to
     * an InstanceType:
     *
     * U > user : this is the default value taken for the above register method
     * G > group A > app M > modulate
     *
     * @param aInstance
     * @param aNameSpace
     * @param aInstanceType
     */
    public void unregister(String aInstance,
            String aNameSpace, String aInstanceType, String aActions)
            throws ExceptionQuotaNotFound;

    /**
     * returns a list of registered instances (e.g. users) to this Quota class.
     *
     * @param aNamespace
     * @param aId
     * @return
     */
    public List<String> getRegisteredInstances(String aNamespace, String aId);

    /**
     *
     * @param aNamespace
     * @return
     */
    public List<String> getRegisterdQuotas(String aNamespace);

    /**
     *
     * @param aNamespace
     * @param aInstance
     * @param aInstanceType
     * @return
     */
    public String getQuotaUuid(String aQuotaIdentifier, String aNamespace, String aInstance,
            String aInstanceType, String aActions);
}
