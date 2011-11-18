//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2011 jwebsocket.org
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
package org.jwebsocket.session;

import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.ISessionReconnectionManager;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.logging.Logging;

/**
 * Manages the jWebSocket sessions. It uses a cache implementation, 
 * which can be configured by Spring.
 * @author kyberneees, aschulze
 */
public class SessionManager implements ISessionManager, IInitializable /*, WebSocketServerListener */ {

	private IStorageProvider mStorageProvider;
	private ISessionReconnectionManager mReconnectionManager;
	private static Logger mLog = Logging.getLogger(SessionManager.class);
	private Map<String, IBasicStorage<String, Object>> mSessions;

	/**
	 * 
	 * @return
	 */
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
	 * @return
	 */
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
	 * @param aSessionId
	 * @return
	 * @throws Exception
	 */
	@Override
	public IBasicStorage<String, Object> getSession(String aSessionId) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Getting session for: " + aSessionId + "...");
		}

		if (mSessions.containsKey(aSessionId)) {
			// Getting the local cached storage instance if exists
			return mSessions.get(aSessionId);
		}

		if (mReconnectionManager.isExpired(aSessionId)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating a blank storage for session: " + aSessionId + "...");
			}
			IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(aSessionId);
			lStorage.clear();
			mSessions.put(aSessionId, lStorage);

			return lStorage;
		} else {
			// Avoid security holes 
			mReconnectionManager.getReconnectionIndex().remove(aSessionId);
			// Recovered session, require to be removed from the trash
			mReconnectionManager.getSessionIdsTrash().remove(aSessionId);

			IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(aSessionId);
			mSessions.put(aSessionId, lStorage);

			return lStorage;
		}
	}

	@Override
	public void initialize() throws Exception {
		mSessions = new FastMap<String, IBasicStorage<String, Object>>();
	}

	@Override
	public void shutdown() throws Exception {
		mSessions.clear();
	}

/*
	@Override
	public void processClosed(WebSocketServerEvent aEvent) {
		//Allowing all connectors for a reconnection
		String lSessionId = aEvent.getConnector().getSession().getSessionId();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Putting the session: " + lSessionId + ", in reconnection mode...");
		}

		synchronized (this) {
			//Removing the local cached storage instance. Free space if 
			//the client never gets reconnected
			mSessions.remove(lSessionId);
			mReconnectionManager.putInReconnectionMode(lSessionId);
		}
	}
	
	@Override
	public void processOpened(WebSocketServerEvent aEvent) {
		try {
			// set session id first, so that it can be processed in the connectorStarted
			// method
			Random lRand = new Random(System.nanoTime());

			// @TODO: if unique node id is passed check if already assigned in the
			// network and reject connect if so!

			if (mLog.isDebugEnabled()) {
				mLog.debug("Setting the session identifier: " + aEvent.getConnector().getId());
			}
			aEvent.getSession().setSessionId(
					Tools.getMD5(aEvent.getConnector().generateUID()
					+ "." + lRand.nextInt()));

			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating the WebSocketSession persistent storage "
						+ "for connector: " + aEvent.getConnector().getId());
			}
			aEvent.getSession().setStorage((Map<String, Object>) (getSession(aEvent.getSessionId())));
		} catch (Exception ex) {
			 // TODO: try this with the ExceptionHandler
		}
	}

	@Override
	public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket aPacket) {
	}
*/

}
