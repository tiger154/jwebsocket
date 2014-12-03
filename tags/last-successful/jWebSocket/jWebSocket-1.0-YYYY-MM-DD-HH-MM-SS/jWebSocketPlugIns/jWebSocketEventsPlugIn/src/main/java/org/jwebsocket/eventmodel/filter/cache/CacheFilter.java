//	---------------------------------------------------------------------------
//	jWebSocket - CacheFilter (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.cache;

import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.ICacheStorageProvider;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.event.filter.ResponseFromCache;
import org.jwebsocket.eventmodel.exception.CachedResponseException;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class CacheFilter extends EventModelFilter implements IListener {

	/**
	 *
	 */
	public final static String CLIENT_CACHE_ASPECT_STATUS = "client_cache_aspect_status";
	private static Logger mLog = Logging.getLogger(CacheFilter.class);
	private ICacheStorageProvider mCacheStorageProvider;

	/**
	 *
	 * @return
	 */
	public ICacheStorageProvider getCacheStorageProvider() {
		return mCacheStorageProvider;
	}

	/**
	 *
	 * @param aCacheStorageProvider
	 */
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
			Token lResponse;
			String lCachedResponse;

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

				//Converting the stored serialized token to object
				lResponse = JSONProcessor.JSONStringToToken(lCachedResponse);

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
				getEm().getParent().getServer().sendTokenFragmented(aConnector,
						lResponse,
						getEm().getFragmentSize());

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
	 * @throws Exception
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
