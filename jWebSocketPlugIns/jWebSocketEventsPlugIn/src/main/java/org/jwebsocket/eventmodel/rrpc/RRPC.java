//	---------------------------------------------------------------------------
//	jWebSocket - RRPC (Community Edition, CE)
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
package org.jwebsocket.eventmodel.rrpc;

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class RRPC implements ITokenizable {

	private String procedureName;
	private String responseType;
	private Integer timeout;
	private Map<String, Object> args;
	/**
	 *
	 */
	public static final String PROCEDURE_NAME = "$pn";
	/**
	 *
	 */
	public static final String RESPONSE_TYPE = "$rt";
	/**
	 *
	 */
	public static final String ARGUMENTS = "$args";

	/**
	 * Create a new RRPC
	 *
	 * @param aProcedureName
	 * @param aResponseType
	 * @param aTimeout
	 */
	public RRPC(String aProcedureName, String aResponseType, Integer aTimeout) {
		this.procedureName = aProcedureName;
		this.responseType = aResponseType;
		this.timeout = aTimeout;
		this.args = new FastMap<String, Object>();
	}

	/**
	 * Create a new RRPC
	 *
	 * @param aProcedureName
	 * @param aResponseType
	 * @param aTimeout
	 * @param aArgs
	 */
	public RRPC(String aProcedureName, String aResponseType, Integer aTimeout, Map<String, Object> aArgs) {
		this.procedureName = aProcedureName;
		this.responseType = aResponseType;
		this.timeout = aTimeout;
		this.args = aArgs;
	}

	/**
	 *
	 * @return
	 */
	public String getProcedureName() {
		return procedureName;
	}

	/**
	 *
	 * @param procedureName
	 */
	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	/**
	 *
	 * @return
	 */
	public String getResponseType() {
		return responseType;
	}

	/**
	 *
	 * @param responseType
	 */
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	/**
	 *
	 * @return
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 *
	 * @param timeout
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Object> getArgs() {
		return args;
	}

	/**
	 *
	 * @param args
	 */
	public void setArgs(Map<String, Object> args) {
		this.args = args;
	}

	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeToToken(Token aToken) {
		aToken.setString(PROCEDURE_NAME, procedureName);
		aToken.setString(RESPONSE_TYPE, responseType);
		aToken.setMap(ARGUMENTS, args);
	}
}
