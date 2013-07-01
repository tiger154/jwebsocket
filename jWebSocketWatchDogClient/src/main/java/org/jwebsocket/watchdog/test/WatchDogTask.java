// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-212 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.watchdog.test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.watchdog.api.ITest;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTest;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class WatchDogTask implements IWatchDogTask {

    private String mId;
    private String mType;
    private String mLastExecution;
    private Integer mEveryNMinutes;
    private Integer mEveryNHours;
    private Integer mEveryNDays;
    private List<IWatchDogTest> mTests = new FastList<IWatchDogTest>();

    /**
     * Setter 
     */
    @Override
    public void setId(String aId) {
        this.mId = aId;
    }

    @Override
    public void setTests(List<IWatchDogTest> aTests) {
        mTests = aTests;
    }

    @Override
    public void setLastExecution(String aLastExecution) {
        this.mLastExecution = aLastExecution;
    }

    @Override
    public void setEveryNMinutes(Integer aMinutes) {
        this.mEveryNMinutes = aMinutes;
    }

    @Override
    public void setEveryNHours(Integer aHours) {
        this.mEveryNHours = aHours;
    }

    @Override
    public void setEveryNDays(Integer aDays) {
        this.mEveryNDays = aDays;
    }

    @Override
    public void setType(String aType) {
        this.mType = aType;

    }

    /*
     * To Execute a Task it needs a CGITokenCLient and a list of Test Report
     */
    @Override
    public void execute(BaseTokenClient aClient, List<ITestReport> aReport) {

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
    public String getType() {
        return mType;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public List<IWatchDogTest> getTests() {
        return mTests;
    }

    @Override
    public String getLastExecution() {
        return mLastExecution;
    }

    @Override
    public Integer getEveryNMinutes() {
        return mEveryNMinutes;
    }

    @Override
    public Integer getEveryNHours() {
        return mEveryNHours;
    }

    @Override
    public Integer getEveryNDays() {
        return mEveryNDays;
    }

    /*
     * Converting as Document so you can save it into MongoDB
     */
    @Override
    public DBObject asDocument() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("id", getId());
        obj.put("everyNMinutes", getEveryNMinutes());
        obj.put("everyNHours", getEveryNHours());
        obj.put("everyNDays", getEveryNDays());
        obj.put("frequency", getType());
        obj.put("lastExecution", getLastExecution());

        BasicDBList lList = new BasicDBList();

        for (IWatchDogTest t : getTests()) {
            lList.add(t.getId());
        }

        obj.put("idTests", lList);

        return obj;
    }
}
