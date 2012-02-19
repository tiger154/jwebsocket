// ---------------------------------------------------------------------------
// jWebSocket - ArduinoRemoteControlPlugIn
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
package org.jwebsocket.plugins.arduino;

import org.jwebsocket.plugins.arduino.connection.ArduinoConnection;
import org.jwebsocket.plugins.arduino.connection.event.DataIn;
import org.jwebsocket.plugins.arduino.event.c2s.Command;
import org.jwebsocket.plugins.arduino.event.s2c.S2CJoystickPosition;
import org.jwebsocket.plugins.arduino.event.s2c.S2CLedState;
import org.jwebsocket.plugins.arduino.event.c2s.StartArduinoRemoteControl;
import java.io.IOException;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.exception.MissingTokenSenderException;
import org.jwebsocket.plugins.arduino.util.JoystickProgram;
import org.jwebsocket.plugins.arduino.util.LedsProgram;

public class ArduinoRemoteControlPlugIn extends EventModelPlugIn {

	static org.apache.log4j.Logger mLog = Logger.getLogger(ArduinoRemoteControlPlugIn.class);
	private ArduinoConnection mArduinoConnection;

	public ArduinoRemoteControlPlugIn(String aPortName) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Arduino plug-in (port: " + aPortName + ")...");
		}
		this.mArduinoConnection = new ArduinoConnection(aPortName);
		if (mLog.isInfoEnabled()) {
			mLog.info("Arduino plug-in successfully instantiated (port: " + aPortName + ").");
		}
	}

	public ArduinoConnection getArduinoConnection() {
		return mArduinoConnection;
	}

	public void setArduinoConnection(ArduinoConnection aArduinoConnection) {
		this.mArduinoConnection = aArduinoConnection;

	}

	@Override
	public void initialize() throws Exception {
		if (mLog.isInfoEnabled()) {
			mLog.info("Initializing the parameters for communication with Arduino...");
		}
		try {
			this.mArduinoConnection.init();
			this.mArduinoConnection.on(DataIn.class, this);
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on starting Arduino: " + ex.getMessage());
		}
	}

	public void processEvent(DataIn aEvent, ResponseEvent aResponseEvent) throws MissingTokenSenderException {

		char lId = aEvent.getData().charAt(0);
		String ldata = aEvent.getData().split("-")[1];

		switch (lId) {
			case 'L':
				Boolean[] lLedsState = LedsProgram.parseLedState(Integer.valueOf(ldata.substring(0, ldata.length() - 1)));
				sendLedState(lLedsState[0], lLedsState[1], lLedsState[2], lLedsState[3]);
				break;

			case 'J':
				Integer[] lTrunkValues = JoystickProgram.treatValues(ldata);
				sendJoystickPosition(lTrunkValues[0], lTrunkValues[1]);
				break;
		}
	}

	public void processEvent(Command aEvent, C2SResponseEvent aResponseEvent) throws IOException, MissingTokenSenderException {
		if (mLog.isInfoEnabled()) {
			mLog.info("Processing command event (cmd: " + aEvent.getCmd() + ")...");
		}
		try {
			// Sends a command to Arduino
			mArduinoConnection.sendCommand(aEvent.getCmd());
		} catch (IOException ex) {
			aResponseEvent.getArgs().setString("message", "The command could not be sent to Arduino.");
		}
	}

	public void processEvent(StartArduinoRemoteControl aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isInfoEnabled()) {
			mLog.info("Processing start-rc event...");
		}
		try {
			// Requests the status of the LEDs
			mArduinoConnection.sendCommand(53);
		} catch (IOException ex) {
			aResponseEvent.getArgs().setString("message", "Unable to communicate with Arduino: " + ex.getMessage());
		} catch (NullPointerException ex) {
			aResponseEvent.getArgs().setString("message", "Unable to communicate with Arduino: " + ex.getMessage());
		}
	}

	private void sendLedState(Boolean aBlue, Boolean aRed, Boolean aGreen, Boolean aYellow) throws MissingTokenSenderException {
		// Notified with an event status LEDs
		for (WebSocketConnector lClient : this.getServerAllConnectors().values()) {
			this.notifyS2CEvent(new S2CLedState(aBlue,
					aRed,
					aGreen,
					aYellow)).to(lClient, null);
		}
	}

	private void sendJoystickPosition(Integer aX, Integer aY) throws MissingTokenSenderException {
		//Notified with an event the Position,(x,y) of the joystick
		for (WebSocketConnector lClient : this.getServerAllConnectors().values()) {
			this.notifyS2CEvent(new S2CJoystickPosition(aX, aY)).to(lClient, null);
		}
	}
}
