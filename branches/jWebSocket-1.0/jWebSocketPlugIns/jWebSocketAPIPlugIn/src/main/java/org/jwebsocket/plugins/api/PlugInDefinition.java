//  ---------------------------------------------------------------------------
//  jWebSocket - PlugInDefinition
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javolution.util.FastList;
import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The plug-in definition class
 *
 * @author kyberneees
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
