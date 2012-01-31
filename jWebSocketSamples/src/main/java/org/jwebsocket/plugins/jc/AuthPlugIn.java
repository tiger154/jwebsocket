//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jc;

import java.util.Arrays;
import java.util.Map;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.plugin.jc.JcPlugIn;
import org.jwebsocket.eventmodel.plugin.jc.JcResponseCallback;
import org.jwebsocket.eventmodel.s2c.FailureReason;
import org.jwebsocket.eventmodel.s2c.TransactionContext;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.jc.commands.Select;
import org.jwebsocket.plugins.jc.event.DoLogin;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public class AuthPlugIn extends JcPlugIn {

	private static Logger mLog = Logging.getLogger(AuthPlugIn.class);
	private byte[] mAppName = new byte[]{(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18};
	private byte[] mMartaUser = Tools.hexStringToByteArray("E84D34"); //aResponse.getBytes()
	private byte[] mAlexUser = Tools.hexStringToByteArray("EB7D34");  //aResponse.getBytes()

	public void processEvent(DoLogin aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		String lClient = aEvent.getConnector().getId();
		String lTerminal = getTerminals(lClient).toArray()[0].toString();

		TransactionContext lContext = new TransactionContext(getEm(), aEvent, null);
		transmit(lClient, lTerminal, new CommandAPDU(new Select(mAppName).getBytes()), new JcResponseCallback(lContext) {

			@Override
			public boolean isValid(ResponseAPDU aResponse, String aFrom) {
				return true;
			}

			@Override
			public void success(ResponseAPDU aResponse, String aFrom) {
				String lResult = Tools.hexByteArrayToString(aResponse.getBytes());

				if (Arrays.equals(aResponse.getBytes(), mAlexUser)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Authenticating as 'alex' user...");
					}

					Map<String, String> lResponse = new FastMap();
					lResponse.put("user", "alex");
					lResponse.put("password", "jwebsocket2012");

					((TransactionContext) getContext()).success(lResponse);
				} else if (Arrays.equals(aResponse.getBytes(), mMartaUser)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Authenticating as 'marta' user...");
					}

					Map<String, String> lResponse = new FastMap();
					lResponse.put("user", "marta");
					lResponse.put("password", "jws+nfc");

					((TransactionContext) getContext()).success(lResponse);
				}
			}

			@Override
			public void failure(FailureReason aReason, String aFrom) {
				((TransactionContext) getContext()).failure(aReason, "Command execution failed!");
			}
		});
	}
}
