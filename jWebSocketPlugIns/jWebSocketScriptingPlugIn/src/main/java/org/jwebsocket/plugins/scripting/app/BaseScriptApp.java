//	---------------------------------------------------------------------------
//	jWebSocket - BaseScriptApp for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import java.io.File;
import java.security.Permissions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.jms.Connection;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IChunkable;
import org.jwebsocket.api.IChunkableDeliveryListener;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.LocalLoader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.scripting.ScriptingPlugIn;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.Assert;

/**
 * The class represents the global "App" object into script applications.
 * Contains abstract methods to be extended by concrete implementations for
 * custom languages like JavaScript or Groovy.
 *
 * @author Rolando Santamaria Maso
 */
abstract public class BaseScriptApp {

	/**
	 *
	 */
	protected ScriptingPlugIn mPlugIn;
	private final String mAppName;
	private final String mAppPath;
	private final ScriptEngine mScriptApp;
	private final Map<String, List<Object>> mCallbacks = new FastMap<String, List<Object>>().shared();
	private final ScriptAppLogger mLogger;
	private final Logger mLog = Logging.getLogger();
	private final Map<String, Object> mApi = new FastMap<String, Object>().shared();
	private final GenericApplicationContext mBeanFactory = new GenericApplicationContext(new DefaultListableBeanFactory());
	private final ServerClient mServerClient;
	private final LocalLoader mClassLoader;
	private final String mJWSHome;
	private final String mNodeId;
	/**
	 * String value for the "Connector Started" event. The event is fired when a
	 * client started a connection with the server.
	 */
	public final static String EVENT_CONNECTOR_STARTED = "connectorStarted";
	/**
	 * String value for the "Connector Stopped" event. The event is fired when a
	 * client stopped a connection with the server.
	 */
	public final static String EVENT_CONNECTOR_STOPPED = "connectorStopped";
	/**
	 * String value for the "Engine Started" event. The event is fired when a
	 * jWebSocket engine has started.
	 */
	public final static String EVENT_ENGINE_STARTED = "engineStarted";
	/**
	 * String value for the "Engine Stopped" event. The event is fired when a
	 * jWebSocket engine has stopped.
	 */
	public final static String EVENT_ENGINE_STOPPED = "engineStopped";
	/**
	 * String value for the "Session Started" event. The event is fired when a
	 * new client session is created on the server.
	 */
	public final static String EVENT_SESSION_STARTED = "sessionStarted";
	/**
	 * String value for the "Session Stopped" event. The event is fired when an
	 * existing client session is stopped(destroyed) on the server.
	 */
	public final static String EVENT_SESSION_STOPPED = "sessionStopped";
	/**
	 * String value for the "Logon" event. The event is fired when a client
	 * logon.
	 */
	public final static String EVENT_LOGON = "logon";
	/**
	 * String value for the "Logoff" event. The event is fired when a client
	 * logoff.
	 */
	public final static String EVENT_LOGOFF = "logoff";
	/**
	 * String value for the "Token" event. The event is fired when a token is
	 * received from the client to the script application.
	 */
	public final static String EVENT_TOKEN = "token";
	/**
	 * String value for the "Filter In" event. The event is fired before client
	 * tokens arrive to the application controller layer. It is designed to
	 * allow pre-filtering.
	 */
	public final static String EVENT_FILTER_IN = "filterIn";
	/**
	 * String value for the "Filter Out" event. The event is fired before
	 * application tokens are sent to the client. It is designed to allow
	 * post-filtering.
	 */
	public final static String EVENT_FILTER_OUT = "filterOut";
	/**
	 * String value for the "System Starting" event. The event is fired when the
	 * jWebSocket server system is starting.
	 */
	public final static String EVENT_SYSTEM_STARTING = "systemStarting";
	/**
	 * String value for the "System Started" event. The event is fired when the
	 * jWebSocket server system has started.
	 */
	public final static String EVENT_SYSTEM_STARTED = "systemStarted";
	/**
	 * String value for the "System Started" event. The event is fired when the
	 * jWebSocket server system is stopping.
	 */
	public final static String EVENT_SYSTEM_STOPPING = "systemStopping";
	/**
	 * String value for the "System Started" event. The event is fired when the
	 * jWebSocket server system has stopped.
	 */
	public final static String EVENT_SYSTEM_STOPPED = "systemStopped";
	/**
	 * String value for the "Before App Reload" event. The event is fired before
	 * reaload an script application. Allows to applications to perform
	 * operations before to be reloaded.
	 */
	public final static String EVENT_BEFORE_APP_RELOAD = "beforeAppReload";
	/**
	 * String value for the "App Loaded" event. The event is fired when an
	 * script app has been successfully loaded.
	 */
	public final static String EVENT_APP_LOADED = "appLoaded";
	/**
	 * String value for the "Undeploying" event. The event is fired when an
	 * script app is going to be undeployed.
	 */
	public final static String EVENT_UNDEPLOYING = "undeploying";

