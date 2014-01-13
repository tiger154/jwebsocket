/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaChildSI;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public abstract class BaseQuota implements IQuota {

    protected static Logger mLog = Logging.getLogger();
    protected IQuotaStorage mQuotaStorage;
    protected String mQuotaType;
    protected String mQuotaIdentifier;
    protected long mDefaultReduceValue;
    protected long mDefaultIncrease;

    @Override
    public long getDefaultReduceValue() {
        return mDefaultReduceValue;
    }

    public long getDefaultIncrease() {
        return mDefaultIncrease;
    }

    public void setDefaultReduceValue(long aDefaultReduceValue) {
        this.mDefaultReduceValue = aDefaultReduceValue;
    }

    public void setQuotaIdentifier(String mQuotaIdentifier) {
        this.mQuotaIdentifier = mQuotaIdentifier;
    }

    public void setDefaultIncreaseValue(long aDefaultIncrease) {
        this.mDefaultIncrease = aDefaultIncrease;
    }

    public void setQuotaType(String aQuotaType) {
        this.mQuotaType = aQuotaType;
    }

    @Override
    public IQuotaStorage getStorage() {
        return mQuotaStorage;
    }

    @Override
    public String getType() {
        return mQuotaType;
    }

    @Override
    public void setStorage(IQuotaStorage aQuotaStorage) {
        this.mQuotaStorage = aQuotaStorage;
    }

    @Override
    public IQuotaSingleInstance getQuota(String aInstance, String aNameSpace, String aInstanceType) {

        IQuotaSingleInstance lQResult;
        String lUuid;
        try {
            lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);
        } catch (Exception e) {
            lUuid = "not-found";
        }

        //Asking if the user has as part of a group quota that belong to a group. 
        if (lUuid.equals("not-found") && aInstanceType.equals("User")) {

            List<IQuotaSingleInstance> lQuotasGroup =
                    mQuotaStorage.getQuotasByIdentifierNSInstanceType(mQuotaIdentifier, aNameSpace, "Group");
            lQResult = findQuotaByInstance(lQuotasGroup, aInstance);

            if (lQResult == null) {
                List<IQuotaSingleInstance> lQuotasUser =
                        mQuotaStorage.getQuotasByIdentifierNSInstanceType(mQuotaIdentifier, aNameSpace, "User");
                lQResult = findQuotaByInstance(lQuotasUser, aInstance);
            }
            return lQResult;
        } else {
            return getQuota(lUuid);
        }
    }

    /**
     * get a IQuotaSingleInstance list and an string with the instanceType and
     * return the IQuotaSingleInstance.
     *
     * @return
     */
    private IQuotaSingleInstance findQuotaByInstance(List<IQuotaSingleInstance> aQuotas,
            String aInstance) {

        IQuotaSingleInstance lQResult = null;

        for (Iterator<IQuotaSingleInstance> it = aQuotas.iterator(); it.hasNext();) {
            IQuotaSingleInstance lQuotaSingle = it.next();
            QuotaChildSI lChild;
            lChild = lQuotaSingle.getChildQuota(aInstance);
            if (null != lChild) {

                if (lQuotaSingle.getInstanceType().equals("Group")) {

                    lQResult =
                            QuotaHelper.factorySingleInstance(lChild.getValue(), lChild.getInstance(),
                            lChild.getUuid(), lQuotaSingle.getNamespace(), lQuotaSingle.getQuotaType(),
                            lQuotaSingle.getQuotaIdentifier(), lChild.getInstanceType(), lQuotaSingle.getActions());
                }

                if (lQuotaSingle.getInstanceType().equals("User")) {

                    lQResult =
                            QuotaHelper.factorySingleInstance(lQuotaSingle.getvalue(), lChild.getInstance(),
                            lChild.getUuid(), lQuotaSingle.getNamespace(), lQuotaSingle.getQuotaType(),
                            lQuotaSingle.getQuotaIdentifier(), lChild.getInstanceType(), lQuotaSingle.getActions());
                }

                return lQResult;
            }
        }
        return null;
    }

    @Override
    public IQuotaSingleInstance getQuota(String aUuid) {

        IQuotaSingleInstance lQuotaInstance = (IQuotaSingleInstance) mQuotaStorage.getQuotaByUuid(aUuid);
        return lQuotaInstance;

    }

    @Override
    public String getIdentifier() {
        return mQuotaIdentifier;
    }

    @Override
    abstract public long reduceQuota(String aUuid, long aAmount);

    @Override
    public long reduceQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount) {

        IQuotaSingleInstance lQSingle = getQuota(aInstance, aNameSpace, aInstanceType);
        long lResult = -1;
        if (lQSingle != null) {
            long lReduce = lQSingle.getvalue() - aAmount;
            lResult = setQuota(lQSingle.getInstance(), aNameSpace, lQSingle.getInstanceType(), lReduce);
        }
        return lResult;
    }

    @Override
    public long reduceQuota(String aUuid) {
        return reduceQuota(aUuid, mDefaultReduceValue);
    }

    @Override
    public String getActions(String aUuid) {
        String lActions = this.getStorage().getActions(aUuid);
        return lActions;
    }

    @Override
    public long increaseQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount) {
        IQuotaSingleInstance lQSingle = getQuota(aInstance, aNameSpace, aInstanceType);
        long lResult = -1;
        if (lQSingle != null) {
            long lReduce = lQSingle.getvalue() + aAmount;
            lResult = setQuota(lQSingle.getInstance(), aNameSpace, lQSingle.getInstanceType(), lReduce);
        }
        return lResult;

    }

    @Override
    public long increaseQuota(String aUuid, long aAmount) {
        long lValue = getQuota(aUuid).getvalue();
        return getStorage().update(aUuid, lValue + aAmount);
    }

    @Override
    public long setQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount) {

        IQuotaSingleInstance lQSingle = getQuota(aInstance, aNameSpace, aInstanceType);
        //if the instanceType request is Group then update the father quota.
        if (lQSingle.getInstanceType().equals("Group") && aInstanceType.equals("Group")) {
            return setQuota(lQSingle.getUuid(), aAmount);
        }

        /**
         * if the quota type of the getQuota method is User, it is possible that
         * this quota be part of a father quota.
         */
        if (lQSingle.getInstanceType().equals("User")) {
            if (mQuotaStorage.quotaExist(aNameSpace, mQuotaIdentifier, lQSingle.getInstance())) {
                return setQuota(lQSingle.getUuid(), aAmount);
            } else {
                IQuotaSingleInstance lSingleInstance = mQuotaStorage.getQuotaByUuid(lQSingle.getUuid());
                QuotaChildSI lQChild = lSingleInstance.getChildQuota(lQSingle.getInstance());
                lQChild.setValue(aAmount);

                if (lSingleInstance.getInstanceType().equals("User")) {
                    return mQuotaStorage.update(lSingleInstance.getUuid(), lQChild.getValue());
                }
                return mQuotaStorage.update(lQChild);
            }
        } else {
            return -1;
        }
    }

    @Override
    public long setQuota(String aUuid, long aAmount) {
        return getStorage().update(aUuid, aAmount);
    }

    @Override
    public void register(String aUuid, String aInstance,
            String aInstanceType) throws Exception {

        if (!mQuotaStorage.quotaExist(aUuid)) {
            throw new ExceptionQuotaNotFound(aUuid);
        }
        //Creating the child Quota
        IQuotaSingleInstance lSingleInstance = mQuotaStorage.getQuotaByUuid(aUuid);
        QuotaChildSI lChildQuota = new QuotaChildSI(aInstance, aUuid, aInstanceType);

        //if a register quota occur over a quota with InstanceType user
        //The quota is shared between the users of this quota, by this reason
        //The quota is register to the parent quota with 0 as ther own value.
        if (lSingleInstance.getInstanceType().equals("User")) {
            lChildQuota.setValue(0);
        } else {
            lChildQuota.setValue(lSingleInstance.getvalue());
        }
        boolean lResult;

        lResult = lSingleInstance.addChildQuota(lChildQuota);

        if (lResult == true) {
            lResult = mQuotaStorage.save(lChildQuota);

            if (lResult == false) {
                throw new Exception("Error saving the quota.");
            }
        } else {
            throw new ExceptionQuotaAlreadyExist(aInstance);
        }
    }

    @Override
    public void create(String aInstance, String aNameSpace, String aUuid,
            long aAmount, String aInstanceType, String aQuotaType, String aQuotaIdentifier, String aActions)
            throws Exception {
        if (mQuotaStorage.quotaExist(aNameSpace, aQuotaIdentifier, aInstance)) {
            System.out.println("La quota existe create");
            throw new ExceptionQuotaAlreadyExist(mQuotaStorage.getUuid(aQuotaIdentifier, aNameSpace, aInstance, aInstanceType));
        }
    }

    @Override
    public void unregister(String aInstance, String aUuid)
            throws ExceptionQuotaNotFound {

        if (!mQuotaStorage.quotaExist(aUuid)) {
            throw new ExceptionQuotaNotFound(aUuid);
        }
        IQuotaSingleInstance lQSingle = getQuota(aUuid);
        if (lQSingle.getInstance().equals(aInstance)) {
            mQuotaStorage.remove(aUuid, aInstance);
        } else {
            QuotaChildSI lChild = lQSingle.getChildQuota(aInstance);
            if (lChild != null) {
                mQuotaStorage.remove(lChild);
            }

        }

    }

    @Override
    public void unregister(String aInstance,
            String aNameSpace, String aInstanceType)
            throws ExceptionQuotaNotFound {

        String lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);

        unregister(aInstance, lUuid);
    }

    @Override
    public List<String> getRegisteredInstances(String aNamespace, String aId) {
        return new FastList<String>();
    }

    @Override
    public List<String> getRegisterdQuotas(String aNamespace) {
        return new FastList<String>();
    }

    @Override
    public String getQuotaUuid(String aQuotaIdentifier, String aNamespace, String aInstance, String aInstanceType) {

        try {
            return mQuotaStorage.getUuid(aQuotaIdentifier, aNamespace, aInstance, aInstanceType);
        } catch (ExceptionQuotaNotFound ex) {
            return "not-found";
        }
    }
}
