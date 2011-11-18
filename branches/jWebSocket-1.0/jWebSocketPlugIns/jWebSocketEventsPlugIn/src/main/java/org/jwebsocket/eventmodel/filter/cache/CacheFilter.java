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

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.cachestorage.mongodb.MongoDBCacheStorageBuilder;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.token.Token;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.filter.ResponseFromCache;
import org.jwebsocket.eventmodel.exception.CachedResponseException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;

/**
 *
 * @author kyberneees
 */
public class CacheFilter extends EventModelFilter implements IListener {

	public final static String CLIENT_CACHE_ASPECT_STATUS = "client_cache_aspect_status";
	private static Logger mLog = Logging.getLogger(CacheFilter.class);
	private MongoDBCacheStorageBuilder cacheBuilder;

	public MongoDBCacheStorageBuilder getCacheBuilder() {
		return cacheBuilder;
	}

	public void setCacheBuilder(MongoDBCacheStorageBuilder cacheBuilder) {
		this.cacheBuilder = cacheBuilder;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		C2SEventDefinition def = getEm().getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
		if (!def.isCacheEnabled()) {
			return;
		}

		if (def.getCacheTime() > 0) {
			Token response = null;

			String oIn = null;
			if (def.isCachePrivate()) {

				String uuid = aConnector.getString("uuid");
				uuid = (uuid != null) ? uuid : "";

				oIn = (String) getCacheBuilder().
						getCacheStorage(MongoDBCacheStorageBuilder.V2, aEvent.getId() + uuid).get(aEvent.getRequestId());
			} else {
				oIn = (String) getCacheBuilder().
						getCacheStorage(MongoDBCacheStorageBuilder.V2, aEvent.getId()).get(aEvent.getRequestId());
			}

			if (oIn != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Element recovered from cache: " + oIn);
				}

				//Converting the stored stringyfied token to object
				response = JSONProcessor.jsonStringToToken(oIn);

				//ResponseFromCache event notification
				ResponseFromCache event = new ResponseFromCache();
				event.setId("response.from.cache");
				event.setCachedResponse(response);
				event.setEvent(aEvent);
				notify(event, null, true);

				Token protToken = getEm().getParent().createResponse(aEvent.getArgs());
				response.setInteger("utid", protToken.getInteger("utid"));
				//From cache the processing time is cero
				response.setDouble("processingTime", 0.0);

				//Sending the cached response clients the c
				if (def.isResponseAsync()) {
					getEm().getParent().getServer().sendTokenAsync(aConnector, response);
				} else {
					getEm().getParent().getServer().sendToken(aConnector, response);
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
		C2SEventDefinition def = aEvent.getEventDefinition();
		if (def.isCacheEnabled() && def.getCacheTime() > 0) {
			//Caching local value
			String id = def.getId();

			//Saving in cache
			if (def.isCachePrivate()) {
				String uuid = aEvent.getConnector().getString("uuid");
				uuid = (uuid != null) ? uuid : "";

				//Putting the response token in cache using the event cache time
				if (mLog.isDebugEnabled()) {
					mLog.debug("Caching element with id("
							+ id + uuid + "): " + aEvent.getRequestId());
				}

				getCacheBuilder().getCacheStorage(MongoDBCacheStorageBuilder.V2, id + uuid).
						put(aEvent.getRequestId(),
						JSONProcessor.tokenToJSON(aEvent.getArgs()).toString(), def.getCacheTime());
			} else {
				//Putting the response token in cache using the event cache time
				if (mLog.isDebugEnabled()) {
					mLog.debug("Caching element with id("
							+ id + "): " + aEvent.getRequestId());
				}
				getCacheBuilder().getCacheStorage(MongoDBCacheStorageBuilder.V2, id).put(aEvent.getRequestId(),
						JSONProcessor.tokenToJSON(aEvent.getArgs()).toString(), def.getCacheTime());
			}
		}
	}
}
