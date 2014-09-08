// ---------------------------------------------------------------------------
// jWebSocket - Arduino Remote Control Plug-in  (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
package org.jwebsocket.plugins.arduino;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.exception.InvalidConnectorIdentifier;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.arduino.connection.ArduinoConnection;
import org.jwebsocket.plugins.arduino.connection.event.DataIn;
import org.jwebsocket.plugins.arduino.event.c2s.Command;
import org.jwebsocket.plugins.arduino.event.c2s.StartArduinoRemoteControl;
import org.jwebsocket.plugins.arduino.event.s2c.S2CJoystickPosition;
import org.jwebsocket.plugins.arduino.event.s2c.S2CLedState;
import org.jwebsocket.plugins.arduino.util.JoystickProgram;
import org.jwebsocket.plugins.arduino.util.LedsProgram;

/**
 *
 * @author Alexander Schulze
 */
public class ArduinoRemoteControlPlugIn extends EventModelPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private ArduinoConnection mArduinoConnection;
	private String mPort;

	/**
	 *
	 * @param aPortName
	 */
	public ArduinoRemoteControlPlugIn(String aPortName) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Arduino plug-in (port: " + aPortName + ")...");
		}
		this.mArduinoConnection = new ArduinoConnection(aPortName);
		if (mLog.isInfoEnabled()) {
			mLog.info("Arduino plug-in successfully instantiated (port: " + aPortName + ").");
		}
		this.mPort = aPortName;
	}

	/**
	 *
	 * @return ArduinoConnection
	 */
	public ArduinoConnection getArduinoConnection() {
		return mArduinoConnection;
	}

	/**
	 *
	 * @param aArduinoConnection
	 */
	public void setArduinoConnection(ArduinoConnection aArduinoConnection) {
		this.mArduinoConnection = aArduinoConnection;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws java.lang.Exception
	 */
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

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws InvalidConnectorIdentifier
	 */
	public void processEvent(DataIn aEvent, ResponseEvent aResponseEvent) throws InvalidConnectorIdentifier {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Receiving: " + aEvent.getData() + " from Arduino micro-controller");
		}
		char lId = aEvent.getData().charAt(0);
		String ldata = aEvent.getData().split("/")[1];

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

	/**
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws IOException
	 * @throws InvalidConnectorIdentifier
	 */
	public void processEvent(Command aEvent, C2SResponseEvent aResponseEvent) throws IOException, InvalidConnectorIdentifier {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing command event (cmd: " + aEvent.getCmd() + ")...");
		}
		try {
			// Sends a command to Arduino
			mArduinoConnection.sendCommand(aEvent.getCmd());
		} catch (IOException ex) {
			aResponseEvent.getArgs().setString("message", "The command could not be sent to Arduino.");
		} catch (NullPointerException ex) {
			aResponseEvent.getArgs().setString("message", "Arduino instance could not be created.");
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(ConnectorStopped aEvent, ResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("aConnector: " + aEvent.getId() + "Stopped");
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(StartArduinoRemoteControl aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing start-rc event...");
		}
		try {
			// Requests the status of the LEDs
			mArduinoConnection.sendCommand(53);
		} catch (IOException ex) {
			aResponseEvent.getArgs().setString("message", "Unable to communicate with Arduino: " + ex.getMessage());
		} catch (NullPointerException ex) {
			aResponseEvent.getArgs().setString("message", "Unable to communicate with Arduino: The microcontroller is not connected to port: " + this.mPort);
		}
	}

	/**
	 *
	 * @param aBlue
	 * @param aRed
	 * @param aGreen
	 * @param aYellow
	 * @throws InvalidConnectorIdentifier
	 */
	private void sendLedState(Boolean aBlue, Boolean aRed, Boolean aGreen, Boolean aYellow) throws InvalidConnectorIdentifier {
		// Notified with an event status LEDs
		for (WebSocketConnector lClient : this.getServerAllConnectors().values()) {
			this.notifyS2CEvent(new S2CLedState(aBlue,
					aRed,
					aGreen,
					aYellow)).to(lClient, null);
		}
	}

	/**
	 *
	 * @param aX
	 * @param aY
	 * @throws InvalidConnectorIdentifier
	 */
	private void sendJoystickPosition(Integer aX, Integer aY) throws InvalidConnectorIdentifier {
		//Notified with an event the Position,(x,y) of the joystick
		for (WebSocketConnector lClient : this.getServerAllConnectors().values()) {
			this.notifyS2CEvent(new S2CJoystickPosition(aX, aY)).to(lClient, null);
		}
	}
}
