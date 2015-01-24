//	---------------------------------------------------------------------------
//	jWebSocket - TokenDefinition (Community Edition, CE)
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
package org.jwebsocket.plugins.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastSet;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The token definition class
 *
 * @author Rolando Santamaria Maso
 * @author Alexander Schulze
 */
public class TokenDefinition implements ITokenizable {

	private String mType;
	/**
	 * wr (with response), nr (no-response), none (not testable from the client)
	 */
	private String mRequestType = "wr";
	private Integer mResponseCode = 0;
	private Set<TokenArgument> mInArgs = new FastSet<TokenArgument>();
	private Set<TokenArgument> mOutArgs = new FastSet<TokenArgument>();
	private String mComment;

	/**
	 * @return The token type
	 */
	public String getType() {
		return mType;
	}

	/**
	 * @param aType The token type to set
	 */
	public void setType(String aType) {
		this.mType = aType;
	}

	/**
	 * @return The response code
	 */
	public Integer getResponseCode() {
		return mResponseCode;
	}

	/**
	 * @param aResponseCode The response code to set
	 */
	public void setResponseCode(Integer aResponseCode) {
		this.mResponseCode = aResponseCode;
	}

	/**
	 * @return The input arguments
	 */
	public Set<TokenArgument> getInArguments() {
		if (mInArgs != null) {
			return Collections.unmodifiableSet(mInArgs);
		} else {
			return null;
		}
	}

	/**
	 * @param aInArgs The input arguments to set
	 */
	public void setInArguments(Set<TokenArgument> aInArgs) {
		this.mInArgs = aInArgs;
	}

	/**
	 * @return The output arguments
	 */
	public Set<TokenArgument> getOutArguments() {
		if (mOutArgs != null) {
			return Collections.unmodifiableSet(mOutArgs);
		} else {
			return null;
		}
	}

	/**
	 * @param aOutArgs The output arguments to set
	 */
	public void setOutArguments(Set<TokenArgument> aOutArgs) {
		this.mOutArgs = aOutArgs;
	}

	/**
	 * @return The comment
	 */
	public String getComment() {
		return mComment;
	}

	/**
	 * @param comment The comment to set
	 */
	public void setComment(String comment) {
		this.mComment = comment;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("type", getType());
		aToken.setString("comment", getComment());
		aToken.setInteger("responseCode", getResponseCode());
		aToken.setString("requestType", getRequestType());

		List<Token> lArgs = new FastList<Token>();
		Token lTempArg;
		for (TokenArgument lInArg : getInArguments()) {
			lTempArg = TokenFactory.createToken();
			lInArg.writeToToken(lTempArg);
			lArgs.add(lTempArg);
		}
		aToken.setList("inArguments", lArgs);

		lArgs = new FastList<Token>();
		for (TokenArgument lOutArg : getOutArguments()) {
			lTempArg = TokenFactory.createToken();
			lOutArg.writeToToken(lTempArg);
			lArgs.add(lTempArg);
		}
		aToken.setList("outArguments", lArgs);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return The request type
	 */
	public String getRequestType() {
		return mRequestType;
	}

	/**
	 * @param aRequestType The request type to set
	 */
	public void setRequestType(String aRequestType) {
		this.mRequestType = aRequestType;
	}
}
