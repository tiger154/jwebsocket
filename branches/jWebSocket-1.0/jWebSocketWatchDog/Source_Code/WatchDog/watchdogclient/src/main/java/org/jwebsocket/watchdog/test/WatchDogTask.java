/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.Date;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.client.cgi.CGITokenClient;
import org.jwebsocket.watchdog.api.ITest;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTest;

/**
 *
 * @author lester
 */
public class WatchDogTask implements IWatchDogTask {

    private String id;
    private Integer seconds;
    private Integer minutes;
    private Integer hour;
    private Date time;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Integer month;
    private Integer year;
    private Date date;
    private String lastExecution;
    private Integer interval;
    private Integer everyNSeconds;
    private Integer everyNMinutes;
    private Integer everyNHours;
    private Integer everyNDays;
    private Integer everyNmonth;
    private Integer everyNYear;
    private String frequency;
    private List<IWatchDogTest> mTests = new FastList<IWatchDogTest>();
    private List<Integer> daysOfWeeksList;

    /**
     * Setter 
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setTests(List<IWatchDogTest> aTests) {
        mTests = aTests;
    }

    @Override
    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public void setHour(Integer hour) {
        this.hour = hour;
    }

    @Override
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    @Override
    public void setLastExecution(String lastExecution) {
        this.lastExecution = lastExecution;
    }

    @Override
    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    @Override
    public void setMonth(Integer month) {
        this.month = month;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    @Override
    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public void setEveryNSeconds(Integer seconds) {
        this.everyNSeconds = seconds;
    }

    @Override
    public void setEveryNMinutes(Integer minutes) {
        this.everyNMinutes = minutes;
    }

    @Override
    public void setEveryNHours(Integer hours) {
        this.everyNHours = hours;
    }

    @Override
    public void setEveryNDays(Integer days) {
        this.everyNDays = days;
    }

    @Override
    public void setEveryNmonth(Integer months) {
        this.everyNmonth = months;
    }

    @Override
    public void setEveryNYear(Integer years) {
        this.everyNYear = years;
    }

    @Override
    public void setDaysOfWeeks(List<Integer> list) {
        this.daysOfWeeksList = list;
    }

    @Override
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /*
     * To Execute a Task it needs a CGITokenCLient and a list of Test Report
     */
    @Override
    public void execute(CGITokenClient aClient, List<ITestReport> aReport) {

        ITest lTest;
        TestReport lTestReport;

        for (IWatchDogTest lTestDef : mTests) {
            try {
                lTestReport = new TestReport();

                lTest = (ITest) Class.forName(lTestDef.getImplClass()).newInstance();
                lTest.setClient(aClient);

                lTestReport.setTestDescription(lTestDef.getDescription());
                lTestReport.setTestId(lTestDef.getId());
                lTestReport.setFatal(lTestDef.isFatal());
                lTest.execute(lTestReport);

                while (!lTest.isDone()) {
                    //Wait until the test is finished
                    Thread.sleep(50);
                };
                
                aReport.add(lTestReport);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /*
     * Getter
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<IWatchDogTest> getTests() {
        return mTests;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public Date getTime() {
        return time;
    }

    @Override
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public Integer getHour() {
        return hour;
    }

    @Override
    public Integer getInterval() {
        return interval;
    }

    @Override
    public String getLastExecution() {
        return lastExecution;
    }

    @Override
    public Integer getMonth() {
        return month;
    }

    @Override
    public Integer getYear() {
        return year;
    }

    @Override
    public Integer getMinutes() {
        return minutes;
    }

    @Override
    public Integer getSeconds() {
        return seconds;
    }

    @Override
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    @Override
    public Integer getEveryNSeconds() {
        return everyNSeconds;
    }

    @Override
    public Integer getEveryNMinutes() {
        return everyNMinutes;
    }

    @Override
    public Integer getEveryNHours() {
        return everyNHours;
    }

    @Override
    public Integer getEveryNDays() {
        return everyNDays;
    }

    @Override
    public Integer getEveryNmonth() {
        return everyNmonth;
    }

    @Override
    public Integer getEveryNYear() {
        return everyNYear;
    }

    @Override
    public List<Integer> getDaysOfWeeks() {
        return daysOfWeeksList;
    }

    @Override
    public String getFrequency() {
        return frequency;
    }

    /*
     * Converting as Document so you can save it into MongoDB
     */
    @Override
    public DBObject asDocument() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("id", getId());
        obj.put("seconds", getSeconds());
        obj.put("minutes", getMinutes());
        obj.put("hour", getHour());
        obj.put("time", getTime());
        obj.put("dayofweek", getDayOfWeek());
        obj.put("dayofmonth", getDayOfMonth());
        obj.put("month", getMonth());
        obj.put("year", getYear());
        obj.put("date", getDate());
        obj.put("lastExecution", getLastExecution());
        obj.put("interval", getInterval());
        obj.put("frequency", getFrequency());

        BasicDBList l = new BasicDBList();
        BasicDBList m = new BasicDBList();

        for (IWatchDogTest t : getTests()) {
            l.add(t.getId());
        }

        obj.put("idTests", l);

        for (Integer t : getDaysOfWeeks()) {
            m.add(t);
        }

        obj.put("daysOfWeek", m);

        return obj;
    }
}
