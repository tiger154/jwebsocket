//	---------------------------------------------------------------------------
//	jWebSocket - PlugInResponse (Community Edition, CE)
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

/**
 * Implements the response class to return results from the plug-in chain to the
 * server. The server can forward data packets to a chain of plug-ins. Each
 * plug-in can either process or ignore the packet. If the packet was
 * successfully processed the plug-in can abort the chain.
 *
 * @author Alexander Schulze
 */
public class PlugInResponse {

	private boolean mChainAborted = false;
	private boolean mTokenProcessed = false;
	private int mReturnCode = -1;
	private String mMessage = null;

	/**
	 * Returns if the plug-in chain has to be aborted after a plug-in has
	 * finished its work.
	 *
	 * @return the chainAborted
	 */
	public Boolean isChainAborted() {
		return mChainAborted;
	}

	/**
	 * Signals that the plug-in chain has to be be aborted. The token has not
	 * been processed.
	 */
	public void abortChain() {
		this.mChainAborted = true;
		this.mTokenProcessed = false;
	}

	/**
	 * Signals that the plug-in chain has to be be aborted. The token has been
	 * processed.
	 */
	public void breakChain() {
		this.mChainAborted = true;
		this.mTokenProcessed = true;
	}

	/**
	 * Signals that the plug-in chain has to be be continued.
	 */
	public void continueChain() {
		this.mChainAborted = false;
	}

	/**
	 * @return the mReturnCode
	 */
	public int getReturnCode() {
		return mReturnCode;
	}

	/**
	 *
	 * @param aReturnCode
	 */
	public void setReturnCode(int aReturnCode) {
		this.mReturnCode = aReturnCode;
	}

	/**
	 * @return the mMessage
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 *
	 * @param aMessage
	 */
	public void setMessage(String aMessage) {
		this.mMessage = aMessage;
	}
}
