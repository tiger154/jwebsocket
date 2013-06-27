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
package org.jwebsocket.client.plugins.rpc;

import java.util.List;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class RpcListener implements WebSocketClientTokenListener {
//  private BaseTokenClient mBaseTokenClient;
//  
//  public RpcListener(BaseTokenClient aBaseTokenClient) {
//  	mBaseTokenClient = aBaseTokenClient;
//  }

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		if (CommonRpcPlugin.RRPC_TYPE.equals(aToken.getType())) {
			String lClassName = aToken.getString(CommonRpcPlugin.RRPC_KEY_CLASSNAME);
			String lMethodName = aToken.getString(CommonRpcPlugin.RRPC_KEY_METHOD);
			String lSourceId = aToken.getString(CommonRpcPlugin.RRPC_KEY_SOURCE_ID);
			List largs = aToken.getList(CommonRpcPlugin.RRPC_KEY_ARGS);
			Token lRespToken = RPCPlugin.processRrpc(lClassName, lMethodName, largs, lSourceId);
//			try {
//				if (mBaseTokenClient == null) {
//					// TODO add some beautiful log compatible with all clients here ?
//					// BaseTokenClient is not correctly initialized
//				} else {
//					mBaseTokenClient.sendToken(lRespToken);
//				}
//			} catch (WebSocketException e) {
//				// TODO add some beautiful log compatible with all clients here ?
//			}
		}
	}
}
