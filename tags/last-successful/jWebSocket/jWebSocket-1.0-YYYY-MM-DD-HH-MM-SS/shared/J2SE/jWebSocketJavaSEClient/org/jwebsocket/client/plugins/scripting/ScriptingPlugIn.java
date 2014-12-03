//	---------------------------------------------------------------------------
//	jWebSocket - ScriptingPlugIn (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.client.plugins.scripting;

import java.util.List;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ScriptingPlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 * @param aClient
	 */
	public ScriptingPlugIn(WebSocketTokenClient aClient) {
		super(aClient, JWebSocketClientConstants.NS_SCRIPTING);
	}

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public ScriptingPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	/**
	 * Calls an application published object method.
	 *
	 * @param aApp The application name
	 * @param aObjectId The application published object identifier
	 * @param aMethod The method name
	 * @param aArgs The method arguments
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void callMethod(String aApp, String aObjectId, String aMethod, List<Object> aArgs,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "callMethod");
		lRequest.setString("app", aApp);
		lRequest.setString("method", aMethod);
		lRequest.setString("objectId", aObjectId);
		lRequest.setList("args", aArgs);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Reloads an script application in runtime.
	 *
	 * @param aApp The application name
	 * @param aHotReload If TRUE, the script app is reloaded without to destroy
	 * the app context. Default TRUE
	 * @param aListener The response listener
	 * @throws WebSocketException
	 */
	public void reloadApp(String aApp, Boolean aHotReload, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "reloadApp");
		lRequest.setString("app", aApp);
		lRequest.setBoolean("hotReload", aHotReload);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Sends a token to an script application.
	 *
	 * @param aApp The application name
	 * @param aToken The token to be sent
	 * @param aListener The response listener
	 * @throws WebSocketException
	 */
	public void sendToken(String aApp, Token aToken, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lToken = TokenFactory.createToken(getNS(), "token");
		lToken.setString("app", aApp);
		lToken.setToken("token", aToken);

		getTokenClient().sendToken(aToken, aListener);
	}

	/**
	 * Gets the target script application version.
	 *
	 * @param aApp The application name
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void getVersion(String aApp, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "getVersion");
		lRequest.setString("app", aApp);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * List server side script apps
	 *
	 * @param aUserOnly If TRUE, only the active user apps are listed, FALSE
	 * will list all apps.
	 * @param aNamesOnly If TRUE, only the names value is retrieved per app,
	 * FALSE will include more app data.
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void listApps(boolean aUserOnly, boolean aNamesOnly,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "listApps");
		lRequest.setBoolean("userOnly", aUserOnly);
		lRequest.setBoolean("namesOnly", aNamesOnly);

		getTokenClient().sendToken(lRequest, aListener);
	}
}
