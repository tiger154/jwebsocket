//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn
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
package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.JWebSocketJarClassLoader;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.rpc.RPCCallableClassLoader.MethodRightLink;
import org.jwebsocket.plugins.rpc.rrpc.Rrpc;
import org.jwebsocket.plugins.rpc.rrpc.RrpcRightNotGrantedException;
import org.jwebsocket.plugins.rpc.util.RPCRightNotGrantedException;
import org.jwebsocket.plugins.rpc.util.ServerMethodMatcher;
import org.jwebsocket.plugins.rpc.util.TypeConverter;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.Token;

/**
 * This plug-in provides all the functionality for remote procedure calls (RPC)
 * for client-to-server (C2S) apps, and reverse remote procedure calls (RRPC)
 * for server-to-client (S2C) or client-to-client apps (C2C).
 *
 * @author aschulze
 * @author Quentin Ambard
 */
// TODO: questions for Alex:
// When doing a rrpc S2C, doing the following sendToken(null, lConnector,
// lRRPC); log a warn error since the source connector is null. Do we have a
// kind of ServerConnector ?
public class RPCPlugIn extends TokenPlugIn {

	// keys to buil the rrpc call
	private static RPCPlugIn sRPCPlugIn = null;
	private static Logger mLog = Logging.getLogger(RPCPlugIn.class);
	// Store the parameters type allowed for rpc method.
	private Map<String, RPCCallableClassLoader> mRpcCallableClassLoader = new FastMap<String, RPCCallableClassLoader>();

	// TODO: We need simple unique IDs to address a certain target, session id not
	// suitable here.
	// TODO: Show target(able) clients in a drop down box
	// TODO: RPC demo does not show other clients logging in
	/**
	 *
	 */
	public RPCPlugIn() {
		this(null);
	}

