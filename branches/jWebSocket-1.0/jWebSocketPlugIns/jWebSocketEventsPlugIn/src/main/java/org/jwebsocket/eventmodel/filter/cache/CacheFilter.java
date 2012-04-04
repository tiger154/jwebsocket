//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.filter.cache;

import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.token.Token;
import org.apache.log4j.Logger;
import org.jwebsocket.api.ICacheStorageProvider;
import org.jwebsocket.eventmodel.event.filter.ResponseFromCache;
import org.jwebsocket.eventmodel.exception.CachedResponseException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.system.SystemPlugIn;

/**
 *
 * @author kyberneees
 */
public class CacheFilter extends EventModelFilter implements IListener {

	public final static String CLIENT_CACHE_ASPECT_STATUS = "client_cache_aspect_status";
	private static Logger mLog = Logging.getLogger(CacheFilter.class);
	private ICacheStorageProvider mCacheStorageProvider;

	public ICacheStorageProvider getCacheStorageProvider() {
		return mCacheStorageProvider;
	}

	public void setCacheStorageProvider(ICacheStorageProvider aCacheStorageProvider) {
		mCacheStorageProvider = aCacheStorageProvider;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		C2SEventDefinition lDef = getEm().getEventFactory().
				getEventDefinitions().getDefinition(aEvent.getId());
		if (!lDef.isCacheEnabled()) {
			return;
		}

		if (lDef.getCacheTime() > 0) {
			Token lResponse = null;

			String lCachedResponse = null;
			if (lDef.isCachePrivate()) {

				String uuid = aConnector.getString("uuid");
				uuid = (uuid != null) ? uuid : "";

				lCachedResponse = (String) getCacheStorageProvider().
						getCacheStorage(aEvent.getId() + uuid).get(aEvent.getRequestId());
			} else {
				lCachedResponse = (String) getCacheStorageProvider().
						getCacheStorage(aEvent.getId()).get(aEvent.getRequestId());
			}

			if (lCachedResponse != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Element recovered from cache: " + lCachedResponse);
				}

				//Converting the stored stringyfied token to object
				lResponse = JSONProcessor.jsonStringToToken(lCachedResponse);

				//ResponseFromCache event notification
				ResponseFromCache lEvent = new ResponseFromCache();
				lEvent.setId("response.from.cache");
				lEvent.setCachedResponse(lResponse);
				lEvent.setEvent(aEvent);
				notify(lEvent, null, true);

				lResponse.setInteger("utid", aEvent.getArgs().getInteger("utid"));
				//From cache the processing time is cero
				lResponse.setDouble("processingTime", 0.0);

				//Sending the cached response clients the c
				if (lDef.isResponseAsync()) {
					getEm().getParent().getServer().sendTokenAsync(aConnector, lResponse);
				} else {
					getEm().getParent().getServer().sendToken(aConnector, lResponse);
				}

				//Stopping the filter chain
				throw new CachedResponseException();
			}
		}
	}

	/**
	 * Called before send the response clients the client
	 * 
	 * @param aEvent The event clients process
	 * @param aResponseEvent 
	 */
	public void processEvent(BeforeRouteResponseToken aEvent, ResponseEvent aResponseEvent) throws Exception {
		C2SEventDefinition lDef = aEvent.getEventDefinition();
		if (lDef.isCacheEnabled() && lDef.getCacheTime() > 0) {
			//Caching local value
			String lId = lDef.getId();

			//Saving in cache
			Map<String, Object> lConnectorSession = aEvent.getConnector().getSession().getStorage();
			if (lDef.isCachePrivate() && lConnectorSession.containsKey(SystemPlugIn.UUID)) {
				String lUUID = lConnectorSession.get(SystemPlugIn.UUID).toString();
				lUUID = (lUUID != null) ? lUUID : "";

				//Putting the response token in cache using the event cache time
				if (mLog.isDebugEnabled()) {
					mLog.debug("Caching element with id("
							+ lId + lUUID + "): " + aEvent.getRequestId());
				}

				getCacheStorageProvider().getCacheStorage(lId + lUUID).
						put(aEvent.getRequestId(),
						JSONProcessor.tokenToJSON(aEvent.getArgs()).toString(), lDef.getCacheTime());
			} else {
				//Putting the response token in cache using the event cache time
				if (mLog.isDebugEnabled()) {
					mLog.debug("Caching element with id("
							+ lId + "): " + aEvent.getRequestId());
				}
				getCacheStorageProvider().getCacheStorage(lId).put(aEvent.getRequestId(),
						JSONProcessor.tokenToJSON(aEvent.getArgs()).toString(), lDef.getCacheTime());
			}
		}
	}
}
