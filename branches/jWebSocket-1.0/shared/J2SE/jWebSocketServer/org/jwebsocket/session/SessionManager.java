//	---------------------------------------------------------------------------
//	jWebSocket - SessionManager (Community Edition, CE)
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
package org.jwebsocket.session;

import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.*;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

/**
 * Manages the jWebSocket sessions. It uses a cache implementation, which can be
 * configured by Spring.
 *
 * @author kyberneees, aschulze
 */
public class SessionManager implements ISessionManager {

	private IStorageProvider mStorageProvider;
	private ISessionReconnectionManager mReconnectionManager;
	private static Logger mLog = Logging.getLogger();
	private Map<String, IBasicStorage<String, Object>> mStorageRefs;

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @return
	 */
	@Override
	public ISessionReconnectionManager getReconnectionManager() {
		return mReconnectionManager;
	}

	/**
	 *
	 * @param aReconnectionManager
	 */
	public void setReconnectionManager(ISessionReconnectionManager aReconnectionManager) {
		this.mReconnectionManager = aReconnectionManager;
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @return
	 */
	@Override
	public IStorageProvider getStorageProvider() {
		return mStorageProvider;
	}

	/**
	 *
	 * @param aStorageProvider
	 */
	public void setStorageProvider(IStorageProvider aStorageProvider) {
		this.mStorageProvider = aStorageProvider;
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param aConnector
	 * @return
	 * @throws Exception
	 */
	@Override
	public IBasicStorage<String, Object> getSession(WebSocketConnector aConnector) throws Exception {
		return getSession(aConnector.getSession().getSessionId());
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param aSessionId
	 * @return
	 * @throws Exception
	 */
	@Override
	public IBasicStorage<String, Object> getSession(String aSessionId) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Getting session for: " + aSessionId + "...");
		}

		if (mStorageRefs.containsKey(aSessionId)) {
			// Getting the local cached storage instance if exists
			return mStorageRefs.get(aSessionId);
		}

		if (mReconnectionManager.isExpired(aSessionId)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating a blank storage for session: " + aSessionId + "...");
			}
			IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(aSessionId);
			lStorage.clear();
			mStorageRefs.put(aSessionId, lStorage);

			return lStorage;
		} else {
			// avoid security holes 
			mReconnectionManager.getReconnectionIndex().remove(aSessionId);
			// recovered session, require to be removed from the trash
			mReconnectionManager.getSessionIdsTrash().remove(aSessionId);

			IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(aSessionId);
			mStorageRefs.put(aSessionId, lStorage);

			return lStorage;
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
		mStorageRefs = new FastMap<String, IBasicStorage<String, Object>>();
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void shutdown() throws Exception {
		mStorageRefs.clear();
	}

	/**
	 *
	 * @param aConnector
	 * @throws Exception
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) throws Exception {
		Map<String, Object> lStorage;
		if (null == aConnector.getSession().getStorage()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating the WebSocketSession persistent storage "
						+ "for connector '" + aConnector.getId() + "'...");
			}
			lStorage = (Map<String, Object>) getSession(aConnector.getSession().getSessionId());
			aConnector.getSession().setStorage(lStorage);
		} else {
			lStorage = aConnector.getSession().getStorage();
		}

		// setting the username
		if (lStorage.containsKey(SystemPlugIn.USERNAME)) {
			aConnector.setUsername(lStorage.get(SystemPlugIn.USERNAME).toString());
		}
	}

	/**
	 *
	 * @param aConnector
	 * @throws Exception
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector) throws Exception {
		Map<String, Object> lStorage = aConnector.getSession().getStorage();

		// ommiting if running in embedded session mode (HTTP servlet containers)
		if (lStorage != null && !(lStorage instanceof HttpSessionStorage)) {
			String lSessionId = aConnector.getSession().getSessionId();

			if (mLog.isDebugEnabled()) {
				mLog.debug("Putting the session: " + lSessionId
						+ ", in reconnection mode...");
			}
			synchronized (this) {
				// removing the local cached  storage instance. 
				// free space if the client never gets reconnected
				mStorageRefs.remove(lSessionId);
				getReconnectionManager().putInReconnectionMode(aConnector.getSession());
			}
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Removing connection specific storage for connector '" + aConnector.getId() + "'...");
		}
		getStorageProvider().removeStorage(aConnector.getId());
	}
}
