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
package org.jwebsocket.watchdog.test.impl;

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
		Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "ping");
		try {
			getClient().sendToken(lToken, new WatchDogTokenResponseListener(this, aReport) {
				@Override
				public void OnSuccess(Token token) {
					getReport().setTestResult(OK);
				}

				@Override
				public void OnResponse(Token token) {
					getTest().setIsDone(true);
				}

				@Override
				public void OnTimeout(Token token) {
					getReport().setTestResult(NOT_OK);
				}

				@Override
				public void OnFailure(Token token) {
					getReport().setTestResult(NOT_OK);
				}
			});
		} catch (WebSocketException ex) {
			aReport.setTestResult(NOT_OK);
			setIsDone(true);
		}
	}
}