package org.jwebsocket.watchdog.api;

/**
 *
 * @author lester
 */
public interface ITestReport {
    //get the ID of the report test    

    /*
     *Getter 
     */
    
    public String getTestId();

    /*
     * get if the result is OK(true) or Not OK(false)
     */
    public Boolean getTestResult();
    
    /*
     * get the description of the test report
     */
    public String getTestDescription();
    
    public Boolean isFatal();

    
    /*
     * Setter
     */
    public void setTestResult(boolean aResult);

    public void setTestId(String aTest);
    
    public void setTestDescription(String aTestDescription);

    public void setFatal(boolean aIsFatal);
}
