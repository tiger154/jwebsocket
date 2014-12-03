//	---------------------------------------------------------------------------
//	jWebSocket - PlugInDefinition (Community Edition, CE)
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
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The plug-in definition class
 *
 * @author Rolando Santamaria Maso
 */
public class PlugInDefinition implements ITokenizable {

	private String mId;
	private String mNamespace;
	private Set<TokenDefinition> mSupportedTokens;
	private String mComment;

	/**
	 * @return The plug-in identifier
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param aId The plug-in identifier to set
	 */
	public void setId(String aId) {
		this.mId = aId;
	}

	/**
	 * @return The supported tokens definition
	 */
	public Set<TokenDefinition> getSupportedTokens() {
		return Collections.unmodifiableSet(mSupportedTokens);
	}

	/**
	 * @param aSupportedTokens The supported tokens definitions to set
	 */
	public void setSupportedTokens(Set<TokenDefinition> aSupportedTokens) {
		this.mSupportedTokens = aSupportedTokens;
	}

	/**
	 * @return The plug-in comment
	 */
	public String getComment() {
		return mComment;
	}

	/**
	 *
	 * @param aTokenType The token type
	 * @return <tt>TRUE</tt> if the token is supported, <tt>FALSE</tt> otherwise
	 */
	public boolean supportsToken(String aTokenType) {
		for (TokenDefinition lTokenDef : getSupportedTokens()) {
			if (lTokenDef.getType().equals(aTokenType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param aComment The plug-in comment to set
	 */
	public void setComment(String aComment) {
		this.mComment = aComment;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aToken
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("id", getId());
		aToken.setString("namespace", getNamespace());
		aToken.setString("comment", getComment());

		List<Token> lTokens = new FastList<Token>();
		Token lTempToken;
		for (TokenDefinition lTokenDef : getSupportedTokens()) {
			lTempToken = TokenFactory.createToken();
			lTokenDef.writeToToken(lTempToken);
			lTokens.add(lTempToken);
		}

		aToken.setList("supportedTokens", lTokens);
	}

	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return The plug-in namespace
	 */
	public String getNamespace() {
		return mNamespace;
	}

	/**
	 * @param aNamespace The plug-in namespace
	 */
	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}
}
