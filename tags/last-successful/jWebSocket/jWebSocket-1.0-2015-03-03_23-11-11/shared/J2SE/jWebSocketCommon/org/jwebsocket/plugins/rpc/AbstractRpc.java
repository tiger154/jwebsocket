//	---------------------------------------------------------------------------
//	jWebSocket AbstractRpc (Community Edition, CE)
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
package org.jwebsocket.plugins.rpc;

import java.util.Arrays;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Alexander Schulze
 */
public abstract class AbstractRpc {

	private static boolean defaultSpawnThread = false;

	/**
	 *
	 * @param aDefaultValue
	 */
	public static void setDefaultSpanwThread(boolean aDefaultValue) {
		defaultSpawnThread = aDefaultValue;
	}
	private Boolean mSpawnThread = null;
	private String mClassname;
	private String mMethod;
	private List mArg = null;

	/**
	 *
	 */
	protected AbstractRpc() {
	}

	;
	/**
	 *
	 * @param aClassname
	 * @param aMethod
	 */
	public AbstractRpc(String aClassname, String aMethod) {
		this(aClassname, aMethod, defaultSpawnThread);
	}

	/**
	 * @param aClassname the classname to call
	 * @param aMethod the method to call
	 * @param aSpawnTread
	 */
	public AbstractRpc(String aClassname, String aMethod, boolean aSpawnTread) {
		mClassname = aClassname;
		mMethod = aMethod;
		mSpawnThread = aSpawnTread;
	}

	/**
	 * The token should contains all the necessary informations. Can be usefull
	 * to create a direct call from an already-created token
	 *
	 * @param aToken
	 */
	public AbstractRpc(Token aToken) {
		mClassname = aToken.getString(CommonRpcPlugin.RRPC_KEY_CLASSNAME);
		mMethod = aToken.getString(CommonRpcPlugin.RRPC_KEY_METHOD);
		mSpawnThread = aToken.getBoolean(CommonRpcPlugin.RRPC_KEY_SPAWNTHREAD, defaultSpawnThread);
		//First try to get a list of arguments
		List lListOfArg = aToken.getList(CommonRpcPlugin.RRPC_KEY_ARGS);
		if (lListOfArg != null) {
			mArg = lListOfArg;
		} else {
			//If there is no list, get a simple object as argument
			Object lObject = aToken.getObject(CommonRpcPlugin.RRPC_KEY_ARGS);
			if (lObject != null) {
				mArg = new FastList();
				mArg.add(lObject);
			}
		}
	}

	/**
	 * Send the Objects you want to the remote procedure. Create a list from
	 * these objects.
	 *
	 * @param aArg objects you want to send to the client.
	 * @return
	 */
	public AbstractRpc send(Object... aArg) {
		if (aArg != null) {
			mArg = new FastList();
			mArg.addAll(Arrays.asList(aArg));
		}
		return this;
	}

	/**
	 * Directly send this list of object to the remote procedure
	 *
	 * @param aArgs a List of arguments already built
	 * @return
	 */
	public AbstractRpc sendListOfArgs(List aArgs) {
		mArg = aArgs;
		return this;
	}

	/**
	 * Make the call.
	 *
	 * @return
	 */
	public Token call() {
		Token rpcToken = TokenFactory.createToken("rpc");
		rpcToken.setNS(CommonRpcPlugin.NS_RPC_DEFAULT);
		rpcToken.setString(CommonRpcPlugin.RRPC_KEY_CLASSNAME, mClassname);
		rpcToken.setString(CommonRpcPlugin.RRPC_KEY_METHOD, mMethod);
		rpcToken.setList(CommonRpcPlugin.RRPC_KEY_ARGS, mArg);
		boolean lSpawnThread;
		if (mSpawnThread == null) {
			lSpawnThread = defaultSpawnThread;
		} else {
			lSpawnThread = mSpawnThread;
		}
		rpcToken.setBoolean("spawnThread", lSpawnThread);
		return rpcToken;
	}
}
