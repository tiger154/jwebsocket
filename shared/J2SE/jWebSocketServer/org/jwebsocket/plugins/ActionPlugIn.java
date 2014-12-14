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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.annotations.AnnotationManager;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;

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
			aResponse.abortChain();
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

	public class AssertionException extends RuntimeException {

		private int mCode;
		private String mMessage;

		public AssertionException(int mCode, String mMessage) {
			super(mMessage);
		}

		public int getCode() {
			return mCode;
		}
	}

	/**
	 * Throws an AssertionException if the given expression argument is FALSE.
	 *
	 * @param aExpression
	 * @param aCode The jWebSocket compliant response code. Example: -1
	 * @param aMessage The exception message
	 */
	protected void assertTrue(Boolean aExpression, int aCode, String aMessage) {
		if (!aExpression) {
			throw new AssertionException(aCode, aMessage);
		}
	}

	/**
	 * Throws an AssertionException if the given object value argument is NULL.
	 *
	 * @param aValue
	 * @param aCode The jWebSocket compliant response code. Example: -1
	 * @param aMessage
	 */
	protected void assertNotNull(Object aValue, int aCode, String aMessage) {
		if (null == aValue) {
			throw new AssertionException(aCode, aMessage);
		}
	}

	/**
	 * Throws an AssertionException if the given object value argument is NULL.
	 *
	 * @param aValue
	 * @param aMessage
	 */
	protected void assertNotNull(Object aValue, String aMessage) {
		assertNotNull(aValue, -1, aMessage);
	}

	/**
	 * Throws an AssertionException if the given expression argument is FALSE.
	 *
	 * @param aExpression
	 * @param aMessage
	 */
	protected void assertTrue(Boolean aExpression, String aMessage) {
		assertTrue(aExpression, -1, aMessage);
	}

	/**
	 * Throws an AssertionException
	 *
	 * @param aCode
	 * @param aMessage
	 */
	protected void fail(Integer aCode, String aMessage) {
		assertTrue(false, aCode, aMessage);
	}

	/**
	 * Throws an AssertionException
	 *
	 * @param aMessage
	 */
	protected void fail(String aMessage) {
		fail(-1, aMessage);
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

			// processing annotations
			AnnotationManager lAnnotationManager = (AnnotationManager) JWebSocketBeanFactory
					.getInstance().getBean("annotationManager");

			for (Annotation lA : lMethod.getAnnotations()) {
				if (lAnnotationManager.supports(lA.annotationType())) {
					try {
						lAnnotationManager.processAnnotation(lA, lMethod, new Object[]{aConnector, aToken});
					} catch (Exception lEx) {
						Token lResponse = createResponse(aToken);
						lResponse.setCode(-1);
						lResponse.setString("msg", lEx.getLocalizedMessage());

						sendToken(aConnector, lResponse);
						return;
					}
				}
			}

			// invoking method
			lMethod.invoke(this, aConnector, aToken);
		} catch (Exception lEx) {
			String lExMsg, lExClass;
			Integer lExCode = -1;

			if (lEx instanceof AssertionException) {
				lExCode = ((AssertionException) lEx).getCode();
				lExClass = lEx.getClass().getName();
				lExMsg = lEx.getLocalizedMessage();
			} else {
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
			}

			Token lResponse = getServer().createErrorToken(aToken, lExCode, lExMsg);
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
