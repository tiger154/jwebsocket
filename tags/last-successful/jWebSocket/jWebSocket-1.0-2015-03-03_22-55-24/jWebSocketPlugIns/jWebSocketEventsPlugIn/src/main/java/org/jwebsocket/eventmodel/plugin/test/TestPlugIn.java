//	---------------------------------------------------------------------------
//	jWebSocket - TestPlugIn (Community Edition, CE)
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
package org.jwebsocket.eventmodel.plugin.test;

import java.util.Map;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.test.*;
import org.jwebsocket.eventmodel.exception.InvalidConnectorIdentifier;
import org.jwebsocket.eventmodel.plugin.jc.JcPlugIn;
import org.jwebsocket.eventmodel.plugin.jc.JcResponseCallback;
import org.jwebsocket.eventmodel.s2c.FailureReason;
import org.jwebsocket.eventmodel.s2c.OnResponse;
import org.jwebsocket.eventmodel.s2c.TransactionContext;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class TestPlugIn extends JcPlugIn {

	private static Logger mLog = Logging.getLogger(TestPlugIn.class);

	/**
	 * Return the hash-code for a custom text
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(GetHashCode aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		aResponseEvent.getArgs().setInteger("hash_code", aEvent.getText().hashCode());
	}

	/**
	 * Return the EventsPlugIn name and version
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(GetEventsInfo aEvent, C2SResponseEvent aResponseEvent) {
		Map lInfo = new FastMap();
		lInfo.put("name", "EventsPlugIn");
		lInfo.put("version", "1.0");

		aResponseEvent.getArgs().setMap("table", lInfo);
	}

	/**
	 * Execute a s2c call
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws InvalidConnectorIdentifier
	 */
	public void processEvent(S2CNotification aEvent, C2SResponseEvent aResponseEvent) throws InvalidConnectorIdentifier {
		//Notification with callbacks
		notifyS2CEvent(new S2CPlusXYEvent(5, 5)).to(aEvent.getConnector(),
				new OnResponse(new TransactionContext(getEm(), aEvent, null)) {
			@Override
			public boolean isValid(Object aResponse, String aFrom) {
				return aResponse.equals(10);
			}

			@Override
			public void success(Object aResponse, String aFrom) {
				((TransactionContext) getContext()).success(aResponse);
			}

			@Override
			public void failure(FailureReason aReason, String aFrom) {
				mLog.error("The S2C event notification failed. Reason: " + aReason.name());
			}
		});
	}

	/**
	 * Test the JavaCard support on the client by sending an arbitrary APDU
	 * command
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(JcTest aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing JcTest event notification...");
		}

		byte[] lApdu = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x43, (byte) 0x4d};

		String lClient = aEvent.getConnector().getId();

		for (String lTerminal : getTerminals(lClient)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Sending '" + lApdu.toString() + "' APDU to '" + lTerminal + "' terminal on '" + lClient + "' client ...");
			}

			transmit(lClient, lTerminal, new CommandAPDU(lApdu), new JcResponseCallback() {
				@Override
				public void success(ResponseAPDU aResponse, String aFrom) {
				}

				@Override
				public void failure(FailureReason aReason, String aFrom) {
					mLog.error("The S2C event notification failed. Reason: " + aReason.name());
				}
			});
		}
	}
}
