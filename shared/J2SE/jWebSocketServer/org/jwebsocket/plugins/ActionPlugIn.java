//	---------------------------------------------------------------------------
//	jWebSocket - ActionPlugIn
//	Copyright (c) 2012 
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
package org.jwebsocket.plugins;

import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.annotations.Authenticated;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
 */
public class ActionPlugIn extends TokenPlugIn {

	private Logger mLog = Logging.getLogger();

	public ActionPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);

		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info(this.getClass().getSimpleName() + " plug-in successfully instantiated.");
		}
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

	protected void callAction(String aMethodName, WebSocketConnector aConnector, Token aToken) {
		aMethodName += "Action";
		try {
			// processing annotations
			Method lMethod = getClass().getMethod(aMethodName, WebSocketConnector.class, Token.class);

			// processing core annotations
			if (lMethod.isAnnotationPresent(Role.class)) {
				Role lRole = lMethod.getAnnotation(Role.class);
				if (!hasAuthority(aConnector, lRole.name())) {
					sendToken(aConnector, createAccessDenied(aToken));
					return;
				}
			} else if (lMethod.isAnnotationPresent(Authenticated.class)) {
				if (null == aConnector.getUsername()) {
					sendToken(aConnector, createAccessDenied(aToken));
					return;
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
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "calling '" + aMethodName + "' action..."));
				lEx.printStackTrace(System.out);
			} else if (mLog.isDebugEnabled()) {
				// nested expections are debugged only
				mLog.debug("Exception (" + lExClass + ":" + lExMsg + ") produced calling '"
						+ aMethodName + "' action on " + lEx.getCause().getStackTrace()[1].getClassName() + ":"
						+ lEx.getCause().getStackTrace()[1].getLineNumber()
						+ " class...");
			}
			Token lResponse = getServer().createErrorToken(aToken, -1, lExMsg);
			sendToken(aConnector, lResponse);
		}
	}

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
