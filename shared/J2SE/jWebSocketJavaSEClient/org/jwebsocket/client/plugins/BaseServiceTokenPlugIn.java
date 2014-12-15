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
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Betancourt Toucet
 * @author Rolando Santamaria Maso
 */
public abstract class BaseServiceTokenPlugIn extends BaseClientTokenPlugIn {

	private Timer mTimer;

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public BaseServiceTokenPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
		if (aClient.isConnected()) {
			updateCpuUsage();
		}
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		super.processOpened(aEvent);
		
		updateCpuUsage();
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
		String lSourceConnector = null;
		if (aInToken != null) {
			lTokenId = aInToken.getInteger("utid", -1);
			lType = aInToken.getString("type");
			lNS = "org.jwebsocket.plugins.loadbalancer";
			lSourceConnector = aInToken.getString("sourceId");
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
		if (lSourceConnector != null) {
			aOutToken.setString("sourceId", lSourceConnector);
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
	private void updateCpuUsage() {
		getTimer().schedule(new JWSTimerTask() {

			@Override
			public void runTask() {

				try {
					Token lTokenCpuUsage = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "updateCpuUsage");
					lTokenCpuUsage.setDouble("usage", Tools.getCpuUsage());
					getTokenClient().sendToken(lTokenCpuUsage);
				} catch (Exception lEx) {
					this.cancel();
				}
			}
		}, 2000, 2000);
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		getTimer().cancel();
	}
}
