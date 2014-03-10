//	---------------------------------------------------------------------------
//	jWebSocket - SampleRPCLibrary (Community Edition, CE)
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
package org.jwebsocket.rpc.sample;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;

import org.jwebsocket.plugins.rpc.BaseRPCCallable;
import org.jwebsocket.plugins.rpc.SampleRPCObject;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Alexander Schulze
 */
public class SampleRPCLibrary extends BaseRPCCallable {

	private static Logger mLog = null;

	private void logInfo(String aMessage) {
		if (mLog == null) {
			mLog = Logging.getLogger(SampleRPCLibrary.class);
		}
		if (mLog.isInfoEnabled()) {
			mLog.info(aMessage);
		}
	}

	/**
	 * simply returns the MD5 sum of the given string.
	 *
	 * @param aArg
	 * @return MD5 sum of the given string.
	 */
	public Object getMD5(String aArg) {
		return (Tools.getMD5(aArg));
	}

	/**
	 *
	 * @param aObject
	 * @return MD5 sum of the given string.
	 */
	public SampleRPCObject getRPCObject(SampleRPCObject aObject) {
		return aObject;
	}

	/**
	 * echos the given integer back to the client. This method is for
	 * demonstration, test and debug purpose only.
	 *
	 * @param aInt
	 * @return
	 */
	public Object runIntDemo(int aInt) {
		return ("Successfully executed integer variant of method with unique name: " + aInt);
	}

	/**
	 * echos the given string back to the client. This method is for
	 * demonstration, test and debug purpose only.
	 *
	 * @param aString
	 * @return
	 */
	public Object runStringDemo(String aString) {
		return ("Successfully executed string variant of method with unique name: " + aString);
	}

	/**
	 * echos the given list back to the client. This method is for
	 * demonstration, test and debug purpose only.
	 *
	 * @param aList
	 * @return
	 */
	public Object runListDemo(List aList) {
		return (aList);
	}

	/**
	 * echos the given map back to the client. This method is for demonstration,
	 * test and debug purpose only.
	 *
	 * @param aMap
	 * @return
	 */
	public Object runMapDemo(Map aMap) {
		return (aMap);
	}

	/**
	 * echos the given integer back to the client. This method demonstrates the
	 * use of overloaded methods.
	 *
	 * @param aInt
	 * @return
	 */
	public Object runOverloadDemo(int aInt) {
		return ("Successfully executed integer variant of overloaded method: " + aInt);
	}

	/**
	 * echos the given string back to the client. This method demonstrates the
	 * use of overloaded methods.
	 *
	 * @param aString
	 * @return
	 */
	public Object runOverloadDemo(String aString) {
		return ("Successfully executed string variant of overloaded method: " + aString);
	}

	/**
	 * echos the given list back to the client. This method demonstrates the use
	 * of overloaded methods.
	 *
	 * @param aList
	 * @return
	 */
	public Object runOverloadDemo(List aList) {
		return (aList);
	}

	/**
	 * echos the given map back to the client. This method demonstrates the use
	 * of overloaded methods.
	 *
	 * @param aMap
	 * @return
	 */
	public Object runOverloadDemo(Map aMap) {
		return (aMap);
	}

	/**
	 * usually protected (i.e. cannot be called from client until explicitely
	 * granted).
	 *
	 * @param aArg
	 * @return MD5 sum of the given string.
	 */
	public Object getProtected(String aArg) {
		return "Protected method has now been granted for RPC";
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	public Token sampleTokenRPC(Token aToken) {
		// currently simply return the same token for test purposes
		return aToken;
	}

	/**
	 * Exemple of 2 overloaded methods
	 * org.jwebsocket.rpc.sample.SampleRPCLibrary.sampleOverloadRPC(int)
	 *
	 * @param aString
	 * @return aString
	 */
	public String sampleOverloadRPC(String aString) {
		// currently simply return the same string for test purposes
		return aString + " the String method has been called";
	}

	/**
	 * Exemple of 2 overloaded methods
	 * org.jwebsocket.rpc.sample.SampleRPCLibrary.sampleOverloadRPC(int)
	 *
	 * @param aInt
	 * @return aInt
	 */
	public int sampleOverloadRPC(int aInt) {
		// currently simply return the same string for test purposes
		return aInt;
	}

	/**
	 *
	 * @param aList
	 * @return
	 */
	public String testList(List<Integer> aList) {
		// currently simply return the same string for test purposes
		return "I'm the server, testList has been called";
	}

	/**
	 *
	 * @return
	 */
	public String rrpcTest1() {
		logInfo("rrpcTest1");
		return "This is the result of 'rrpcTest1'.";
	}

	/**
	 *
	 * @param arg1
	 */
	public void rrpcTest1(String arg1) {
		logInfo("rrpcTest11");
	}

	/**
	 *
	 * @param aWebSocketConnector
	 * @param arg1
	 */
	public void rrpcTest1(WebSocketConnector aWebSocketConnector, int arg1) {
		logInfo("rrpcTest12 has been called by connector: " + aWebSocketConnector.getId());
	}

	/**
	 *
	 * @param aList
	 * @param aList2
	 */
	public void rrpcTest2(List aList, List<List<Integer>> aList2) {
		logInfo("rrpcTest2");
	}

	/**
	 *
	 */
	public void rrpcTest3() {
		logInfo("rrpcTest3");
	}
}
