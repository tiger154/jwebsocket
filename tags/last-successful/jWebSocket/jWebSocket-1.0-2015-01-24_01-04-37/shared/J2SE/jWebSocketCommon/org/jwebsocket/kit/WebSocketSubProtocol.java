//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketSubProtocol (Community Edition, CE)
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
package org.jwebsocket.kit;

import org.jwebsocket.config.JWebSocketCommonConstants;

/**
 *
 * @author Alexander Schulze
 */
public class WebSocketSubProtocol {

	private String mSubProt = null;
	private String mNameSpace = null;
	private String mFormat = null;
	private WebSocketEncoding mEncoding;

	/**
	 *
	 * @param aSubProt
	 * @param aEncoding
	 */
	public WebSocketSubProtocol(String aSubProt, WebSocketEncoding aEncoding) {
		this.mSubProt = aSubProt;
		this.mEncoding = aEncoding;

		if (JWebSocketCommonConstants.WS_SUBPROT_JSON.equals(aSubProt)) {
			mNameSpace = JWebSocketCommonConstants.WS_SUBPROT_PREFIX;
			mFormat = JWebSocketCommonConstants.WS_FORMAT_JSON;
		} else if (JWebSocketCommonConstants.WS_SUBPROT_XML.equals(aSubProt)) {
			mNameSpace = JWebSocketCommonConstants.WS_SUBPROT_PREFIX;
			mFormat = JWebSocketCommonConstants.WS_FORMAT_XML;
		} else if (JWebSocketCommonConstants.WS_SUBPROT_CSV.equals(aSubProt)) {
			mNameSpace = JWebSocketCommonConstants.WS_SUBPROT_PREFIX;
			mFormat = JWebSocketCommonConstants.WS_FORMAT_CSV;
		} else if (JWebSocketCommonConstants.WS_SUBPROT_TEXT.equals(aSubProt)) {
			mNameSpace = JWebSocketCommonConstants.WS_SUBPROT_PREFIX;
			mFormat = JWebSocketCommonConstants.WS_FORMAT_TEXT;
		} else if (JWebSocketCommonConstants.WS_SUBPROT_BINARY.equals(aSubProt)) {
			mNameSpace = JWebSocketCommonConstants.WS_SUBPROT_PREFIX;
			mFormat = JWebSocketCommonConstants.WS_FORMAT_BINARY;
		}
	}

	@Override
	public int hashCode() {
		return mSubProt.hashCode() + mEncoding.getEncoding();
	}

	@Override
	public boolean equals(Object aObj) {
		if (aObj instanceof WebSocketSubProtocol) {
			WebSocketSubProtocol lOther = (WebSocketSubProtocol) aObj;
			return mSubProt.equals(lOther.mSubProt) && mEncoding.equals(lOther.mEncoding);
		} else {
			return super.equals(aObj);
		}
	}

	@Override
	public String toString() {
		StringBuilder lBuff = new StringBuilder();
		lBuff.append(mSubProt).append('[').append(mEncoding.name()).append("]");
		return lBuff.toString();
	}

	/**
	 * @return the mSubProt
	 */
	public String getSubProt() {
		return mSubProt;
	}

	/**
	 * @return the mEncoding
	 */
	public WebSocketEncoding getEncoding() {
		return mEncoding;
	}

	/**
	 * @return the namespace of the sub protocol
	 */
	public String getNameSpace() {
		return mNameSpace;
	}

	/**
	 * @return the format of the sub protocol
	 */
	public String getFormat() {
		return mFormat;
	}
}