	public RPCPlugIn(PluginConfiguration configuration) {
		super(configuration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating rpc plug-in...");
		}
		// specify default name space
		this.setNamespace(CommonRpcPlugin.NS_RPC_DEFAULT);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void engineStarted(WebSocketEngine aEngine) {
		// we get the instance of the Plugin for the rrpc module:
		sRPCPlugIn = this;

		Class lClass = null;

		if (mLog.isDebugEnabled()) {
			mLog.debug("RPC Rights found in xml file: " + SecurityFactory.getGlobalRights(getNamespace()).getRightIdSet().toString());
		}

		loadClassFromThirdJavaPart();

		// Load map of granted procs
		Set<String> lPlugInRights = SecurityFactory.getGlobalRights(getNamespace()).getRightIdSet();
		for (String lRightId : lPlugInRights) {
			// We remove the pluginId because we just want the name of the method:
			String lFullMethodName = lRightId.substring(getNamespace().length() + 1);
			// We don't care about global rpc and rrpc rights in this section.
			if (!lFullMethodName.equals(CommonRpcPlugin.RPC_RIGHT_ID) && !lFullMethodName.equals(CommonRpcPlugin.RRPC_RIGHT_ID)) {
				// setting with parameters type to handle java method overload
				String[] lParameterTypes = null;
				if (lFullMethodName.indexOf("(") != -1 && lFullMethodName.indexOf(")") != -1) {
					String lParameters = lFullMethodName.substring(lFullMethodName.indexOf("(") + 1, lFullMethodName.length() - 1);
					lParameters = lParameters.replace(" ", "");
					lParameterTypes = lParameters.split(",");
					lFullMethodName = lFullMethodName.substring(0, lFullMethodName.indexOf("("));
				}
				String lClassName = lFullMethodName.substring(0, lFullMethodName.lastIndexOf("."));
				String lMethodName = lFullMethodName.substring(lFullMethodName.lastIndexOf(".") + 1);

				if (!mRpcCallableClassLoader.containsKey(lClassName)) {
					initRPCCallableClass(loadClassFromClassPath(lClassName), lClassName);
				}

				Method lMethod = getValidMethod(lClassName, lMethodName, lParameterTypes);
				if (lMethod != null) {
					// Add this method as a RPCCallable method.
					mRpcCallableClassLoader.get(lClassName).addMethod(lMethodName, lMethod, lRightId);
				}
			}
		}
	}

	/**
	 * Load the class from classpath with logs of needed.
	 * @param aClassName the class to be load
	 * @return the class loaded, or null if not found.
	 */
	@SuppressWarnings("rawtypes")
	private Class loadClassFromClassPath(String aClassName) {
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Trying to load class '"
						+ aClassName + "' from classpath...");
			}
			return Class.forName(aClassName);
		} catch (ClassNotFoundException lEx) {
			mLog.info("Class '" + aClassName + "' not found in classpath"
					+ ", hence trying to load from jar.");
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " loading class from classpath: "
					+ lEx.getMessage()
					+ ", hence trying to load from jar.");
		}
		return null;
	}

	/**
	 * Try to load the classes which are not suposed to be on the classPath, so the right definition isn't enought;
	 */
	@SuppressWarnings("rawtypes")
	private void loadClassFromThirdJavaPart() {
		// TODO: move JWebSocketJarClassLoader into ServerAPI module ?
		JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
		Class lClass = null;
		Map<String, Object> lSettings = getSettings();
		// load map of RPC libraries first
		for (Entry<String, Object> lSetting : lSettings.entrySet()) {
			String lKey = lSetting.getKey();
			String lValue = lSetting.getValue().toString();
			if (lKey.startsWith("class:")) {
				String lClassName = lKey.substring(6);
				lClass = loadClassFromClassPath(lClassName);
				// if class could not be loaded from classpath...
				if (lClass == null) {
					String lJarFilePath = null;
					try {
						lJarFilePath = JWebSocketConfig.getLibsFolder(lValue);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Trying to load class '"
									+ lClassName + "' from jar '"
									+ lJarFilePath + "'...");
						}
						lClassLoader.addFile(lJarFilePath);
						lClass = lClassLoader.loadClass(lClassName);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Class '" + lClassName
									+ "' successfully loaded from '"
									+ lJarFilePath + "'.");
						}
					} catch (Exception ex) {
						mLog.error(ex.getClass().getSimpleName()
								+ " loading jar '" + lJarFilePath + "': "
								+ ex.getMessage());
					}
				}
				// could the class be loaded?
				initRPCCallableClass(lClass, lClassName);
			}
		}
	}

	/**
	 * Try to load an instance of the RPCCallable class in parameter.
	 * Log an error if we can't loag this class.
	 * RPCCallable class must have a default constructor or a constructor whith a single WebSocketConnector parameter
	 * @param aClass
	 * @param aClassName
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void initRPCCallableClass(Class aClass, String aClassName) {
		if (aClass != null) {
			try {
				RPCCallable lInstance = null;
				try {
					Constructor lConstructor = aClass.getConstructor(WebSocketConnector.class);
					lInstance = (RPCCallable) lConstructor.newInstance(new Object[]{null});
				} catch (Exception ex) {
					lInstance = (RPCCallable) aClass.newInstance();
				}
				mRpcCallableClassLoader.put(aClassName, new RPCCallableClassLoader(aClass, lInstance));
			} catch (Exception ex) {
				mLog.error(ex.getClass().getSimpleName() + " creating '" + aClassName + "' instance : " + ex.getMessage()
						+ ". RPCCallable class must have a default constructor or a constructor whith a single WebSocketConnector parameter.");
			}
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		if (lType != null && getNamespace().equals(lNS)) {
			//Set the sourceId in the token.
			aToken.setString(CommonRpcPlugin.RRPC_KEY_SOURCE_ID, aConnector.getId());
			// remote procedure call
			if (lType.equals("rpc")) {
				rpc(aConnector, aToken);
				// reverse remote procedure call
			} else if (lType.equals("rrpc")) {
				rrpc(aConnector, aToken);
			}
		}
	}

	/**
	 * remote procedure call (RPC)
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void rpc(WebSocketConnector aConnector, Token aToken) {
		// check if user is allowed to run 'rpc' command
		if (!SecurityFactory.hasRight(
				getUsername(aConnector),
				CommonRpcPlugin.NS_RPC_DEFAULT + "." + CommonRpcPlugin.RPC_RIGHT_ID)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		Token lResponseToken = createResponse(aToken);

		String lClassName = aToken.getString(CommonRpcPlugin.RRPC_KEY_CLASSNAME);
		String lMethod = aToken.getString(CommonRpcPlugin.RRPC_KEY_METHOD);
		List lArgs = aToken.getList(CommonRpcPlugin.RRPC_KEY_ARGS);
		// if it's not a List, but just a simple arg
		if (lArgs == null) {
			Object lArg = aToken.getObject(CommonRpcPlugin.RRPC_KEY_ARGS);
			if (lArg != null) {
				lArgs = new FastList();
				lArgs.add(aToken.getObject(CommonRpcPlugin.RRPC_KEY_ARGS));
			} else {
				lArgs = null;
			}
		}
		String lMsg = null;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing RPC to class '"
					+ lClassName + "', method '"
					+ lMethod + "', args: '" + lArgs + "'...");
		}

		// class is ignored until security restrictions are finished.
		try {
			// The class we try to call is not loaded
			if (mRpcCallableClassLoader.containsKey(lClassName)) {
				// the called method name is unexisting
				if (mRpcCallableClassLoader.get(lClassName).hasMethod(lMethod)) {
					RPCCallableClassLoader lRpcClassLoader = mRpcCallableClassLoader.get(lClassName);
					// We get the instance of the generator
					RPCCallable lInstanceGenerator = lRpcClassLoader.getRpcCallableInstanceGenerator();
					// from this generator, we get an instance of the class we want to
					// call. This part is in the charge of the developper throw the
					// RPCCallable interface.
					RPCCallable lInstance = lInstanceGenerator.getInstance(aConnector);
					if (lInstance != null) {
						Object lObj = call(aConnector, lRpcClassLoader, lInstance, lMethod, lArgs);
						lResponseToken.setValidated("result", lObj);
					} else {
						lMsg = "Class '" + lClassName
								+ "' found but get a null instance when calling the RPCCallable getInstance() method.";
					}
				} else {
					lMsg = "Class '" + lClassName
							+ "' found but the method " + lMethod
							+ " is not available. Right is missing, probably a typo (call are case sensitive)";
				}
			} else {
				lMsg = "Class '" + lClassName
						+ "' not found in the jwebsocket.xml file, or not properly loaded. probably a typo.";
			}
		} catch (NoSuchMethodException ex) {
			lMsg = "NoSuchMethodException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
		} catch (IllegalAccessException ex) {
			lMsg = "IllegalAccessException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
		} catch (InvocationTargetException ex) {
			lMsg = "InvocationTargetException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
		} catch (RPCRightNotGrantedException ex) {
			lMsg = "RPCRightNotGrantedException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
		} catch (ClassNotFoundException ex) {
			lMsg = "ClassNotFoundException (the method does probably not exist or is not defined in the jwebsocket.xml file) calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
		}
		if (lMsg != null) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", lMsg);
		}

		/*
		 * just for testing purposes of multi-threaded rpc's try {
		 * Thread.sleep(3000); } catch (InterruptedException ex) { }
		 */

		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Do a rrpc call from a rrpc-ready-to-use token
	 * @param aConnectorFrom
	 * @param aToken rrpc-ready-to-use
	 */
	public void rrpc(WebSocketConnector aConnectorFrom, Token aToken) {
		try {
			new Rrpc(aToken).call();
		} catch (RrpcRightNotGrantedException e) {
			sendToken(aConnectorFrom, aConnectorFrom, createAccessDenied(aToken));
		}
	}

	/**
	 * reverse remote procedure call (RRPC)
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public static void processRrpc(WebSocketConnector aConnectorFrom, List<WebSocketConnector> aConnectorsTo, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'rrpc'...");
		}
		if (sRPCPlugIn == null) {
			mLog.error("Try to make a rrpc call but the RPCPlugin doesn't seem to be load." + "Please make sure the plugin is correctly added to jWebsocket");
		} else {
			// Send the rpc to every connector
			for (WebSocketConnector lConnector : aConnectorsTo) {
				if (lConnector != null) {
					sRPCPlugIn.sendToken(aConnectorFrom, lConnector, aToken);
				}
			}
		}
	}

	/**
	 *
	 * @param aClassName
	 * @param aURL
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class loadClass(String aClassName, String aURL) {
		Class lClass = null;
		try {
			URLClassLoader lUCL = new URLClassLoader(new URL[]{new URL(aURL)});
			// load class using previously defined class loader
			lClass = Class.forName(aClassName, true, lUCL);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Class '" + lClass.getName() + "' loaded!");
			}
		} catch (ClassNotFoundException ex) {
			mLog.error("Class not found exception: " + ex.getMessage());
		} catch (MalformedURLException ex) {
			mLog.error("MalformesURL exception: " + ex.getMessage());
		}
		return lClass;
	}

	/**
	 *
	 * @param aClass
	 * @param aArgs
	 * @return
	 */
