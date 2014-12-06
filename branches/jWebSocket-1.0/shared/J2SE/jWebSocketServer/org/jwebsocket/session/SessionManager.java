//	---------------------------------------------------------------------------
//	jWebSocket - SessionManager (Community Edition, CE)
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
package org.jwebsocket.session;

import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.api.ISessionReconnectionManager;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

/**
 * Manages the jWebSocket sessions. It uses a cache implementation, which can be
 * configured by Spring.
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class SessionManager implements ISessionManager {

	private IStorageProvider mStorageProvider;
	private ISessionReconnectionManager mReconnectionManager;
	private static final Logger mLog = Logging.getLogger();

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

		if (mReconnectionManager.isExpired(aSessionId)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating a blank storage for session: " + aSessionId + "...");
			}
			IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(aSessionId);
			lStorage.clear();

			return lStorage;
		} else {
			// recovered session, require to be removed from the trash
			mReconnectionManager.getSessionIdsTrash().remove(aSessionId);
			IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(aSessionId);

			return lStorage;
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @throws java.lang.Exception
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @throws java.lang.Exception
	 */
	@Override
	public void shutdown() throws Exception {
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
			// getting the session id
			String lSessionId = aConnector.getSession().getSessionId();

			// REQUIRED FOR CLUSTERING SESSION RECOVERY SUPPORT
			// ------------------------------------------------
			// recovered session, require to be removed from the trash
			mReconnectionManager.getSessionIdsTrash().remove(lSessionId);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 * @param aIsSessionShared
	 * @throws Exception
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason, boolean aIsSessionShared) throws Exception {
		Map<String, Object> lStorage = aConnector.getSession().getStorage();

		if (null != lStorage && !aCloseReason.equals(CloseReason.TIMEOUT) && !aIsSessionShared) {
			// ommiting if running in embedded session mode (HTTP servlet containers)
			if (!(lStorage instanceof HttpSessionStorage)) {
				String lSessionId = aConnector.getSession().getSessionId();

				if (mLog.isDebugEnabled()) {
					mLog.debug("Putting the session: " + lSessionId
							+ ", in reconnection mode...");
				}
				synchronized (this) {
					getReconnectionManager().putInReconnectionMode(aConnector.getSession());
				}
			}
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Removing connection specific storage for connector '" + aConnector.getId() + "'...");
		}
		getStorageProvider().removeStorage(aConnector.getId());
	}
}
