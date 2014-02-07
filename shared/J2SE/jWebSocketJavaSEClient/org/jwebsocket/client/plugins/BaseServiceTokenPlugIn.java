//	---------------------------------------------------------------------------
//	jWebSocket BaseServiceTokenPlugIn (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.plugins;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author rbetancourt
 */
public class BaseServiceTokenPlugIn extends BaseClientTokenPlugIn {

	private Timer mTimer;

	/**
	 *
	 * @param aClient
	 */
	public BaseServiceTokenPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
		// UpdateCpuUsage();
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		super.processOpened(aEvent);
		SendCpuUsage();
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		getTimer().cancel();
	}

	/**
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		Token lResToken = TokenFactory.createToken();
		setResponseFields(aInToken, lResToken);
		return lResToken;
	}

	/**
	 *
	 * @param aInToken
	 * @param aOutToken
	 */
	private void setResponseFields(Token aInToken, Token aOutToken) {
		Integer lTokenId = null;
		String lType = null;
		String lNS = null;
		String lSourceID = null;
		if (aInToken != null) {
			lTokenId = aInToken.getInteger("utid", -1);
			lType = aInToken.getString("type");
			lNS = "org.jwebsocket.plugins.loadbalancer";
			lSourceID = aInToken.getString("sourceId");
		}
		aOutToken.setType("response");

		// if code and msg are already part of outgoing token do not overwrite!
		aOutToken.setInteger("code", aOutToken.getInteger("code", 0));
		aOutToken.setString("msg", aOutToken.getString("msg", "ok"));

		if (lTokenId != null) {
			aOutToken.setInteger("utid", lTokenId);
		}
		if (lNS != null) {
			aOutToken.setString("ns", lNS);
		}
		if (lType != null) {
			aOutToken.setString("reqType", lType);
		}
		if (lSourceID != null) {
			aOutToken.setString("sourceId", lSourceID);
		}
	}

	/**
	 *
	 * @return
	 */
	private Timer getTimer() {
		return (mTimer == null ? new Timer("Send Cpu Usage") : mTimer);
	}

	/**
	 *
	 */
	private void SendCpuUsage() {

		getTimer().schedule(new TimerTask() {

			@Override
			public void run() {

				try {
					Token lTokenCpuUsage = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "updateCpuUsage");
					lTokenCpuUsage.setDouble("usage", Tools.getCpuUsage());
					getTokenClient().sendToken(lTokenCpuUsage);
				} catch (Exception lEx) {
					Logger.getLogger(BaseServiceTokenPlugIn.class.getName()).log(Level.SEVERE, null,
						lEx + " while the load balancer CPU usage was update");
				}
			}
		}, 1500, 1500);
	}
}
