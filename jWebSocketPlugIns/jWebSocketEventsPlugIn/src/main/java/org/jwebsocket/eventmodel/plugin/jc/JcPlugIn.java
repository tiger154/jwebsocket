//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.eventmodel.plugin.jc;

import java.util.Map;
import java.util.Set;
import javax.smartcardio.CommandAPDU;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.commons.codec.binary.Base64;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.event.card.JcTerminalNotReady;
import org.jwebsocket.eventmodel.event.card.JcTerminalReady;
import org.jwebsocket.eventmodel.exception.MissingTokenSender;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
 * 
 * Usage:
 * -----------------------
 *		byte[] lApdu = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x08,
//			(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//
//		String lClient = aConnectorId;
//		String lTerminal = aTerminal;
//		transmit(lClient, lTerminal, new CommandAPDU(lApdu), new JcResponseCallback(null) {
//
//			@Override
//			public void success(ResponseAPDU response, String from) {
//				System.out.println(">> success " + from + " " + response.getBytes());
//			}
//
//			@Override
//			public void failure(FailureReason reason, String from) {
//				System.out.println(">> failure " + from + " " + reason.name());
//			}
//		});
 */
public class JcPlugIn extends EventModelPlugIn {

	/**
	 * @TODO: Replace this by storages to support clusters
	 */
	private Map<String, Set<String>> terminals = new FastMap<String, Set<String>>().shared();

	public Set<String> getTerminals(String aConnectorId) {
		if (!terminals.containsKey(aConnectorId)) {
			terminals.put(aConnectorId, new FastSet<String>());
		}
		return terminals.get(aConnectorId);
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

		private byte[] bytes;
		private String terminalId;

		public TransmitEvent(byte[] aBytes, String aTerminalId) {
			this.bytes = aBytes;
			this.terminalId = aTerminalId;

			setResponseType("string");
			setId("transmit");
			setTimeout(5000);
		}

		@Override
		public void writeToToken(Token aToken) {
			//Base64 encoded to save the bytes integrity
			aToken.setString("apdu", Base64.encodeBase64String(bytes));
			aToken.setString("terminal", terminalId);
		}
	}

	/**
	 * Transmit a CommandAPDU to the client smartcard terminals
	 * 
	 * @param aConnector
	 * @param aTerminalId
	 * @param aCommand
	 * @param aCallback
	 * @throws MissingTokenSender 
	 */
	public void transmit(String aConnectorId, String aTerminalId,
			CommandAPDU aCommand, JcResponseCallback aCallback) throws MissingTokenSender {
		this.notifyS2CEvent(new TransmitEvent(aCommand.getBytes(), aTerminalId)).to(aConnectorId, aCallback);
	}

	public void processEvent(JcTerminalNotReady aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		unregisterTerminal(aEvent.getConnector().getId(), aEvent.getTerminal());
	}

	public void processEvent(JcTerminalReady aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		registerTerminal(aEvent.getConnector().getId(), aEvent.getTerminal());
	}
}
