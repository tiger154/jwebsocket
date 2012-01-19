package org.jwebsocket.watchdog.api;

import org.jwebsocket.client.cgi.CGITokenClient;

/**
 *
 * @author lester
 */
public interface ITest {

    /*
     * Setter
     */
    void setClient(CGITokenClient aClient);

    /*
     * Getter
     */
    CGITokenClient getClient();

    boolean isDone();

    /*
     * Execute the test receiving the report's list
     */
    void execute(ITestReport report);

    void setIsDone(boolean aIsDone);
}