	/**
	 * Gets the ScriptEngine instance associated to the script application.
	 *
	 * @return
	 */
	protected ScriptEngine getScriptApp() {
		return mScriptApp;
	}

	/**
	 * Gets the jWebSocket server node id
	 *
	 * @return
	 */
	public String getNodeId() {
		return mNodeId;
	}

	/**
	 * Gets the script application events callbacks collection.
	 *
	 * @return
	 */
	protected Map<String, List<Object>> getCallbacks() {
		return mCallbacks;
	}

	/**
	 * Gets the script app server internal client instance.
	 *
	 * @return
	 */
	public ServerClient getServerClient() {
		return mServerClient;
	}

	/**
	 * Constructor
	 *
	 * @param aPlugIn The ScriptingPlugIn reference that allows to script
	 * applications to get access to the TokenServer instance.
	 * @param aAppName The application name (unique value)
	 * @param aAppPath The application directory path
	 * @param aScriptApp The scripting engine that runs the application
	 * @param aClassLoader
	 */
	public BaseScriptApp(ScriptingPlugIn aPlugIn, String aAppName, String aAppPath, ScriptEngine aScriptApp, LocalLoader aClassLoader) {
		mPlugIn = aPlugIn;
		mAppName = aAppName;
		mAppPath = aAppPath;
		mScriptApp = aScriptApp;
		mLogger = new ScriptAppLogger(mLog, aAppName);
		mServerClient = new ServerClient(this);
		mClassLoader = aClassLoader;
		mNodeId = JWebSocketConfig.getConfig().getNodeId();

		// registering global "AppUtils" resource
		aScriptApp.put("AppUtils", this);

		mJWSHome = JWebSocketConfig.getJWebSocketHome();
	}

	/**
	 * Returns the Script App programming language extension.
	 *
	 * @return
	 */
	abstract public String getScriptLanguageExt();

	/**
	 * Gets the ScriptEngine instance associated to the script application.
	 *
	 * @return
	 */
	public ScriptEngine getEngine() {
		return mScriptApp;
	}

	/**
	 *
	 * @return
	 */
	public Permissions getPermissions() {
		return mPlugIn.getAppPermissions(mAppName, mAppPath);
	}

	/**
	 * Notify an event into the script application.
	 *
	 * @param aEventName The event name
	 * @param aArgs The event arguments
	 */
	abstract public void notifyEvent(String aEventName, Object[] aArgs);

	/**
	 * Get the application name
	 *
	 * @return
	 */
	public String getName() {
		return mAppName;
	}

	/**
	 * Load a Jar into the script app class loader
	 *
	 * @param aFile
	 * @return TRUE if the jar has beeen loaded, FALSE otherwise
	 * @throws java.lang.Exception
	 */
	public boolean loadJar(String aFile) throws Exception {
		aFile = aFile.replace("${APP_HOME}", mAppPath);
		aFile = aFile.replace("${EXT}", mPlugIn.getExtensionsDirectoryPath()
				+ File.separator);

		return mClassLoader.loadJar(aFile);
	}

