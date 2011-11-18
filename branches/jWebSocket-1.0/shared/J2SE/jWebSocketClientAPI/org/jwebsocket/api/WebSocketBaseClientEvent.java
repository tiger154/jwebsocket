//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.api;

/**
 *
 * @author aschulze
 */
public class WebSocketBaseClientEvent implements WebSocketClientEvent {

	private String mName = null;
	private String mData = null;
	private WebSocketClient mClient = null;
/*
	public WebSocketBaseClientEvent() {
	}
*/
	public WebSocketBaseClientEvent(WebSocketClient aClient, String aName, String aData) {
		mClient = aClient;
		mName = aName;
		mData = aData;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getData() {
		return mData;
	}

	@Override
	public WebSocketClient getClient() {
		return mClient;
	}
}
