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

import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.plugins.rpc.AbstractRpc;
import org.jwebsocket.token.Token;

/**
 * Class used to call a Rpc method (C2S)
 * Example: new Rpc.Call("com.org.aClass", "aMethod").send("hello", "it's a rrpc call", 123)
 * @author Quentin Ambard
 */
public class Rpc extends AbstractRpc {

	private static BaseTokenClient mDefaultBaseTokenClient;

	public static void setDefaultBaseTokenClient(BaseTokenClient aBaseTokenClient) {
		mDefaultBaseTokenClient = aBaseTokenClient;
	}
	private BaseTokenClient mBaseTokenClient;

	public Rpc(String aClassname, String aMethod) {
		super(aClassname, aMethod);
	}

	public Rpc(String aClassname, String aMethod, boolean aSpawnTread) {
		super(aClassname, aMethod, aSpawnTread);
	}

	public Rpc(Token aToken) {
		super(aToken);
	}

	/**
	 * Usefull if you have 2 jwebsocket connexions in the same client.
	 * @param aBaseTokenClient the baseTokenClient that will be used to make the call.
	 */
	public AbstractRpc using(BaseTokenClient aBaseTokenClient) {
		mBaseTokenClient = aBaseTokenClient;
		return this;
	}

	public Token call() {
		//use the default BaseTokenClient if not specified
		if (mBaseTokenClient == null) {
			mBaseTokenClient = mDefaultBaseTokenClient;
		}
		if (mBaseTokenClient == null) {
			return null;
		} else {
			Token lRpcToken = super.call();
			try {
				mBaseTokenClient.sendToken(lRpcToken);
			} catch (WebSocketException e) {
				return null;
			}
			return lRpcToken;
		}
	}
}