	/**
	 * Publish and object to be accesed from the client.
	 *
	 * @param aObjectId The object identifier
	 * @param aObject The object to be published
	 */
	public void publish(String aObjectId, Object aObject) {
		mApi.put(aObjectId, aObject);
	}

	/**
	 * Unpublish an object giving it identifier.
	 *
	 * @param aObjectId
	 */
	public void unpublish(String aObjectId) {
		mApi.remove(aObjectId);
	}

	/**
	 * Return TRUE if an object with the giving object identifier is published,
	 * FALSE otherwise.
	 *
	 * @param aObjectId
	 * @return
	 */
	public boolean isPublished(String aObjectId) {
		return mApi.containsKey(aObjectId);
	}

	/**
	 * Get a published object giving the object identifier.
	 *
	 * @param aObjectId
	 * @return
	 */
	public Object getPublished(String aObjectId) {
		return mApi.get(aObjectId);
	}

	/**
	 * Get the script application root directory path.
	 *
	 * @return
	 */
	public String getPath() {
		return mAppPath;
	}

	/**
	 * Get the script application logger.
	 *
	 * @return
	 */
	public ScriptAppLogger getLogger() {
		return mLogger;
	}

	/**
	 * Raise an exception with the given message if the boolean expression is
	 * FALSE.
	 *
	 * @param aExpression Boolean expression
	 * @param aMessage The exception message
	 */
	public void assertTrue(Boolean aExpression, String aMessage) {
		Assert.isTrue(aExpression, aMessage);
	}

	/**
	 * Raise an exception with the given message if the Object argument is NULL.
	 *
	 * @param aObject The object
	 * @param aMessage The exception message.
	 */
	public void assertNotNull(Object aObject, String aMessage) {
		Assert.notNull(aObject, aMessage);
	}

	/**
	 * Import a JavaScript file into the application.
	 *
	 * @param aFile The JavaScript file path. The string "${APP_HOME}" is
	 * replaced by the application root directory path.
	 * @throws Exception
	 */
	public void importScript(String aFile) throws Exception {
		aFile = aFile.replace("${APP_HOME}", mAppPath);
		aFile = aFile.replace("${EXT}", mPlugIn.getExtensionsDirectoryPath()
				+ File.separator + getScriptLanguageExt() + File.separator);

		// add the script extension (example: .js)
		String lScriptPath = aFile + "." + getScriptLanguageExt();
		eval(lScriptPath);
	}

	/**
	 * Evaluate an script file into the app script engine.
	 *
	 * @param aScriptFilePath The script file path to evaluate.
	 * @return
	 * @throws Exception
	 */
	public abstract Object eval(String aScriptFilePath) throws Exception;

	/**
	 * Send a token to a given connector.
	 *
	 * @param aConnector The connector
	 * @param aMap The Map representation of the Token
	 * @param aFragmentSize The fragment size used to fragment the token. If
	 * value is null, the token is not fragmented.
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap, Integer aFragmentSize) {
		// outbound filtering
		notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aMap, aConnector});

		if (null != aFragmentSize) {
			mPlugIn.sendTokenFragmented(aConnector, toToken(aMap), aFragmentSize);
		} else {
			mPlugIn.sendToken(aConnector, toToken(aMap));
		}
	}

	/**
	 * Send a token to a given connector.
	 *
	 * @param aConnectorId
	 * @param aMap The Map representation of the Token
	 * @param aFragmentSize The fragment size used to fragment the token. If
	 * value is null, the token is not fragmented.
	 */
	public void sendToken(String aConnectorId, Map aMap, Integer aFragmentSize) {
		sendToken(mPlugIn.getServer().getConnector(aConnectorId), aMap, aFragmentSize);
	}

