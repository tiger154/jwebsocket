package org.jwebsocket.watchdog.test;

import org.jwebsocket.watchdog.api.ITestReport;

/**
 *
 * @author lester
 */
public class TestReport implements ITestReport{

    private String testDescription;
    private String testId;
    private boolean testResult;
    private boolean fatal;
    
    /**
     * Setter
     */
    @Override
    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    @Override
    public void setTestId(String testId) {
        this.testId = testId;
    }

    @Override
    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }
    
    @Override
    public void setFatal(boolean isFatal) {
       this.fatal = isFatal;
    }
    
    
    /**
     *Getter
     */
    @Override
    public String getTestDescription() {
        return testDescription;
    }

    @Override
    public String getTestId() {
        return testId;
    }

    @Override
    public Boolean getTestResult() {
        return testResult;
    }

    @Override
    public Boolean isFatal() {
        return fatal;
    }

    @Override
    public String toString() {
        return "TestReport{" + "testDescription=" + testDescription + ", testId=" + testId + ", testResult=" + testResult + ", fatal=" + fatal + '}';
    }
}
