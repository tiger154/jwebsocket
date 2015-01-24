//	---------------------------------------------------------------------------
//	jWebSocket - JcPlugIn (Community Edition, CE)
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

import java.util.Map;
import java.util.Set;
import javax.smartcardio.CommandAPDU;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.event.card.JcTerminalNotReady;
import org.jwebsocket.eventmodel.event.card.JcTerminalReady;
import org.jwebsocket.eventmodel.exception.InvalidConnectorIdentifier;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 *
 * Usage: ----------------------- byte[] lApdu = {(byte) 0x00, (byte) 0xA4,
 * (byte) 0x04, (byte) 0x00, (byte) 0x08, //	(byte) 0xA0, (byte) 0x00, (byte)
 * 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00}; // //
 * String lClient = aConnectorId; //	String lTerminal = aTerminal; //
 * transmit(lClient, lTerminal, new CommandAPDU(lApdu), new
 * JcResponseCallback(null) { // //
 * @Override //	public void success(ResponseAPDU response, String from) { //
 * System.out.println("success " + from + " " + response.getBytes()); //	} // //
 * @Override //	public void failure(FailureReason reason, String from) { //
 * System.out.println("failure " + from + " " + reason.name()); //	} //	});
 */
public class JcPlugIn extends EventModelPlugIn {

	/**
	 * @TODO: Replace this by storages to support clusters
	 */
	private Map<String, Set<String>> mTerminals = new FastMap<String, Set<String>>().shared();

	/**
	 *
	 * @param aConnectorId
	 * @return
	 */
	public Set<String> getTerminals(String aConnectorId) {
		if (!mTerminals.containsKey(aConnectorId)) {
			mTerminals.put(aConnectorId, new FastSet<String>());
		}
		return mTerminals.get(aConnectorId);
	}

	/**
	 * Register a terminal on a connector as ready
	 *
	 * @param aConnectorId
	 * @param aTerminal The terminal name
	 */
	public void registerTerminal(String aConnectorId, String aTerminal) {
		getTerminals(aConnectorId).add(aTerminal);
	}

	/**
	 * Unregister a terminal on a connector
	 *
	 * @param aConnectorId
	 * @param aTerminal The terminal name
	 */
	public void unregisterTerminal(String aConnectorId, String aTerminal) {
		getTerminals(aConnectorId).remove(aTerminal);
	}

	/**
	 * The s2c event that is sent to the client with the APDU command
	 */
	class TransmitEvent extends S2CEvent {

		private byte[] mBytes;
		private String mTerminalId;

		public TransmitEvent(byte[] aBytes, String aTerminalId) {
			this.mBytes = aBytes;
			this.mTerminalId = aTerminalId;

			setResponseType("string");
			setId("transmit");
			setTimeout(10000);
		}

		@Override
		public void writeToToken(Token aToken) {
			//Base64 encoded to save the bytes integrity
			aToken.setString("apdu", Tools.hexByteArrayToString(mBytes));
			aToken.setString("terminal", mTerminalId);
		}
	}

	/**
	 * Transmit a CommandAPDU to the client smartcard terminals
	 *
	 * @param aConnectorId
	 * @param aTerminalId
	 * @param aCommand
	 * @param aCallback
	 * @throws InvalidConnectorIdentifier
	 */
	public void transmit(String aConnectorId, String aTerminalId, CommandAPDU aCommand, JcResponseCallback aCallback) throws InvalidConnectorIdentifier {
		this.notifyS2CEvent(new TransmitEvent(aCommand.getBytes(), aTerminalId)).to(aConnectorId, aCallback);
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(JcTerminalNotReady aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		unregisterTerminal(aEvent.getConnector().getId(), aEvent.getTerminal());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(JcTerminalReady aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		registerTerminal(aEvent.getConnector().getId(), aEvent.getTerminal());
	}
}
