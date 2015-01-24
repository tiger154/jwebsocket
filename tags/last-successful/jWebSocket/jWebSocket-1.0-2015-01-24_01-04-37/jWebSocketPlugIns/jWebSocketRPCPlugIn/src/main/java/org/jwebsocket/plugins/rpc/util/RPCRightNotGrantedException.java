//	---------------------------------------------------------------------------
//	jWebSocket - RPCRightNotGrantedException (Community Edition, CE)
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
package org.jwebsocket.plugins.rpc.util;

/**
 * Exception when a client try to call a method without the right
 *
 * @author Quentin Ambard
 */
public class RPCRightNotGrantedException extends Exception {

	private String mMethod;
	private String mParameters;

	/**
	 *
	 */
	public RPCRightNotGrantedException() {
	}

	/**
	 *
	 * @param aMethod
	 * @param aParameters
	 */
	public RPCRightNotGrantedException(String aMethod, String aParameters) {
		mMethod = aMethod;
		mParameters = aParameters;
	}

	@Override
	public String getMessage() {
		return "the user does not have the right to call the rpc method "
				+ mMethod
				+ "(" + mParameters + ").";
	}
}
