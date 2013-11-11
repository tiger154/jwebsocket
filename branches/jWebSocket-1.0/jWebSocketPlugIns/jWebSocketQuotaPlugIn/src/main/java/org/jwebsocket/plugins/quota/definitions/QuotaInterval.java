/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaIntervalSI;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;

/**
 *
 * @author Osvaldo Aguilar
 */
public class QuotaInterval extends BaseQuota {

    private Interval mInterval;

    public long getMaxValue(String aUuid) {
        return 12;
    }

    public Interval getInterval() {
        return mInterval;
    }

    public void setInterval(Interval mInterval) {
        this.mInterval = mInterval;
    }

    @Override
    public long reduceQuota(String aUuid, long aAmount) {
        ResetQuota();
        if (CompareDates(Calendar.getInstance())) {
            return -1;
        }
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

    private void ResetQuota() {
        Calendar lToday = Calendar.getInstance();
        boolean flag = false;
        if (mInterval.mIntervalType == IntervalType.Yearly
                && lToday.get(Calendar.MONTH) > Integer.parseInt(mInterval.mEndDate)) {
            flag = true;
        }
        if (mInterval.mIntervalType == IntervalType.Monthly
                && lToday.get(Calendar.DAY_OF_MONTH) > Integer.parseInt(mInterval.mEndDate)) {
            flag = true;
        }
        if (mInterval.mIntervalType == IntervalType.Weekly
                && lToday.get(Calendar.DAY_OF_WEEK) > Integer.parseInt(mInterval.mEndDate)) {
            flag = true;
        }
        if (mInterval.mIntervalType == IntervalType.Daily
                && lToday.get(Calendar.HOUR_OF_DAY) > Integer.parseInt(mInterval.mEndDate)) {
            flag = true;
        }
        if (mInterval.mIntervalType == IntervalType.Hourly
                && lToday.get(Calendar.MINUTE) > Integer.parseInt(mInterval.mEndDate)) {
            flag = true;
        }
        if (flag) {
            String lUuid = getQuotaUuid(mQuotaIdentifier, mQuotaType, mQuotaType, mQuotaType);
            setQuota(lUuid, getMaxValue(lUuid));
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
            if (lAux.mIntervalType == IntervalType.Yearly) {
                lToday.set(Calendar.MONTH, Integer.parseInt(lAux.mStartDate));

            }
            if (lAux.mIntervalType == IntervalType.Monthly) {
                lToday.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lAux.mStartDate));

            }
            if (lAux.mIntervalType == IntervalType.Weekly) {
                lToday.set(Calendar.DAY_OF_WEEK, Integer.parseInt(lAux.mStartDate));

            }
            if (lAux.mIntervalType == IntervalType.Daily) {
                lToday.set(Calendar.HOUR_OF_DAY, Integer.parseInt(lAux.mStartDate));

            }
            if (lAux.mIntervalType == IntervalType.Hourly) {
                lToday.set(Calendar.MINUTE, Integer.parseInt(lAux.mStartDate));

            }
            lAux = lAux.mInterval;
        }
        return lToday;
    }

    private Calendar getEndDay() {
        Calendar lToday = Calendar.getInstance();
        Interval lAux = mInterval;
        while (lAux != null) {
            if (lAux.mIntervalType == IntervalType.Yearly) {
                lToday.set(Calendar.MONTH, Integer.parseInt(lAux.mEndDate));

            }
            if (lAux.mIntervalType == IntervalType.Monthly) {
                lToday.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lAux.mEndDate));

            }
            if (lAux.mIntervalType == IntervalType.Weekly) {
                lToday.set(Calendar.DAY_OF_WEEK, Integer.parseInt(lAux.mEndDate));

            }
            if (lAux.mIntervalType == IntervalType.Daily) {
                lToday.set(Calendar.HOUR_OF_DAY, Integer.parseInt(lAux.mEndDate));

            }
            if (lAux.mIntervalType == IntervalType.Hourly) {
                lToday.set(Calendar.MINUTE, Integer.parseInt(lAux.mEndDate));

            }
            lAux = lAux.mInterval;
        }
        return lToday;
    }

    public class Interval {

        private String mStartDate;
        private String mEndDate;
        private Interval mInterval;
        private IntervalType mIntervalType;

        public Interval(String mStartDate, String mEndDate, IntervalType mIntervalType) {
            this.mStartDate = mStartDate;
            this.mEndDate = mEndDate;
            this.mIntervalType = mIntervalType;
        }

        public String getStartDate() {
            return mStartDate;
        }

        public void setStartDate(String mStartDate) {
            this.mStartDate = mStartDate;
        }

        public String getEndDate() {
            return mEndDate;
        }

        public void setEndDate(String mEndDate) {
            this.mEndDate = mEndDate;
        }

        public Interval getInterval() {
            return mInterval;
        }

        public void setInterval(Interval mInterval) {
            this.mInterval = mInterval;
        }

        public IntervalType getIntervalType() {
            return mIntervalType;
        }

        public void setIntervalType(IntervalType mIntervalType) {
            this.mIntervalType = mIntervalType;
        }
        
        public String toString(){
            return "Y";
        }
    }

    public enum IntervalType {

        Hourly, Daily, Weekly, Monthly, Yearly
    }
}
//    /**
//     * Convert the given start string to date
//     * 
//     * @return 
//     */
//    public Calendar StartDateToDate() {
//        
//        
//        Calendar lDate = new GregorianCalendar();
//        String[] lDateArray = mStartDate.split("-");
//        Calendar lToday = Calendar.getInstance();
//        
//        
//        int el = lToday.getActualMaximum(Calendar.DAY_OF_MONTH);
//        
//        
//        String lFormatDate = (((lDateArray[3].equals("*")) ? "01" : lDateArray[3])
//                + "/" + ((lDateArray[2].equals("*")) ? "01" : lDateArray[2])
//                + "/" + String.valueOf(lToday.getTime().getYear())
//                + " " + ((lDateArray[1].equals("*")) ? "00" : lDateArray[1])
//                + ":" + ((lDateArray[0].equals("*")) ? "00" : lDateArray[0]));
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//
//        lDate.setTime(sdf.parse(lFormatDate, new ParsePosition(0)));
//
//        return lDate;
//    }
//
//    public Calendar EndDateToDate() {
//        Calendar lDate = new GregorianCalendar();
//        String[] lDateArray = mStartDate.split("-");
//        Calendar lToday = new GregorianCalendar();
//
//        String lFormatDate = (((lDateArray[3].equals("*"))
//                ? (getLastDayofMonth(lDateArray[2]))
//                : lDateArray[3])
//                + "/" + ((lDateArray[2].equals("*")) ? "12" : lDateArray[2])
//                + "/" + String.valueOf(lToday.getTime().getYear())
//                + " " + ((lDateArray[1].equals("*")) ? "23" : lDateArray[1])
//                + ":" + ((lDateArray[0].equals("*")) ? "59" : lDateArray[0]));
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//
//        lDate.setTime(sdf.parse(lFormatDate, new ParsePosition(0)));
//
//        return lDate;
//    }

