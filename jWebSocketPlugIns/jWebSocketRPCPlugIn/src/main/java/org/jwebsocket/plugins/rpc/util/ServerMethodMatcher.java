//	---------------------------------------------------------------------------
//	jWebSocket - Server Method Matcher
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.rpc.util;

import java.lang.reflect.Method;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.MethodMatcher;

/**
 * @author Quentin Ambard
 * Used to match a method against parameters.
 * Does the same thing than MethodMatcher, but allows the method server to add a WebSocketConnector object as parameter.
 * If so, the WebSocketConnector which invoke this method will be passed.
 */
@SuppressWarnings("rawtypes")
public class ServerMethodMatcher extends MethodMatcher {

	private WebSocketConnector mWebSocketConnector;

	public ServerMethodMatcher(Method aMethod, WebSocketConnector aWebSocketConnector) {
		super(aMethod);
		mWebSocketConnector = aWebSocketConnector;
	}

	/**
	 * @return true if lParameterType is a WebSocjetConnector type. If so, add to the websocketConnector to the list of paramters
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
	 * return the number of valid parameters: all the parameters except WebSocketConnector
	 * If it's a WebSocketConnector, we will push the instance of the connector which is performing the (r)rpc
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
