package org.jwebsocket.watchdog.test.impl;

import java.util.Map;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.test.Test;
import org.jwebsocket.watchdog.test.WatchDogTokenResponseListener;

/**
 *
 * @author lester
 */
public class BasicTest extends Test {

    @Override
    public void execute(ITestReport aReport) {
        Token lToken = TokenFactory.createToken("test", "testing.get.events_info");
        try {
            getClient().sendToken(lToken, new WatchDogTokenResponseListener(this, aReport) {

                @Override
                public void OnSuccess(Token token) {
                    Map lInfo = token.getMap("table");
                    if (null != lInfo) {
                        if (lInfo.containsKey("version")) {
                            if (lInfo.containsKey("name")) {
                                if (lInfo.get("version") instanceof String) {
                                    if (lInfo.get("name") instanceof String) {
                                        getReport().setTestResult(OK);
                                        getTest().setIsDone(true);
                                    }
                                }
                            }
                        }
                    } else {
                        getReport().setTestResult(NOT_OK);
                        getTest().setIsDone(true);
                    }
                }

                @Override
                public void OnTimeout(Token token) {
                    getReport().setTestResult(NOT_OK);
                    getTest().setIsDone(true);
                }

                @Override
                public void OnFailure(Token token) {
                    getReport().setTestResult(NOT_OK);
                    getTest().setIsDone(true);
                }
            });
        } catch (WebSocketException ex) {
            aReport.setTestResult(NOT_OK);
            setIsDone(true);
        }
    }
}