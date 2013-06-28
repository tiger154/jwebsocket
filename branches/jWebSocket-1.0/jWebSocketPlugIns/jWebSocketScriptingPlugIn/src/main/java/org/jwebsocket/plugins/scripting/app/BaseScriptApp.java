//	---------------------------------------------------------------------------
//	jWebSocket - BaseScriptApp for Scripting Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.scripting.ScriptingPlugIn;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;

/**
 * Class suppose to be extended by concrete script applications types.
 *
 * @author kyberneees
 */
abstract public class BaseScriptApp {

	private ScriptingPlugIn mServer;
	private String mAppName;
	private String mAppPath;
	private ScriptEngine mScriptApp;
	private Map<String, List<Object>> mCallbacks = new FastMap<String, List<Object>>().shared();
	private ScriptAppLogger mLogger;
	private Logger mLog = Logging.getLogger();
	private Map<String, Object> mApi = new FastMap<String, Object>().shared();
	/**
	 *
	 */
	public final static String EVENT_CONNECTOR_STARTED = "connectorStarted";
	/**
	 *
	 */
	public final static String EVENT_CONNECTOR_STOPPED = "connectorStopped";
	/**
	 *
	 */
	public final static String EVENT_ENGINE_STARTED = "engineStarted";
	/**
	 *
	 */
	public final static String EVENT_ENGINE_STOPPED = "engineStopped";
	/**
	 *
	 */
	public final static String EVENT_SESSION_STARTED = "sessionStarted";
	/**
	 *
	 */
	public final static String EVENT_SESSION_STOPPED = "sessionStopped";
	/**
	 *
	 */
	public final static String EVENT_LOGON = "logon";
	/**
	 *
	 */
	public final static String EVENT_LOGOFF = "logoff";
	/**
	 *
	 */
	public final static String EVENT_TOKEN = "token";
	/**
	 *
	 */
	public final static String EVENT_FILTER_IN = "filterIn";
	/**
	 *
	 */
	public final static String EVENT_FILTER_OUT = "filterOut";
	/**
	 *
	 */
	public final static String EVENT_SYSTEM_STARTING = "systemStarting";
	/**
	 *
	 */
	public final static String EVENT_SYSTEM_STARTED = "systemStarted";
	/**
	 *
	 */
	public final static String EVENT_SYSTEM_STOPPING = "systemStopping";
	/**
	 *
	 */
	public final static String EVENT_SYSTEM_STOPPED = "systemStopped";
	/**
	 *
	 */
	public final static String EVENT_BEFORE_APP_RELOAD = "beforeAppReload";
	/**
	 *
	 */
	public final static String EVENT_APP_LOADED = "appLoaded";
	/**
	 *
	 */
	public final static String EVENT_UNDEPLOYING = "undeploying";

	/**
	 *
	 * @return
	 */
	protected ScriptEngine getScriptApp() {
		return mScriptApp;
	}

	/**
	 *
	 * @return
	 */
	protected Map<String, List<Object>> getCallbacks() {
		return mCallbacks;
	}

	/**
	 *
	 * @param aServer
	 * @param aAppName
	 * @param aAppPath
	 * @param aScriptApp
	 */
	public BaseScriptApp(ScriptingPlugIn aServer, String aAppName, String aAppPath, ScriptEngine aScriptApp) {
		mServer = aServer;
		mAppName = aAppName;
		mAppPath = aAppPath;
		mScriptApp = aScriptApp;
		mLogger = new ScriptAppLogger(mLog, aAppName);

		// registering global "AppUtils" resource
		aScriptApp.put("AppUtils", this);
	}

	/**
	 *
	 * @return
	 */
	public ScriptEngine getEngine() {
		return mScriptApp;
	}

	/**
	 *
	 * @param aEventName
	 * @param aArgs
	 */
	abstract public void notifyEvent(String aEventName, Object[] aArgs);

	/**
	 *
	 * @return
	 */
	public String getName() {
		return mAppName;
	}

	/**
	 *
	 * @param aObjectId
	 * @param aObject
	 */
	public void publish(String aObjectId, Object aObject) {
		mApi.put(aObjectId, aObject);
	}

	/**
	 *
	 * @param aObjectId
	 */
	public void unpublish(String aObjectId) {
		mApi.remove(aObjectId);
	}

