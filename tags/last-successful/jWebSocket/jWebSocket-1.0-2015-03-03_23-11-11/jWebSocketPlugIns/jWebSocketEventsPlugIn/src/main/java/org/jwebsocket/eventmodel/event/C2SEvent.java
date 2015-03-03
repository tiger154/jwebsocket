//	---------------------------------------------------------------------------
//	jWebSocket - C2SEvent (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.util.Util;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class C2SEvent extends Event implements IInitializable {

	private WebSocketConnector mConnector;
	private String mRequestId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() throws Exception {
		mRequestId = Util.generateSharedUTID(this.getArgs());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() {
	}

	/**
	 * @return The client WebSocketConnector
	 */
	public WebSocketConnector getConnector() {
		return mConnector;
	}

	/**
	 * @param aConnector The client WebSocketConnector to set
	 */
	public void setConnector(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}

	/**
	 * @return The event arguments MD5
	 */
	public String getRequestId() {
		return mRequestId;
	}
}
