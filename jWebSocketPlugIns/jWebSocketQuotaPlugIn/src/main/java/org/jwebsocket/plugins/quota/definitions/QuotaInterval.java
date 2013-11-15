/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaIntervalSI;
import org.jwebsocket.plugins.quota.storage.StorageQuotaMongo;
import org.jwebsocket.plugins.quota.utils.Interval;
import org.jwebsocket.plugins.quota.utils.IntervalType;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;

/**
 *
 * @author Osvaldo Aguilar
 */
public class QuotaInterval extends BaseQuota {

    private Interval mInterval;
    
/**
 * 
 * @param aUuid
 * Get the quota's max value in database given the quota's uuid   
 * @return long
 */
    
    public long getMaxValue(String aUuid) {
        IQuotaSingleInstance lquota = mQuotaStorage.getQuotaByUuid(aUuid);
        Map<String, Object> lMap = getStorage().getRawQuota(aUuid, lquota.getInstance());
        int lMaxValue = Integer.parseInt(lMap.get("maxValue").toString());
        return lMaxValue;
    }

    public Interval getInterval() {
        return mInterval;
    }

    public void setInterval(Interval mInterval) {
        this.mInterval = mInterval;
    }

    @Override
    public long reduceQuota(String aUuid, long aAmount) {

        if (!CompareDates(Calendar.getInstance())) {
            return -1;
        }
        ResetQuota(aUuid);
        long lValue = getQuota(aUuid);
        if (lValue == 0) {
            return -1;
        }
        if (lValue - aAmount < 0) {
            getStorage().update(aUuid, (long) 0);
            return -1;
        }
        return getStorage().update(aUuid, lValue - aAmount);

    }

    @Override
    public void register(String aInstance, String aNameSpace, String aUuid, long aAmount, String aInstanceType, String aQuotaType, String aQuotaIdentifier) throws ExceptionQuotaAlreadyExist {

        try {
            super.register(aInstance, aNameSpace, aUuid, aAmount, aInstanceType, aQuotaType, aQuotaIdentifier);
        } catch (Exception ex) {
            Logger.getLogger(QuotaInterval.class.getName()).log(Level.SEVERE, null, ex);
        }
        IQuotaSingleInstance lSingleQuota;
        lSingleQuota = new QuotaIntervalSI(mInterval, aAmount, aInstance, aUuid, aNameSpace, aQuotaType, aQuotaIdentifier, aInstanceType);
        mQuotaStorage.save(lSingleQuota);
    }
    /**
     * @param aUuid 
     * reset the quota to max value if the actual date is after the quota's last end day 
     */

    private void ResetQuota(String aUuid) {
        Calendar lToday = Calendar.getInstance();
        Calendar lDate = Calendar.getInstance();
        boolean flag = false;
        IQuotaSingleInstance lquota = mQuotaStorage.getQuotaByUuid(aUuid);
        Map<String, Object> lMap = getStorage().getRawQuota(aUuid, lquota.getInstance());
        String lResetDate = lMap.get("resetDate").toString();
        String[] lSplit = lResetDate.split("-");
        if (mInterval.getIntervalType().equals(IntervalType.Yearly)) {
            lDate.set(Calendar.MONTH, Integer.parseInt(lSplit[0]));
            lDate.set(Calendar.YEAR, Integer.parseInt(lSplit[1]));
        } else if (mInterval.getIntervalType().equals(IntervalType.Monthly)) {
            lDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lSplit[0]));
            lDate.set(Calendar.MONTH, Integer.parseInt(lSplit[1]));
        } else if (mInterval.getIntervalType().equals(IntervalType.Weekly)) {
            lDate.set(Calendar.DAY_OF_WEEK, Integer.parseInt(lSplit[0]));
            lDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lSplit[1]));
        } else if (mInterval.getIntervalType().equals(IntervalType.Daily)) {
            lDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(lSplit[0]));
            lDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lSplit[1]));
        } else {
            lDate.set(Calendar.MINUTE, Integer.parseInt(lSplit[0]));
            lDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(lSplit[1]));
        }
        if (lToday.after(lDate)) {
            mQuotaStorage.update(aUuid, getMaxValue(aUuid));
            //to see for change the method's name
            mQuotaStorage.updateIntervalResetDate(aUuid, mInterval.toResetDate());
        }


    }

    private boolean CompareDates(Calendar aDate) {
        if (getStartDay().before(aDate) && getEndDay().after(aDate)) {
            return true;
        }
        return false;
    }

    private Calendar getStartDay() {
        Calendar lToday = Calendar.getInstance();
        Interval lAux = mInterval;
        while (lAux != null) {
            if (lAux.getIntervalType().equals(IntervalType.Yearly)) {
                lToday.set(Calendar.MONTH, Integer.parseInt(lAux.getStartDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Monthly) ){
                lToday.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lAux.getStartDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Weekly)) {
                lToday.set(Calendar.DAY_OF_WEEK, Integer.parseInt(lAux.getStartDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Daily)) {
                lToday.set(Calendar.HOUR_OF_DAY, Integer.parseInt(lAux.getStartDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Hourly)) {
                lToday.set(Calendar.MINUTE, Integer.parseInt(lAux.getStartDate()));

            }
            lAux = lAux.getInterval();
        }
        return lToday;
    }

    private Calendar getEndDay() {
        Calendar lToday = Calendar.getInstance();
        Interval lAux = mInterval;
        while (lAux != null) {
            if (lAux.getIntervalType().equals(IntervalType.Yearly) ){
                lToday.set(Calendar.MONTH, Integer.parseInt(lAux.getEndDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Monthly)) {
                lToday.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lAux.getEndDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Weekly)) {
                lToday.set(Calendar.DAY_OF_WEEK, Integer.parseInt(lAux.getEndDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Daily)) {
                lToday.set(Calendar.HOUR_OF_DAY, Integer.parseInt(lAux.getEndDate()));

            }
            if (lAux.getIntervalType().equals(IntervalType.Hourly) ){
                lToday.set(Calendar.MINUTE, Integer.parseInt(lAux.getEndDate()));

            }
            lAux = lAux.getInterval();
        }
        return lToday;
    }

    
}
