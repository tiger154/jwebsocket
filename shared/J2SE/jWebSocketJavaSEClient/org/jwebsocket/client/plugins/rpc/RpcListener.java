//	---------------------------------------------------------------------------
//	jWebSocket - RpcListener (Community Edition, CE)
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
package org.jwebsocket.client.plugins.rpc;

import java.util.List;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
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