	/**
	 * Send a token to a given connector.
	 *
	 * @param aConnector The connector
	 * @param aMap The Map representation of the Token
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap) {
		sendToken(aConnector, aMap, null);
	}

	/**
	 * Send a token to a given connector with acknowledge callbacks.
	 *
	 * @param aConnector The connector
	 * @param aMap The Map representation of the Token
	 * @param aListener The IPacketDeliveryListener implementation callback.
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap, Object aListener) {
		// outbound filtering
		notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aMap, aConnector});

		mPlugIn.sendTokenInTransaction(aConnector, toToken(aMap),
				(IPacketDeliveryListener) cast(aListener, IPacketDeliveryListener.class));
	}

	/**
	 * Send a token to a given connector with acknowledge callbacks.
	 *
	 * @param aConnectorId
	 * @param aMap The Map representation of the Token
	 * @param aListener The IPacketDeliveryListener implementation callback.
	 */
	public void sendToken(String aConnectorId, Map aMap, Object aListener) {
		sendToken(mPlugIn.getServer().getConnector(aConnectorId), aMap, aListener);
	}

	/**
	 * Send a fragmented token to a given connector with acknowledge callbacks.
	 *
	 * @param aConnector The connector
	 * @param aMap The Map representation of the Token
	 * @param aFragmentSize The fragmentation size
	 * @param aListener The IPacketDeliveryListener implementation callback.
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap, Integer aFragmentSize, Object aListener) {
		// outbound filtering
		notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aMap, aConnector});

		mPlugIn.sendTokenInTransaction(aConnector, toToken(aMap), aFragmentSize,
				(IPacketDeliveryListener) cast(aListener, IPacketDeliveryListener.class));
	}

	/**
	 * Send a fragmented token to a given connector with acknowledge callbacks.
	 *
	 * @param aConnectorId
	 * @param aMap The Map representation of the Token
	 * @param aFragmentSize The fragmentation size
	 * @param aListener The IPacketDeliveryListener implementation callback.
	 */
	public void sendToken(String aConnectorId, Map aMap, Integer aFragmentSize, Object aListener) {
		sendToken(mPlugIn.getServer().getConnector(aConnectorId), aMap, aFragmentSize, aListener);
	}

	/**
	 * Send a token to a given connector.
	 *
	 * @param aConnectorId
	 * @param aMap The Map representation of the Token
	 */
	public void sendToken(String aConnectorId, Map aMap) {
		sendToken(mPlugIn.getServer().getConnector(aConnectorId), aMap);
	}

	/**
	 * Send a chunkable object to a given connector with acknowledge callbacks.
	 *
	 * @param aConnectorId
	 * @param aChunkable The IChunkable implementation object
	 */
	public void sendChunkable(String aConnectorId, Object aChunkable) {
		sendChunkable(mPlugIn.getServer().getConnector(aConnectorId), aChunkable);
	}

	/**
	 * Send a chunkable object to a given connector with acknowledge callbacks.
	 *
	 * @param aConnectorId
	 * @param aChunkable The IChunkable implementation object
	 * @param aListener The IChunkableDeliveryListener implementation callback.
	 */
	public void sendChunkable(String aConnectorId, Object aChunkable, Object aListener) {
		sendChunkable(mPlugIn.getServer().getConnector(aConnectorId), aChunkable, aListener);
	}

	/**
	 * Send a chunkable object to a given connector with acknowledge callbacks.
	 *
	 * @param aConnector The connector
	 * @param aChunkable The IChunkable implementation object
	 */
	public void sendChunkable(WebSocketConnector aConnector, Object aChunkable) {
		mPlugIn.sendChunkable(aConnector, (IChunkable) cast(aChunkable, IChunkable.class));
	}

	/**
	 * Send a chunkable object to a given connector with acknowledge callbacks.
	 *
	 * @param aConnector The connector
	 * @param aChunkable The IChunkable implementation object
	 * @param aListener The IChunkableDeliveryListener implementation callback.
	 */
	public void sendChunkable(WebSocketConnector aConnector, Object aChunkable, Object aListener) {
		mPlugIn.sendChunkable(aConnector, (IChunkable) cast(aChunkable, IChunkable.class),
				(IChunkableDeliveryListener) cast(aListener, IChunkableDeliveryListener.class));
	}

