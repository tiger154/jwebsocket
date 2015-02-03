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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.annotations.AllowMethodInvokation;
import org.jwebsocket.plugins.annotations.AnnotationManager;
import org.jwebsocket.plugins.annotations.Param;
import org.jwebsocket.plugins.annotations.Params;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.MapAppender;
import org.jwebsocket.util.ReflectionUtils;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ActionPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logger.getLogger(ActionPlugIn.class);

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
				mLog.debug("Processing action '" + aToken.getNS() + "." + lActionName + "'...");
			}
			callAction(lActionName, aConnector, aToken);
			aResponse.abortChain();
		}
	}

	/**
	 * Listener method that is executed before every action execution. If an exception is thrown
	 * during the method execution, the target action execution is canceled.
	 *
	 * @param aActionName The target action name
	 * @param aConnector The calling connector
	 * @param aToken
	 */
	public void beforeExecuteAction(String aActionName, WebSocketConnector aConnector, Token aToken) {
	}

	public class AssertionException extends RuntimeException {

		private int mCode;

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
	 * Get a parameter value from Token instance. Throws an AssertionException if parameter value is
	 * null.
	 *
	 * @param aToken
	 * @param aParameterName
	 * @return
	 */
	protected Object getParam(Token aToken, String aParameterName) {
		Object lValue = aToken.getObject(aParameterName);
		if (null == lValue) {
			fail(-1, "The parameter '" + aParameterName + "' value cannot be NULL!");
		}

		return lValue;
	}

	/**
	 * Throws an AssertionException
	 *
	 * @param aMessage
	 */
	protected void fail(String aMessage) {
		fail(-1, aMessage);
	}

	private class AnnotatedObject {

		Annotation annotation;
		Object object;

		public AnnotatedObject(Annotation annotation, Object object) {
			this.annotation = annotation;
			this.object = object;
		}
	}

	protected boolean doServicesInvokation(Object[] aServices, WebSocketConnector aConnector, Token aToken) {
		// getting the token type
		String lType = aToken.getType();

		// iterating over given services
		for (Object lService : aServices) {
			Method[] lServiceMethods = lService.getClass().getMethods();
			for (Method lServiceMethod : lServiceMethods) {
				if (lServiceMethod.getName().equals(lType) && lServiceMethod.isAnnotationPresent(AllowMethodInvokation.class)) {
					try {
						// processing method annotations
						processAnnotations(lServiceMethod, aConnector, aToken);
					} catch (Exception lEx) {
						Token lResponse = createResponse(aToken);
						lResponse.setCode(-1);
						lResponse.setString("msg", lEx.getLocalizedMessage());

						sendToken(aConnector, lResponse);
						return true;
					}

					// parameter's value container
					List<Object> lInvokationParams = new ArrayList<Object>();

					// getting method parameters values according to annotations
					List<ReflectionUtils.MethodParameter> lMethodParams = ReflectionUtils.getMethodParameters(lServiceMethod);
					for (ReflectionUtils.MethodParameter lMethodParam : lMethodParams) {
						Param lParamAnnotation = (Param) lMethodParam.getAnnotation(Param.class);
						if (null != lParamAnnotation) {
							if (lParamAnnotation.required()) {
								lInvokationParams.add(getParam(aToken, lParamAnnotation.id()));
							} else {
								lInvokationParams.add(aToken.getObject(lParamAnnotation.id()));
							}
						}
					}

					// invoking service method
					invokeMethod(lService, lServiceMethod, lInvokationParams, aConnector, aToken);

					return true;
				}
			}
		}
		return false;
	}

	protected boolean routeToServices(WebSocketConnector aConnector, Token aToken) {
		return false;
	}

	protected AnnotationManager getAnnotationManager() {
		// processing annotations
		AnnotationManager lAnnotationManager = (AnnotationManager) JWebSocketBeanFactory
				.getInstance().getBean("annotationManager");

		return lAnnotationManager;
	}

	private void invokeMethod(Object aSubject, Method aMethod, List<Object> aParams, WebSocketConnector aConnector, Token aToken) {
		try {
			// calling before execute action method on plug-in
			beforeExecuteAction(aMethod.getName(), aConnector, aToken);

			// invoke method
			Object lResult = aMethod.invoke(aSubject, aParams.toArray());
			boolean lIsServiceMethod = aMethod.isAnnotationPresent(AllowMethodInvokation.class);
			if (lIsServiceMethod) {
				// send method's invokation result to calling client
				Token lResponse = createResponse(aToken);
				lResponse.getMap().put("data", lResult);
				sendToken(aConnector, lResponse);
			}
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
							+ aMethod.getName() + "' action on " + lEx.getCause().getStackTrace()[1].getClassName() + ":"
							+ lEx.getCause().getStackTrace()[1].getLineNumber()
							+ " class...");
				}
			}

			Token lResponse = getServer().createErrorToken(aToken, lExCode, lExMsg);
			lResponse.setString("exception", lExClass);
			sendToken(aConnector, lResponse);
		}
	}

	private void processAnnotations(Method aMethod, WebSocketConnector aConnector, Token aToken) throws Exception {
		List<AnnotatedObject> lAnnotations = new ArrayList<AnnotatedObject>();
		// getting method annotations
		Annotation[] lMethodAnnotations = aMethod.getAnnotations();
		for (Annotation lMethodAnnotation : lMethodAnnotations) {
			lAnnotations.add(new AnnotatedObject(lMethodAnnotation, aMethod));
		}
		// getting method parameter's annotations
		List<ReflectionUtils.MethodParameter> lMethodParamsAnnotations = ReflectionUtils.getMethodParameters(aMethod);
		for (ReflectionUtils.MethodParameter lMethodParam : lMethodParamsAnnotations) {
			for (Annotation lA : lMethodParam.getAnnotations()) {
				lAnnotations.add(new AnnotatedObject(lA, lMethodParam));
			}
		}

		// processing annotations
		for (AnnotatedObject lAO : lAnnotations) {
			if (getAnnotationManager().supports(lAO.annotation.annotationType())) {
				getAnnotationManager().processAnnotation(lAO.annotation, lAO.object,
						new Object[]{aConnector, aToken});
			}
		}
	}

	/**
	 * Plug-in action's executor
	 *
	 * @param aMethodName
	 * @param aConnector
	 * @param aToken
	 */
	protected void callAction(String aMethodName, WebSocketConnector aConnector, Token aToken) {
		Method lMethod;
		try {
			aMethodName += "Action";
			// processing annotations
			lMethod
					= getClass().getMethod(aMethodName, WebSocketConnector.class, Token.class
					);

			processAnnotations(lMethod, aConnector, aToken);
		} catch (Exception lEx) {
			Token lResponse = createResponse(aToken);
			lResponse.setCode(-1);
			lResponse.setString("msg", lEx.getLocalizedMessage());

			sendToken(aConnector, lResponse);
			return;
		}

		// invoke plug-in method
		List<Object> lParams = new ArrayList<Object>();
		lParams.add(aConnector);
		lParams.add(aToken);
		invokeMethod(this, lMethod, lParams, aConnector, aToken);
	}

	/**
	 *
	 * @param aActionName
	 * @return
	 */
	protected boolean isActionSupported(String aActionName) {
		Method[] lMethods = getClass().getMethods();
		for (Method lMethod : lMethods) {
			if (lMethod.getName().equals(aActionName + "Action") && !lMethod.getName().equals("beforeExecuteAction")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the action plug-in API.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getAPIAction(WebSocketConnector aConnector, Token aToken) {
		List<Map> lAPI = new ArrayList<Map>();

		// getting plugin methods
		Method[] lMethods = getClass().getMethods();
		for (Method lMethod : lMethods) {
			if (lMethod.getName().endsWith("Action")
					// excluding core methods
					&& !lMethod.getName().equals("getAPIAction")
					&& !lMethod.getName().equals("beforeExecuteAction")) {
				// get method api
				lAPI.add(getMethodAPI(lMethod));
			}
		}

		// getting services methods
		for (Object lService : getServices()) {
			lMethods = lService.getClass().getMethods();
			for (Method lMethod : lMethods) {
				if (lMethod.isAnnotationPresent(AllowMethodInvokation.class)) {
					lAPI.add(getMethodAPI(lMethod));
				}
			}
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("data", lAPI);
		sendToken(aConnector, lResponse);
	}

	private Map getMethodAPI(Method aMethod) {
		Map lMethodAPI = new HashMap();
		List<Map> lParams = new ArrayList<Map>();
		lMethodAPI.put("params", lParams);

		if (aMethod.getName().endsWith("Action")) {
			Params lParamsAnnotation = aMethod.getAnnotation(Params.class);
			lMethodAPI.put("name", aMethod.getName().substring(0, aMethod.getName().length() - 6));
			if (null != lParamsAnnotation) {
				for (Param lParam : lParamsAnnotation.params()) {
					lParams.add(new MapAppender()
							.append("name", lParam.id())
							.append("required", lParam.required())
							.append("type", lParam.type().getCanonicalName())
							.getMap()
					);
				}
			}
		} else {
			lMethodAPI.put("name", aMethod.getName());
			List<ReflectionUtils.MethodParameter> lParamAnnotations = ReflectionUtils.getMethodParameters(aMethod);
			for (ReflectionUtils.MethodParameter lParamAnnotation : lParamAnnotations) {
				Param lParam = (Param) lParamAnnotation.getAnnotation(Param.class);
				lParams.add(new MapAppender()
						.append("name", lParam.id())
						.append("required", lParam.required())
						.append("type", lParamAnnotation.getType().getCanonicalName())
						.getMap()
				);
			}
		}

		return lMethodAPI;
	}

	public Object[] getServices() {
		return new Object[0];
	}
}
