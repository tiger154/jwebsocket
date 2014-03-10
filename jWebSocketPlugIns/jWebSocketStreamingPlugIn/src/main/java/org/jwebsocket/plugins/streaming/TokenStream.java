//	---------------------------------------------------------------------------
//	jWebSocket - Token In- and Outbound Stream (Community Edition, CE)
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
package org.jwebsocket.plugins.streaming;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * implements a stream with a queue of token instances. In addition to the
 * <tt>BaseStream</tt> the <tt>TokenStream</tt> also maintains a reference to a
 * <tt>TokenServer</tt> instance. Unlike the <tt>BaseStream</tt> which has no
 * control or limitation regarding the objects in the Queue, the
 * <tt>TokenStream</tt> allow tokens in the queue only. To support the various
 * sub protocols the TokenStream does not send the queued tokens directly to the
 * client but via the <tt>TokenServer</tt>. The <tt>TokenServer</tt> knows about
 * the used sub protocols of the clients and can decide wether to format them as
 * JSON, CSV or XML. Thus application streams usually are descend from
 * <tt>TokenStream</tt>.
 *
 * @author Alexander Schulze
 */
public class TokenStream extends BaseStream {

	private static final Logger mLog = Logging.getLogger();
	private TokenServer mServer = null;

	/**
	 * creates a new instance of the TokenStream. In Addition to the
	 * <tt>BaseStream</tt> the <tt>TokenStream</tt> also maintains a reference
	 * to a <tt>TokenServer</tt> instance.
	 *
	 * @param aStreamID
	 * @param aServer
	 */
	public TokenStream(String aStreamID, TokenServer aServer) {
		super(aStreamID);
		mServer = aServer;
	}

	@Override
	protected void processConnector(WebSocketConnector aConnector, Object aObject) {
		try {
			getServer().sendToken(aConnector, (Token) aObject);
		} catch (Exception lEx) {
			mLog.error("(processConnector) "
					+ lEx.getClass().getSimpleName()
					+ ": " + lEx.getMessage());
		}
	}

	/**
	 * returns the referenced <tt>TokenServer</tt> instance.
	 *
	 * @return the server
	 */
	public TokenServer getServer() {
		return mServer;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	public Token control(Token aToken) {
		// needs to be implemented in descending classes
		return null;

	}
}