	/**
	 * Cast JavaScript context objects into Java objects
	 *
	 * @param aObject The object to be casted
	 * @param aClass The casting object class
	 * @return
	 */
	public Object cast(Object aObject, Class aClass) {
		if (aObject.getClass().equals(aClass)) {
			return aObject;
		}

		try {
			// trying to cast first
			return aClass.cast(aObject);
		} catch (Exception lEx) {
		}

		// extracting interface
		return ((Invocable) mScriptApp)
				.getInterface(aObject, aClass);
	}

	/**
	 * Get all server connectors (clients).
	 *
	 * @return
	 */
	public Collection<WebSocketConnector> getAllConnectors() {
		return mPlugIn.getServer().selectTokenConnectors().values();
	}

	/**
	 * Return TRUE if the given connector has the given authority, FALSE
	 * otherwise.
	 *
	 * @param aConnector The connector
	 * @param aAuthority The authority
	 * @return
	 */
	public boolean hasAuthority(WebSocketConnector aConnector, String aAuthority) {
		return mPlugIn.hasAuthority(aConnector, aAuthority);
	}

	/**
	 * Raise an exception if the client does not have the given authority.
	 *
	 * @param aConnector The connector
	 * @param aAuthority The authority
	 * @throws Exception
	 */
	public void requireAuthority(WebSocketConnector aConnector, String aAuthority) throws Exception {
		if (!mPlugIn.hasAuthority(aConnector, aAuthority)) {
			throw new Exception("Not authorized. Missing required '" + aAuthority + "' authority!");
		}
	}

	/**
	 * Raise an exception if the client is not authenticated.
	 *
	 * @param aConnector The connector
	 * @throws Exception
	 */
	public void requireAuthenticated(WebSocketConnector aConnector) throws Exception {
		if (!aConnector.getSession().isAuthenticated()) {
			throw new Exception("Not authorized!");
		}
	}

	/**
	 * Register a callback for a collection of events.
	 *
	 * @param aEvents The collection of events
	 * @param aFn The callback
	 */
	public void on(Collection<String> aEvents, Object aFn) {
		for (String lEventName : aEvents) {
			on(lEventName, aFn);
		}
	}

	/**
	 * Register a callback for a certain event.
	 *
	 * @param aEventName The event name
	 * @param aFn The callback
	 */
	public void on(String aEventName, Object aFn) {
		if (!mCallbacks.containsKey(aEventName)) {
			mCallbacks.put(aEventName, new FastList<Object>());
		}
		mCallbacks.get(aEventName).add(aFn);
	}

	/**
	 * Unregister a callback for a certain event.
	 *
	 * @param aEventName The event name
	 * @param aFn The callback object
	 */
	public void un(String aEventName, Object aFn) {
		if (mCallbacks.containsKey(aEventName)) {
			mCallbacks.get(aEventName).remove(aFn);
		}
	}

	/**
	 * Create a Token object from a Map instance.
	 *
	 * @param aMap The Map instance
	 * @return
	 */
	public Token toToken(Map aMap) {
		return TokenFactory.createToken(aMap);
	}

	/**
	 * Create a response Token from a received Token.
	 *
	 * @param aInToken The incoming Token.
	 * @return The response Token
	 */
	public Map createResponse(Map aInToken) {
		return mPlugIn.createResponse(toToken(aInToken)).getMap();
	}

	/**
	 * Broadcast a Token to a given collection of connectors.
	 *
	 * @param aConnectors The collection of connectors
	 * @param aToken The Token to be broascasted
	 */
	public void broadcast(Collection<WebSocketConnector> aConnectors, Map aToken) {
		for (WebSocketConnector aConnector : aConnectors) {
			sendToken(aConnector, aToken);
		}
	}