	/**
	 *
	 * @param aObjectId
	 * @return
	 */
	public boolean isPublished(String aObjectId) {
		return mApi.containsKey(aObjectId);
	}

	/**
	 *
	 * @param aObjectId
	 * @return
	 */
	public Object getPublished(String aObjectId) {
		return mApi.get(aObjectId);
	}

	/**
	 *
	 * @return
	 */
	public String getPath() {
		return mAppPath;
	}

	/**
	 *
	 * @return
	 */
	public ScriptAppLogger getLogger() {
		return mLogger;
	}

	/**
	 *
	 * @param aBoolean
	 * @param aMessage
	 */
	public void assertTrue(Boolean aBoolean, String aMessage) {
		Assert.isTrue(aBoolean, aMessage);
	}

	/**
	 *
	 * @param aObject
	 * @param aMessage
	 */
	public void assertNotNull(Object aObject, String aMessage) {
		Assert.notNull(aObject, aMessage);
	}

	/**
	 *
	 * @param aFile
	 * @throws Exception
	 */
	public void importScript(String aFile) throws Exception {
		aFile = aFile.replace("${APP_HOME}", mAppPath);
		String lFile = FileUtils.readFileToString(new File(Tools.expandEnvVarsAndProps(aFile)));
		mScriptApp.eval(lFile);
	}