//  public static Object createInstance(Class aClass, Object[] aArgs) {
//    Object lObj = null;
//    try {
//      Class[] lCA = new Class[aArgs != null ? aArgs.length : 0];
//      for (int i = 0; i < lCA.length; i++) {
//        lCA[i] = aArgs[i].getClass();
//      }
//      Constructor lConstructor = aClass.getConstructor(lCA);
//      lObj = lConstructor.newInstance(aArgs);
//      if (mLog.isDebugEnabled()) {
//        mLog.debug("Object '" + aClass.getName() + "' instantiated!");
//      }
//    } catch (Exception ex) {
//      mLog.error("Exception instantiating class " + aClass.getName() + ": " + ex.getMessage());
//    }
//    return lObj;
//  }
	/**
	 *
	 * @param aInstance
	 * @param aName
	 * @param aArgs
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws RPCRightNotGrantedException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	private Object call(WebSocketConnector aConnector, RPCCallableClassLoader aRpcClassLoader, Object aInstance, String aMethodName, List aArgs) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, RPCRightNotGrantedException, ClassNotFoundException {
		Class lClass = aRpcClassLoader.getRpcCallableClass();
		Object[] lArg = null;
		// JSONArray lJsonArrayArgs = null;
		Method lMethodToInvoke = null;
		List<MethodRightLink> lListMethod = aRpcClassLoader.getMethods(aMethodName);
		// aConnector.
		// We look if one of the method we have loaded match
		for (MethodRightLink lMethodRight : lListMethod) {
			Method lMethod = lMethodRight.getMethod();
			//We try to match each method against the parameter
			MethodMatcher lMethodMatcher = new ServerMethodMatcher(lMethod, aConnector);
			//If lArg is not null, means the method match
			if (lMethodMatcher.isMethodMatchingAgainstParameter(aArgs)) {
				//If the method match, we make sure the connector has the right to execute this method.
				if (SecurityFactory.hasRight(aConnector.getUsername(), lMethodRight.getRightId())) {
					lMethodToInvoke = lMethod;
					lArg = lMethodMatcher.getMethodParameters();
					break;
				}
				//otherwise, that's the correct method but the connector hasn't the correct right.
				throw new RPCRightNotGrantedException(lMethodRight.getRightId(), aArgs.toString());
			}
		}
		//If no method have been found.
		if (lMethodToInvoke == null) {
			throw new NoSuchMethodException();
		}

		//We cast the intance to the correct class.
		aInstance = lClass.cast(aInstance);
		Object lObj = lMethodToInvoke.invoke(aInstance, lArg);

		return lObj;
	}

	/**
	 * Check if a RPCCallable method has correct parameters type Store each class
	 * methods inside mClassMethods to grant a faster access for each rpc call.
	 * Log an error if on parameter is not valid Only called during the plugin
	 * initialization
	 *
	 * @param aClassName class name
	 * @param aMethodName method name
	 * @param aXmlParametersType list of parameter type found in the xml file (for instance: "int, string, map")
	 * @return true if the parameters are OK
	 */
	@SuppressWarnings("rawtypes")
	private Method getValidMethod(String aClassName, String aMethodName, String[] aXmlParametersType) {
		// check if the method own to a class which has been loaded.
		if (!mRpcCallableClassLoader.containsKey(aClassName)) {
			mLog.error("You try to grant access to a method which own to a"
					+ " class the server can't initialize."
					+ " Make sure you didn't forget to declare it's jar file. "
					+ aMethodName
					+ " will *not* be loaded");
			return null;
		}
		// Check if 2 methods have the same name and the same number of arguments
		// (this block just log an error if 2 method have the same number of
		// parameters without specific types)
		Class lClass = mRpcCallableClassLoader.get(aClassName).getRpcCallableClass();
		Method[] lMethods = lClass.getMethods();
		ArrayList<Integer> lMethodWithSameNameAndNumberOfArguments = new ArrayList<Integer>();
		if (aXmlParametersType == null) {
			for (int i = 0; i < lMethods.length; i++) {
				Method lMethod = lMethods[i];
				if (lMethod.getName().equals(aMethodName)) {
					if (lMethodWithSameNameAndNumberOfArguments.contains(lMethod.getParameterTypes().length)) {
						// 2 methods have the same name and number of parameters
						if (mLog.isDebugEnabled()) {
							mLog.debug("Two methods named " + aMethodName
									+ " have the same number of argument. Can't know which one this setting concerns. Please use xml settings such as: MyClass.myMethod(int, boolean, string, double, map, array)");
						}
						return null;
					} else {
						lMethodWithSameNameAndNumberOfArguments.add(lMethod.getParameterTypes().length);
					}
				}
			}
		}
//    if (aXmlParametersType != null && aXmlParametersType.length == 1 && "".equals(aXmlParametersType[0])) {
//    	aXmlParametersType =null;
//    }
		// Make sure every parameter type is valid
		if (aXmlParametersType != null) {
			for (String parameterType : aXmlParametersType) {
				if (!"".equals(parameterType) && !TypeConverter.isValidProtocolType(parameterType)) {
					mLog.error(aXmlParametersType
							+ " is not a valid parameter type. Valid parameter types are: "
							+ TypeConverter.getValidParameterTypes());
					return null;
				}
			}
		}
		// Check if on of the method match
		for (int i = 0; i < lMethods.length; i++) {
			// If we are on a method with the same name, we check it's parameters
			if (lMethods[i].getName().equals(aMethodName)) {
				if (checkMethodParameters(lMethods[i], aXmlParametersType, aClassName)) {
					return lMethods[i];
				}
			}
		}
		mLog.error("The method " + aMethodName + " could not be loaded. " + "Probably a typo or invalid parameter (check previous error).");
		return null;
	}

	/**
	 * Check if the method aMethod match with aXmlParametersType.
	 *
	 * @param aMethod
	 * @param aXmlParametersType  list of parameter type found in the xml file (for instance: "int, string, map")
	 * @param aClassName
	 * @return true if the method match, false otherwise
	 */
	@SuppressWarnings("rawtypes")
	private boolean checkMethodParameters(Method aMethod, String[] aXmlParametersType, String aClassName) {
		Class[] lRealParametersType = aMethod.getParameterTypes();

		List<Class> lParametersType = new FastList<Class>();
		for (Class lClass : lRealParametersType) {
			if (lClass != null && lClass != WebSocketConnector.class) {
				lParametersType.add(lClass);
			}
		}

		// no parameters
		if (aXmlParametersType != null
				&& aXmlParametersType.length == 1
				&& "".equals(aXmlParametersType[0])) {
			if (lParametersType.isEmpty()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Method " + aMethod.getName()
							+ " loaded (expect 0 parameters).");
				}
				return true;
			} else {
				return false;
			}
		}

		// Look for a method with the same arguments if they are defined in the xml
		// setting...
		if (aXmlParametersType != null) {
			//If it's not the same number of parameters as expected, that's not the correct method.
			if (aXmlParametersType.length != lParametersType.size()) {
				return false;
			}
			boolean methodMatch = true;
			for (int j = 0; j < aXmlParametersType.length; j++) {
				if (!TypeConverter.matchProtocolTypeToJavaType(
						aXmlParametersType[j], lParametersType.get(j).getName())) {
					methodMatch = false;
					break;
				}
			}
			if (methodMatch) {
				if (mLog.isDebugEnabled()) {
					StringBuilder lParametersList = new StringBuilder();
					for (int k = 0; k < aXmlParametersType.length; k++) {
						lParametersList.append(aXmlParametersType[k] + ", ");
					}
					lParametersList.setLength(lParametersList.length() - 2);
					if (mLog.isDebugEnabled()) {
						mLog.debug("Method '" + aMethod.getName()
								+ "' loaded (expecting " + lParametersType.size()
								+ " explicit parameters: " + lParametersList.toString() + ").");
					}
				}
				return true;
			}
			return false;
		}
		// without parameters, always true
		if (lParametersType.isEmpty()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Method '" + aMethod.getName() + "' with no parameters loaded.");
			}
			return true;
		}

		// if parameters are not defined in the setting, means that we have a unique
		// method with this name.
		for (int j = 0; j < lParametersType.size(); j++) {
			Class lParameterType = lParametersType.get(j);
			if (!TypeConverter.isValidProtocolJavaType(lParameterType)) {
				mLog.error("The method " + aMethod.getName()
						+ " has an invalid parameter: "
						+ lParameterType.getName() + ". "
						+ "This method will *not* be loaded. "
						+ "Supported parameters type are primitive, primitive's wrapper, List and Token.");
				return false;
			}
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Method '" + aMethod.getName()
					+ "' loaded (expecting " + lParametersType.size()
					+ " explicit parameters).");
		}
		// store the "complex" method in the Map.
		return true;
	}

	/**
	 * Alert all the RpcCallableInstance that a connecter has stopped
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// We alert every instance of the generator that a connector stopped its
		// connection
		for (Entry<String, RPCCallableClassLoader> entry : mRpcCallableClassLoader.entrySet()) {
			entry.getValue().getRpcCallableInstanceGenerator().connectorStopped(aConnector, aCloseReason);
		}
	}

	/**
	 * call the getConnector method of the instance of the server of the RpcPlugin
	 * loaded
	 *
	 * @param aEngine
	 * @param aConnectorId
	 * @return
	 */
	public static WebSocketConnector getConnector(String aEngine, String aConnectorId) {
		if (sRPCPlugIn == null) {
			mLog.error("Try to make a rrpc call but the RPCPlugin doesn't seem to be load." + "Please make sure the plugin is correctly added to jWebsocket");
			return null;
		} else {
			return sRPCPlugIn.getServer().getConnector(aEngine, aConnectorId);
		}
	}

	/**
	 * call the getUsername method of the instance of the RpcPlugin
	 *
	 * @param aConnector
	 * @return
	 */
	public static String getUsernameStatic(WebSocketConnector aConnector) {
		if (sRPCPlugIn == null) {
			mLog.error("Try to make a rrpc call but the RPCPlugin doesn't seem to be load." + "Please make sure the plugin is correctly added to jWebsocket");
			return null;
		} else {
			return sRPCPlugIn.getUsername(aConnector);
		}
	}
}
