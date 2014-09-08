//	---------------------------------------------------------------------------
//	jWebSocket - ServerMethodMatcher (Community Edition, CE)
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
package org.jwebsocket.plugins.rpc.util;

import java.lang.reflect.Method;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.MethodMatcher;

/**
 * @author Quentin Ambard Used to match a method against parameters. Does the
 * same thing than MethodMatcher, but allows the method server to add a
 * WebSocketConnector object as parameter. If so, the WebSocketConnector which
 * invoke this method will be passed.
 */
@SuppressWarnings("rawtypes")
public class ServerMethodMatcher extends MethodMatcher {

	private final WebSocketConnector mWebSocketConnector;

	/**
	 *
	 * @param aMethod
	 * @param aWebSocketConnector
	 */
	public ServerMethodMatcher(Method aMethod, WebSocketConnector aWebSocketConnector) {
		super(aMethod);
		mWebSocketConnector = aWebSocketConnector;
	}

	/**
	 * @return true if lParameterType is a WebSocjetConnector type. If so, add
	 * to the websocketConnector to the list of paramters
	 */
	@Override
	protected boolean specificMatching(Class lParameterType, int aIndice) {
		if (lParameterType == WebSocketConnector.class) {
			setMethodParameters(aIndice, mWebSocketConnector);
			return true;
		}
		return false;
	}

	/**
	 * return the number of valid parameters: all the parameters except
	 * WebSocketConnector If it's a WebSocketConnector, we will push the
	 * instance of the connector which is performing the (r)rpc
	 *
	 * @return
	 */
	@Override
	protected int getNumberOfValidParameters(Class[] aListOfParametersType) {
		int numberOfValidParameters = 0;
		for (Class lClass : aListOfParametersType) {
			if (lClass != WebSocketConnector.class) {
				numberOfValidParameters++;
			}
		}
		return numberOfValidParameters;
	}
}
