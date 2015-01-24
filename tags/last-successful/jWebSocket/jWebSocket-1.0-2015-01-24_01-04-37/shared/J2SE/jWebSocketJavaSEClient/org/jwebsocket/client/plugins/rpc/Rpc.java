//	---------------------------------------------------------------------------
//	jWebSocket - Rpc (Community Edition, CE)
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

import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.plugins.rpc.AbstractRpc;
import org.jwebsocket.token.Token;

/**
 * Class used to call a Rpc method (C2S) Example: new Rpc.Call("com.org.aClass",
 * "aMethod").send("hello", "it's a rrpc call", 123)
 *
 * @author Quentin Ambard
 */
public class Rpc extends AbstractRpc {

	private static BaseTokenClient mDefaultBaseTokenClient;

	/**
	 *
	 * @param aBaseTokenClient
	 */
	public static void setDefaultBaseTokenClient(BaseTokenClient aBaseTokenClient) {
		mDefaultBaseTokenClient = aBaseTokenClient;
	}
	private BaseTokenClient mBaseTokenClient;

	/**
	 *
	 * @param aClassname
	 * @param aMethod
	 */
	public Rpc(String aClassname, String aMethod) {
		super(aClassname, aMethod);
	}

	/**
	 *
	 * @param aClassname
	 * @param aMethod
	 * @param aSpawnTread
	 */
	public Rpc(String aClassname, String aMethod, boolean aSpawnTread) {
		super(aClassname, aMethod, aSpawnTread);
	}

	/**
	 *
	 * @param aToken
	 */
	public Rpc(Token aToken) {
		super(aToken);
	}

	/**
	 * Usefull if you have 2 jwebsocket connexions in the same client.
	 *
	 * @param aBaseTokenClient the baseTokenClient that will be used to make the
	 * call.
	 * @return
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
