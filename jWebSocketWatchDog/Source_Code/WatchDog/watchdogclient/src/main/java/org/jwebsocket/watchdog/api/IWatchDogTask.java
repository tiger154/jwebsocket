package org.jwebsocket.watchdog.api;

import java.util.Date;
import java.util.List;
import org.jwebsocket.client.cgi.CGITokenClient;

/**
 *
 * @author lester
 */
public abstract interface IWatchDogTask extends IMongoDocument {

    /*
     * Getter
     */
    public String getId();

    public String getFrequency();

    public List<IWatchDogTest> getTests();

    public List<Integer> getDaysOfWeeks();

    public Date getDate();

    public Date getTime();

    public Integer getDayOfWeek();

    public Integer getDayOfMonth();

    public Integer getHour();

    public String getLastExecution();

    public Integer getInterval();

    public Integer getMinutes();

    public Integer getMonth();

    public Integer getSeconds();

    public Integer getYear();

    public Integer getEveryNSeconds();

    public Integer getEveryNMinutes();

    public Integer getEveryNHours();

    public Integer getEveryNDays();

    public Integer getEveryNmonth();

    public Integer getEveryNYear();

    /*
     * Setter
     */
    public void setFrequency(String frequency);

    public void setDaysOfWeeks(List<Integer> list);

    public void setId(String id);

    public void setTests(List<IWatchDogTest> aTests);

    public void setDate(Date date);

    public void setTime(Date time);

    public void setDayOfWeek(Integer dayOfWeek);

    public void setDayOfMonth(Integer dayOfMonth);

    public void setHour(Integer hour);

    public void setInterval(Integer interval);

    public void setLastExecution(String lastExecution);

    public void setMinutes(Integer minutes);

    public void setMonth(Integer month);

    public void setSeconds(Integer seconds);

    public void setYear(Integer year);

    public void setEveryNSeconds(Integer seconds);

    public void setEveryNMinutes(Integer minutes);

    public void setEveryNHours(Integer hours);

    public void setEveryNDays(Integer days);

    public void setEveryNmonth(Integer months);

    public void setEveryNYear(Integer years);
    
    /*
     * Execute use a CGITokenClient and a list of Reports.
     */
    public void execute(CGITokenClient aClient, List<ITestReport> aReport);
}