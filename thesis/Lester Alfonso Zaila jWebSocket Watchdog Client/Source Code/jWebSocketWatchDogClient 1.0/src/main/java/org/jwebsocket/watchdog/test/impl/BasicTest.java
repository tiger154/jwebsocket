// ---------------------------------------------------------------------------
// jWebSocket - < BasicTest >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class BasicTest extends Test {

    @Override
    public void execute(ITestReport aReport) {
        Token lToken = TokenFactory.createToken("test", "testing.get.events_info");
        try {

            getClient().sendToken(lToken, new WatchDogTokenResponseListener(this, aReport) {
                //trying to reach the server

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
                                        //if all getting request are OK then Success 

                                        return;
                                    }
                                }
                            }
                        }
                    }
                    
                    getReport().setTestResult(NOT_OK);
                    //some test failed
                    getTest().setIsDone(true);
                }
            });
        } catch (WebSocketException ex) {
            aReport.setTestResult(NOT_OK);
            //writting the report
            setIsDone(true);
        }
    }
}