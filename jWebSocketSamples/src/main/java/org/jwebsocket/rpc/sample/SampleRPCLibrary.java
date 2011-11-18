//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Sample RPC-Library
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
 * @author aschulze
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
	 * @param aArg
	 * @return MD5 sum of the given string.
	 */
	public Object getMD5(String aArg) {
		return (Tools.getMD5(aArg));
	}

	/**
	 *
	 * @param aArg
	 * @return MD5 sum of the given string.
	 */
	public SampleRPCObject getRPCObject(SampleRPCObject aObject) {
		return aObject;
	}

	/**
	 * echos the given integer back to the client.
	 * This method is for demonstration, test and debug purpose only.
	 * @param aString
	 * @return
	 */
	public Object runIntDemo(int aInt) {
		return ("Successfully executed integer variant of method with unique name: " + aInt);
	}

	/**
	 * echos the given string back to the client.
	 * This method is for demonstration, test and debug purpose only.
	 * @param aString
	 * @return
	 */
	public Object runStringDemo(String aString) {
		return ("Successfully executed string variant of method with unique name: " + aString);
	}

	/**
	 * echos the given list back to the client.
	 * This method is for demonstration, test and debug purpose only.
	 * @param aList
	 * @return
	 */
	public Object runListDemo(List aList) {
		return (aList);
	}

	/**
	 * echos the given map back to the client.
	 * This method is for demonstration, test and debug purpose only.
	 * @param aMap
	 * @return
	 */
	public Object runMapDemo(Map aMap) {
		return (aMap);
	}

	/**
	 * echos the given integer back to the client.
	 * This method demonstrates the use of overloaded methods.
	 * @param aString
	 * @return
	 */
	public Object runOverloadDemo(int aInt) {
		return ("Successfully executed integer variant of overloaded method: " + aInt);
	}

	/**
	 * echos the given string back to the client.
	 * This method demonstrates the use of overloaded methods.
	 * @param aString
	 * @return
	 */
	public Object runOverloadDemo(String aString) {
		return ("Successfully executed string variant of overloaded method: " + aString);
	}

	/**
	 * echos the given list back to the client.
	 * This method demonstrates the use of overloaded methods.
	 * @param aList
	 * @return
	 */
	public Object runOverloadDemo(List aList) {
		return (aList);
	}

	/**
	 * echos the given map back to the client.
	 * This method demonstrates the use of overloaded methods.
	 * @param aMap
	 * @return
	 */
	public Object runOverloadDemo(Map aMap) {
		return (aMap);
	}

	/**
	 * usually protected (i.e. cannot be called from client
	 * until explicitely granted).
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
