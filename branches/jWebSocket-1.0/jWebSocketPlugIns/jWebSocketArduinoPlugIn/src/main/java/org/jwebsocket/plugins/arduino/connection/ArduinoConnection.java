// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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
package org.jwebsocket.plugins.arduino.connection;

import org.jwebsocket.plugins.arduino.connection.event.DataIn;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import org.jwebsocket.eventmodel.observable.ObservableObject;

/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
public class ArduinoConnection extends ObservableObject implements SerialPortEventListener {

	private InputStream mInput;
	private OutputStream mOutput;
	private CommPortIdentifier mPortId;
	private SerialPort mPort;
	private int mTimeOut = 2000;
	private String mPortName;
	private int mDebugRate;
	private int mDataBits;
	private int mStopBits;
	private int mParity;
	private byte[] mBuffer;

	public ArduinoConnection(String aPortName) {
		addEvents(DataIn.class);
		//COM0,...,COM4 for windows
		//in Mac would probably be something like /dev/tty.usbserial-1B1
		//in Linux, it should be /dev/ttyUSB0, /dev/ttyUSB1 or similar.
		this.mPortName = aPortName;
		//default debug rate: 9600 bps
		this.mDebugRate = 9600;
		this.mDataBits = SerialPort.DATABITS_8;
		this.mStopBits = SerialPort.STOPBITS_1;
		this.mParity = SerialPort.PARITY_NONE;
		this.mBuffer = new byte[1024];
	}

	public void init() throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException {
		mPortId = CommPortIdentifier.getPortIdentifier(getPortName());
		//Open the serial port for data exchange
		mPort = (SerialPort) mPortId.open("arduino", getTimeOut());
		//Get input port for receiving data
		mInput = mPort.getInputStream();
		//Get output port to send data
		mOutput = mPort.getOutputStream();
		//Set the port settings for the exchange
		mPort.setSerialPortParams(getDebugRate(),
				getDataBits(),
				getStopBits(),
				getParity());
		mPort.addEventListener(this);
		mPort.notifyOnDataAvailable(true);
	}

	public int getTimeOut() {
		return mTimeOut;
	}

	public void setTimeOut(int aTimeOut) {
		mTimeOut = aTimeOut;
	}

	public void sendCommand(Integer aCmd) throws IOException {
		//Write a simple Integer as ASCII code in the Port
		mOutput.write(aCmd);
	}

	public void sendCommand(String aCmd) throws IOException {
		mOutput.write(aCmd.getBytes());
	}

	@Override
	public void serialEvent(SerialPortEvent aEvent) {
		try {
			switch (aEvent.getEventType()) {
				//Notifies when there is data in the port
				case SerialPortEvent.DATA_AVAILABLE:
					int data;
					int len = 0;
					// Read character to character when there is data
					while ((data = mInput.read()) > -1) {
						if (data == '\n') {// 
							break;
						}
						//Stored in a buffer of bytes read from port
						mBuffer[len++] = (byte) data;
					}
					//Notice DataIn event to listeners...  					
					notify(new DataIn(new String(mBuffer, 0, len)), null, true);
					break;
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	public void closePort() {
		if (this.mPort != null) {
			// Remove all Event listeners
			mPort.removeEventListener();
			mPort.close();
		}
	}

	public String getPortName() {
		return mPortName;
	}

	public void setPortName(String portName) {
		this.mPortName = portName;
	}

	public int getDebugRate() {
		return mDebugRate;
	}

	public void setDebugRate(int debugRate) {
		this.mDebugRate = debugRate;
	}

	public int getDataBits() {
		return mDataBits;
	}

	public void setDataBits(int dataBits) {
		this.mDataBits = dataBits;
	}

	public int getStopBits() {
		return mStopBits;
	}

	public void setStopBits(int stopBits) {
		this.mStopBits = stopBits;
	}

	public int getParity() {
		return mParity;
	}

	public void setParity(int parity) {
		this.mParity = parity;
	}
}
