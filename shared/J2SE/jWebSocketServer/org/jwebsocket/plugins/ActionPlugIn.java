//	---------------------------------------------------------------------------
//	jWebSocket - ActionPlugIn (Community Edition, CE)
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
package org.jwebsocket.plugins;

import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.annotations.Authenticated;
import org.jwebsocket.plugins.annotations.RequireConnection;
import org.jwebsocket.plugins.annotations.RequirePlugIn;
import org.jwebsocket.plugins.annotations.RequirePlugIns;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.ConnectionManager;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ActionPlugIn extends TokenPlugIn {

	private final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aConfiguration
	 */
	public ActionPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lActionName = aToken.getType();

		if (isActionSupported(lActionName)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing action '" + lActionName + "'...");
			}
			callAction(lActionName, aConnector, aToken);
		}
	}

	/**
	 * Listener method that is executed before every action execution. If an
	 * exception is thrown during the method execution, the target action
	 * execution is canceled.
	 *
	 * @param aActionName The target action name
	 * @param aConnector The calling connector
	 * @param aToken
	 */
	public void beforeExecuteAction(String aActionName, WebSocketConnector aConnector, Token aToken) {

	}

	/**
	 * Plug-in action's executor
	 *
	 * @param aMethodName
	 * @param aConnector
	 * @param aToken
	 */
	@SuppressWarnings("UseSpecificCatch")
	protected void callAction(String aMethodName, WebSocketConnector aConnector, Token aToken) {
		aMethodName += "Action";
		try {
			// processing annotations
			Method lMethod = getClass().getMethod(aMethodName, WebSocketConnector.class, Token.class);

			// calling before execute action method on plug-in
			beforeExecuteAction(aMethodName, aConnector, aToken);

			// processing action annotations
			if (lMethod.isAnnotationPresent(Role.class)) {
				Role lAnnotation = lMethod.getAnnotation(Role.class);
				if (!hasAuthority(aConnector, lAnnotation.name())) {
					sendToken(aConnector, createAccessDenied(aToken));
					return;
				}
			} else if (lMethod.isAnnotationPresent(Authenticated.class)) {
				if (null == aConnector.getUsername()) {
					sendToken(aConnector, createAccessDenied(aToken));
					return;
				}
			} else if (lMethod.isAnnotationPresent(RequireConnection.class)) {
				RequireConnection lAnnotation = lMethod.getAnnotation(RequireConnection.class);
				ConnectionManager lConnManager = (ConnectionManager) JWebSocketBeanFactory.getInstance()
						.getBean(JWebSocketServerConstants.CONNECTION_MANAGER_BEAN_ID);
				if (!lConnManager.isValid(lAnnotation.name())) {
					Token lResponse = createResponse(aToken);
					lResponse.setCode(-1);
					lResponse.setString("msg", "Required '" + lAnnotation.name()
							+ "' connection is not valid. Action execution was canceled!");

					return;
				}
			} else if (lMethod.isAnnotationPresent(RequirePlugIn.class)) {
				RequirePlugIn lAnnotation = lMethod.getAnnotation(RequirePlugIn.class);
				if (null == JWebSocketFactory.getTokenServer().getPlugInById(lAnnotation.id())) {
					Token lResponse = createResponse(aToken);
					lResponse.setCode(-1);
					lResponse.setString("msg", "Required '" + lAnnotation.id()
							+ "' plug-in not found. Action execution was canceled!");
				}

				return;
			} else if (lMethod.isAnnotationPresent(RequirePlugIns.class)) {
				RequirePlugIns lAnnotation = lMethod.getAnnotation(RequirePlugIns.class);
				for (String lPlugInId : lAnnotation.ids()) {
					if (null == JWebSocketFactory.getTokenServer().getPlugInById(lPlugInId)) {
						Token lResponse = createResponse(aToken);
						lResponse.setCode(-1);
						lResponse.setString("msg", "Required '" + lPlugInId
								+ "' plug-in not found. Action execution was canceled!");

						return;
					}
				}
			}

			// invoking method
			lMethod.invoke(this, aConnector, aToken);
		} catch (Exception lEx) {
			String lExMsg, lExClass;
			boolean lError = false;
			if (null != lEx.getCause()) {
				// supporting nested exceptions produced inside the method invocation
				lExMsg = lEx.getCause().getMessage();
				lExClass = lEx.getCause().getClass().getName();
			} else {
				// normal exception
				lExMsg = lEx.getMessage();
				lExClass = lEx.getClass().getName();
				lError = true;
			}

			if (lError) {
				// let the global exception handler to process this exception
				throw new RuntimeException(lEx);
			} else if (mLog.isDebugEnabled()) {
				// nested expections are debugged only
				mLog.debug("Exception (" + lExClass + ":" + lExMsg + ") produced calling '"
						+ aMethodName + "' action on " + lEx.getCause().getStackTrace()[1].getClassName() + ":"
						+ lEx.getCause().getStackTrace()[1].getLineNumber()
						+ " class...");
			}
			Token lResponse = getServer().createErrorToken(aToken, -1, lExMsg);
			lResponse.setString("exception", lExClass);
			sendToken(aConnector, lResponse);
		}
	}

	/**
	 *
	 * @param aActionName
	 * @return
	 */
	protected boolean isActionSupported(String aActionName) {
		Method[] lMethods = getClass().getMethods();
		for (Method lMethod : lMethods) {
			if (lMethod.getName().equals(aActionName + "Action")) {
				return true;
			}
		}

		return false;
	}
}