	/**
	 * Broadcast a Token to a given collection of connectors.
	 *
	 * @param aToken The Token to be broascasted
	 */
	public void broadcast(Map aToken) {
		mPlugIn.broadcastToken(null, toToken(aToken));
	}

	/**
	 * Create a new instance of a thread-safe Map.
	 *
	 * @return The thread-safe Map instance.
	 */
	public Map newThreadSafeMap() {
		return new FastMap().shared();
	}

	/**
	 * Create a new instance of a thread-safe Collection.
	 *
	 * @return The thread-safe collection instance.
	 */
	public Collection newThreadSafeCollection() {
		return new FastList().shared();
	}

	/**
	 * Call a method on an application published object.
	 *
	 * @param aObjectId The published object identifier.
	 * @param aMethod The method to be called.
	 * @param aArgs The method calling arguments.
	 * @return The result of the method execution.
	 * @throws Exception
	 */
	public abstract Object callMethod(String aObjectId, String aMethod, Collection aArgs) throws Exception;

	/**
	 * Get the Spring bean factory instance associated to the given namespace.
	 * If 'namespace' argument is null, then the system core bean factory is
	 * returned. The system core bean factory is the parent of name-spaced bean
	 * factories, so system core beans are visible for every namespaced bean
	 * factory.
	 *
	 * @param aNamespace
	 * @return
	 */
	private GenericApplicationContext getBeanFactory(String aNamespace) {
		if (null == aNamespace) {
			return JWebSocketBeanFactory.getInstance();
		} else {
			return JWebSocketBeanFactory.getInstance(aNamespace);
		}
	}

	/**
	 * Get a bean from the system-core bean factory.
	 *
	 * @param aBeanId The bean identifier
	 * @return
	 */
	public Object getBean(String aBeanId) {
		return getBean(aBeanId, null);
	}

	/**
	 * Get a bean from the application bean factory.
	 *
	 * @param aBeanId
	 * @return
	 */
	public Object getAppBean(String aBeanId) {
		return mBeanFactory.getBean(aBeanId);
	}

	/**
	 * Get a bean from a target bean factory.
	 *
	 * @param aBeanId The bean identifier
	 * @param aNamespace The target bean factory namespace
	 * @return
	 */
	public Object getBean(String aBeanId, String aNamespace) {
		String lBeanPath = (null != aNamespace)
				? aNamespace + ":" + aBeanId
				: aBeanId;

		// checking permission for bean access
		mPlugIn.checkWhiteListedBean(mAppName, lBeanPath);

		return getBeanFactory(aNamespace).getBean(aBeanId);
	}

	/**
	 * Get the application bean factory.
	 *
	 * @return
	 */
	public GenericApplicationContext getAppBeanFactory() {
		return mBeanFactory;
	}

	/**
	 * Load an Spring IOC XML configuration file into the script application
	 * bean factory.
	 *
	 * @param aFile
	 * @throws Exception
	 */
	public void loadToAppBeanFactory(String aFile) throws Exception {
		// beans definitions file
		aFile = aFile.replace("${APP_HOME}", mAppPath);

		// creating the XML definitions reader
		XmlBeanDefinitionReader lXmlReader = new XmlBeanDefinitionReader(mBeanFactory);
		lXmlReader.setBeanClassLoader(mClassLoader);

		// path for dtd and xsd files location
		String lBeansDef = FileUtils.readFileToString(new File(aFile));
		lBeansDef = lBeansDef.replace("${JWEBSOCKET_HOME}", mJWSHome);
		lBeansDef = lBeansDef.replace("${APP_HOME}", mAppPath);

		// loading XML definitions file into app bean factory
		lXmlReader.loadBeanDefinitions(new ByteArrayResource(lBeansDef.getBytes()));
	}

