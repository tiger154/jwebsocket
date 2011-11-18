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
package org.jwebsocket.eventmodel.session;

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.eventmodel.api.IStorageProvider;
import org.jwebsocket.eventmodel.api.ISessionReconnectionManager;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.plugins.events.EventsPlugIn;

/**
 *
 * @author kyberneees
 */
public class SessionFactory implements IListener, IInitializable {

	private IStorageProvider storageProvider;
	private ISessionReconnectionManager reconnectionManager;
	private static Logger mLog = Logging.getLogger(SessionFactory.class);
	private Map<String, IBasicStorage<String, Object>> sessions;

	public ISessionReconnectionManager getReconnectionManager() {
		return reconnectionManager;
	}

	public void setReconnectionManager(ISessionReconnectionManager reconnectionManager) {
		this.reconnectionManager = reconnectionManager;
	}

	public IStorageProvider getStorageProvider() {
		return storageProvider;
	}

	public void setStorageProvider(IStorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

	public IBasicStorage<String, Object> getSession(WebSocketConnector aConnector) throws Exception {
		return getSession(aConnector.getSession().getSessionId());
	}

	public IBasicStorage<String, Object> getSession(String aSessionId) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Getting session for: " + aSessionId + "...");
		}

		if (sessions.containsKey(aSessionId)) {
			//Getting the local cached storage instance if exists
			return sessions.get(aSessionId);
		}

		if (reconnectionManager.isExpired(aSessionId)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Creating a blank storage for session: " + aSessionId + "...");
			}
			IBasicStorage<String, Object> s = storageProvider.getStorage(aSessionId);
			s.clear();
			sessions.put(aSessionId, s);

			return s;
		} else {
			//Avoid security holes 
			reconnectionManager.getReconnectionIndex().remove(aSessionId);
			//Recovered session, require to be removed from the trash
			reconnectionManager.getSessionIdsTrash().remove(aSessionId);

			IBasicStorage<String, Object> s = storageProvider.getStorage(aSessionId);
			sessions.put(aSessionId, s);

			return s;
		}
	}

	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		//Never used
	}

	public void processEvent(ConnectorStopped aEvent, ResponseEvent aResponseEvent) {
		//Allowing all connectors for a reconnection
		String sid = aEvent.getConnector().getSession().getSessionId();

		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Putting the session: " + sid + ", in reconnection mode...");
		}

		synchronized (this) {
			//Removing the local cached storage instance. Free space if 
			//the client never re-connect
			sessions.remove(sid);

			if (!aEvent.getCloseReason().equals(CloseReason.SERVER)) {
				//Putting in reconnection mode
				reconnectionManager.putInReconnectionMode(sid);
			}
		}
	}

	@Override
	public void initialize() throws Exception {
		sessions = new FastMap<String, IBasicStorage<String, Object>>();

		//Listen the ConnectorStopped event
		((EventModel) EventsPlugIn.getBeanFactory().getBean("EventModel")).on(ConnectorStopped.class, this);
	}

	@Override
	public void shutdown() throws Exception {
	}
}
