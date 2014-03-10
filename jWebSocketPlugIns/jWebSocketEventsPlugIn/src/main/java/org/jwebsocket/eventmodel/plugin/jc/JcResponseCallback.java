//	---------------------------------------------------------------------------
//	jWebSocket - JcResponseCallback (Community Edition, CE)
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
package org.jwebsocket.eventmodel.plugin.jc;

import javax.smartcardio.ResponseAPDU;
import org.jwebsocket.eventmodel.s2c.OnResponse;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class JcResponseCallback extends OnResponse {

	/**
	 *
	 * @param aContext
	 */
	public JcResponseCallback(Object aContext) {
		super(aContext);
	}

	/**
	 *
	 */
	public JcResponseCallback() {
		super();
	}

	/**
	 * Execute custom validations in client responses
	 *
	 * @param aResponse The response to validate
	 * @param aFrom The target client connector
	 * @return
	 */
	@Override
	public boolean isValid(Object aResponse, String aFrom) {
		return isValid(new ResponseAPDU(Tools.hexStringToByteArray(aResponse.toString())), aFrom);
	}

	/**
	 * Execute custom validations for client card calls responses
	 *
	 * @param aResponse The response to validate
	 * @param aFrom The target client connector
	 * @return
	 */
	public boolean isValid(ResponseAPDU aResponse, String aFrom) {
		if (aResponse.getSW() == 0x9000) {
			return true;
		}

		return false;
	}

	/**
	 * Callback used to handle the success response from the client
	 *
	 * @param aResponse The response returned by the client-side
	 * @param aFrom The target client connector
	 */
	@Override
	public void success(Object aResponse, String aFrom) {
		success(new ResponseAPDU(Tools.hexStringToByteArray(aResponse.toString())), aFrom);
	}

	/**
	 * Callback used to handle the success response from the client card
	 *
	 * @param aResponse The response returned by the client-side
	 * @param aFrom The target client connector
	 */
	public void success(ResponseAPDU aResponse, String aFrom) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