	/**
	 * Get a JMS Manager instance using default configuration.
	 *
	 * @return
	 * @throws java.lang.Exception
	 */
	public JMSManagerAbstraction getJMSManager() throws Exception {
		JMSManagerAbstraction lAbstraction = new JMSManagerAbstraction(this);
		lAbstraction.setDefaultDestination(mPlugIn.getServer().getJMSManager().getDefaultDestination());

		return lAbstraction;
	}

	/**
	 * Get a JMS Manager instance indicating transaction support.
	 *
	 * @param aUseTransaction If TRUE, JMS session instance will support
	 * transactions.
	 * @return
	 * @throws java.lang.Exception
	 */
	public JMSManagerAbstraction getJMSManager(boolean aUseTransaction) throws Exception {
		JMSManagerAbstraction lAbstraction = new JMSManagerAbstraction(this, aUseTransaction);
		lAbstraction.setDefaultDestination(mPlugIn.getServer().getJMSManager().getDefaultDestination());

		return lAbstraction;
	}

	/**
	 * Get a JMS Manager instance indicating transaction support setting and
	 * passing the JMS connection instance.
	 *
	 * @param aUseTransaction If TRUE, JMS session instance will support
	 * transactions.
	 * @param aConn The JMS connection instance to be used.
	 * @return
	 * @throws java.lang.Exception
	 */
	public JMSManagerAbstraction getJMSManager(boolean aUseTransaction, Connection aConn) throws Exception {
		JMSManagerAbstraction lAbstraction = new JMSManagerAbstraction(this, aUseTransaction, aConn);
		lAbstraction.setDefaultDestination(mPlugIn.getServer().getJMSManager().getDefaultDestination());

		return lAbstraction;
	}

	/**
	 * Get a JMS Manager instance indicating transaction support setting and
	 * passing the JMS connection instance.
	 *
	 * @param aUseTransaction If TRUE, JMS session instance will support
	 * transactions.
	 * @param aConn The JMS connection instance to be used.
	 * @param aDefaultDestination The default destination
	 * @return
	 */
	public JMSManagerAbstraction getJMSManager(boolean aUseTransaction, Connection aConn,
			String aDefaultDestination) {
		JMSManagerAbstraction lAbstraction = new JMSManagerAbstraction(this, aUseTransaction,
				aConn, aDefaultDestination);

		return lAbstraction;
	}

	/**
	 * Get the application version.
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract String getVersion() throws Exception;

	/**
	 * Get the application description.
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract String getDescription() throws Exception;

	/**
	 * Get the application client API
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract Map getClientAPI() throws Exception;

	/**
	 * Get system property. May require sandbox permission.
	 *
	 * @param aPropertyName
	 * @return
	 */
	public String getSystemProperty(String aPropertyName) {
		return System.getProperty(aPropertyName);
	}

	/**
	 * Set a system property. May require sandbox permission.
	 *
	 * @param aPropertyName
	 * @param aValue
	 * @return
	 */
	public String setSystemProperty(String aPropertyName, String aValue) {
		return System.setProperty(aPropertyName, aValue);
	}

	/**
	 * Get the WebSocketServer instance that holds the scripting plug-in
	 *
	 * @return
	 */
	public WebSocketServer getWebSocketServer() {
		return mPlugIn.getServer();
	}

	/**
	 * Call invoke method on a target plug-in
	 *
	 * @param aPlugInId
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public Token invokePlugIn(String aPlugInId, WebSocketConnector aConnector, Map aToken) {
		return mPlugIn.invokePlugIn(aPlugInId, aConnector, toToken(aToken));
	}

	/**
	 * Call invoke method on a target plug-in
	 *
	 * @param aPlugInId
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public Token invokePlugIn(String aPlugInId, WebSocketConnector aConnector, Token aToken) {
		return mPlugIn.invokePlugIn(aPlugInId, aConnector, aToken);
	}

	/**
	 * Return TRUE if the jWebSocket cluster is active, FALSE otherwise
	 *
	 * @return
	 */
	public boolean isClusterEnabled() {
		return mPlugIn.isClusterEnabled();
	}
}
