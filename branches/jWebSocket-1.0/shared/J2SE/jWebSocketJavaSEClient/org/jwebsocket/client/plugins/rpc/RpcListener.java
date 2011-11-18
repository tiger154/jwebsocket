package org.jwebsocket.client.plugins.rpc;

import java.util.List;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.token.Token;

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
