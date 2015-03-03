//	---------------------------------------------------------------------------
//	jWebSocket - S2CEvent (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 *
 */
public abstract class S2CEvent implements ITokenizable {

	private String mId;
	private String mResponseType = "void"; //void by default
	private String mPlugInId;
	private Integer mTimeout = 1000; //One second by default

	/**
	 * Write the parent class fields values to the token
	 *
	 * @param aToken
	 */
	public void writeParentToToken(Token aToken) {
		aToken.setString("_e", getId());
		aToken.setString("_p", getPlugInId());
		aToken.setString("_rt", getResponseType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return The S2CEvent identifier
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param id The S2CEvent identifier to set
	 */
	public void setId(String id) {
		this.mId = id;
	}

	/**
	 * @return The S2CEvent response type
	 */
	public String getResponseType() {
		return mResponseType;
	}

	/**
	 * @param aResponseType The S2CEvent response type to set
	 */
	public void setResponseType(String aResponseType) {
		this.mResponseType = aResponseType;
	}

	/**
	 * @return The client target plug-in identifier
	 */
	public String getPlugInId() {
		return mPlugInId;
	}

	/**
	 * @param aPlugInId The client target plug-in identifier to set
	 */
	public void setPlugInId(String aPlugInId) {
		this.mPlugInId = aPlugInId;
	}

	/**
	 *
	 * @return The timeout limitation for this s2c event notification, 0 means
	 * unlimited
	 */
	public Integer getTimeout() {
		return mTimeout;
	}

	/**
	 *
	 * @param aTimeout The timeout limitation for this s2c event notification, 0
	 * means unlimited
	 */
	public void setTimeout(Integer aTimeout) {
		this.mTimeout = aTimeout;
	}
}