	/**
	 *
	 * @param aConnector
	 * @param aMap
	 * @param aFragmentSize
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap, Integer aFragmentSize) {
		// outbound filtering
		notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aMap, aConnector});

		if (null != aFragmentSize) {
			mServer.sendTokenFragmented(aConnector, toToken(aMap), aFragmentSize);
		} else {
			mServer.sendToken(aConnector, toToken(aMap));
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aMap
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap) {
		sendToken(aConnector, aMap, null);
	}

	/**
	 *
	 * @param aConnector
	 * @param aMap
	 * @param aListener
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap, Object aListener) {
		// outbound filtering
		notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aMap, aConnector});

		mServer.sendTokenInTransaction(aConnector, toToken(aMap),
				(IPacketDeliveryListener) cast(aListener, IPacketDeliveryListener.class));
	}

	/**
	 *
	 * @param aConnector
	 * @param aMap
	 * @param aFragmentSize
	 * @param aListener
	 */
	public void sendToken(WebSocketConnector aConnector, Map aMap, Integer aFragmentSize, Object aListener) {
		// outbound filtering
		notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aMap, aConnector});

		mServer.sendTokenInTransaction(aConnector, toToken(aMap), aFragmentSize,
				(IPacketDeliveryListener) cast(aListener, IPacketDeliveryListener.class));
	}

	/**
	 *
	 * @param aConnector
	 * @param aChunkable
	 */
	public void sendChunkable(WebSocketConnector aConnector, Object aChunkable) {
		// IChunkable objects cannot be filtered in this level
		// notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aConnector, aMap});

		mServer.sendChunkable(aConnector, (IChunkable) cast(aChunkable, IChunkable.class));
	}

	/**
	 *
	 * @param aConnector
	 * @param aChunkable
	 * @param aListener
	 */
	public void sendChunkable(WebSocketConnector aConnector, Object aChunkable, Object aListener) {
		// IChunkable objects cannot be filtered in this level
		// notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{aConnector, aMap});

		mServer.sendChunkable(aConnector, (IChunkable) cast(aChunkable, IChunkable.class),
				(IChunkableDeliveryListener) cast(aListener, IChunkableDeliveryListener.class));
	}

	/**
	 * Cast JavaScript context objects into Java objects
	 *
	 * @param aObject
	 * @param aClass
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
	 *
	 * @return
	 */
	public Collection<WebSocketConnector> getAllConnectors() {
		return mServer.getServer().selectTokenConnectors().values();
	}

	/**
	 *
	 * @param aConnector
	 * @param aAuthority
	 * @return
	 */
	public boolean hasAuthority(WebSocketConnector aConnector, String aAuthority) {
		return mServer.hasAuthority(aConnector, aAuthority);
	}

	/**
	 *
	 * @param aConnector
	 * @param aAuthority
	 * @throws Exception
	 */
	public void requireAuthority(WebSocketConnector aConnector, String aAuthority) throws Exception {
		if (!mServer.hasAuthority(aConnector, aAuthority)) {
			throw new Exception("Not authorized. Missing required '" + aAuthority + "' authority!");
		}
	}

	/**
	 *
	 * @param aEvents
	 * @param aFn
	 */
	public void on(Collection<String> aEvents, Object aFn) {
		for (String lEventName : aEvents) {
			on(lEventName, aFn);
		}
	}

	/**
	 *
	 * @param aEventName
	 * @param aFn
	 */
	public void on(String aEventName, Object aFn) {
		if (!mCallbacks.containsKey(aEventName)) {
			mCallbacks.put(aEventName, new FastList<Object>());
		}
		mCallbacks.get(aEventName).add(aFn);
	}

	/**
	 *
	 * @param aEventName
	 * @param aFn
	 */
	public void un(String aEventName, Object aFn) {
		if (mCallbacks.containsKey(aEventName)) {
			mCallbacks.get(aEventName).remove(aFn);
		}
	}

	/**
	 *
	 * @param aMap
	 * @return
	 */
	protected Token toToken(Map aMap) {
		Token lToken = TokenFactory.createToken();
		lToken.setMap(aMap);

		return lToken;
	}

	/**
	 *
	 * @param aInToken
	 * @return
	 */
	public Map createResponse(Map aInToken) {
		return mServer.createResponse(toToken(aInToken)).getMap();
	}

	/**
	 *
	 * @param aConnectors
	 * @param aToken
	 */
	public void broadcast(Collection<WebSocketConnector> aConnectors, Map aToken) {
		for (WebSocketConnector aConnector : aConnectors) {
			sendToken(aConnector, aToken);
		}
	}

	/**
	 *
	 * @return
	 */
	public Map newThreadSafeMap() {
		return new FastMap().shared();
	}

	/**
	 *
	 * @return
	 */
	public Collection newThreadSafeCollection() {
		return new FastList().shared();
	}

	/**
	 *
	 * @param aObjectId
	 * @param aMethod
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public abstract Object callMethod(String aObjectId, String aMethod, Object[] aArgs) throws Exception;

	/**
	 *
	 * @param aNamespace
	 * @return
	 */
	public GenericApplicationContext getBeanFactory(String aNamespace) {
		if (null == aNamespace) {
			return JWebSocketBeanFactory.getInstance();
		} else {
			return JWebSocketBeanFactory.getInstance(aNamespace);
		}
	}

	/**
	 *
	 * @param aBeanId
	 * @return
	 */
	public Object getBean(String aBeanId) {
		return getBean(aBeanId, null);
	}

	/**
	 *
	 * @param aBeanId
	 * @param aNamespace
	 * @return
	 */
	public Object getBean(String aBeanId, String aNamespace) {
		return getBeanFactory(aNamespace).getBean(aBeanId);
	}

	/**
	 *
	 * @return
	 */
	public GenericApplicationContext getAppBeanFactory() {
		return getBeanFactory(getBeanFactoryNamespace());
	}

	/**
	 *
	 * @param aFile
	 * @throws Exception
	 */
	public void loadToAppBeanFactory(String aFile) throws Exception {
		aFile = aFile.replace("${APP_HOME}", mAppPath);
		JWebSocketBeanFactory.load(getBeanFactoryNamespace(), aFile, getClass().getClassLoader());
	}

	/**
	 *
	 * @return
	 */
	protected String getBeanFactoryNamespace() {
		return mServer.getNamespace() + ":" + getName();
	}

	/**
	 *
	 * @return
	 */
	public JMSManager getJMSManager() {
		return new JMSManager(this);
	}

	/**
	 *
	 * @param aUseTransaction
	 * @return
	 */
	public JMSManager getJMSManager(boolean aUseTransaction) {
		return new JMSManager(this, aUseTransaction);
	}

	/**
	 *
	 * @param aUseTransaction
	 * @param aConn
	 * @return
	 */
	public JMSManager getJMSManager(boolean aUseTransaction, Connection aConn) {
		return new JMSManager(this, aUseTransaction, aConn);
	}

	/**
	 *
	 * @return @throws Exception
	 */
	public abstract String getVersion() throws Exception;

	/**
	 *
	 * @return @throws Exception
	 */
	public abstract String getDescription() throws Exception;
}
