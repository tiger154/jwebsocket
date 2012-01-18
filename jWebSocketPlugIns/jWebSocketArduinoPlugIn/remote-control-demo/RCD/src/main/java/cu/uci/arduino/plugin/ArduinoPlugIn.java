package cu.uci.arduino.plugin;

import cu.uci.arduino.plugin.event.DataIn;
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

public class ArduinoPlugIn extends ObservableObject implements SerialPortEventListener {

    private InputStream input;
    private OutputStream output;
    private static CommPortIdentifier portId;
    private SerialPort port;
    private static int timeOut = 2000;
    private String portName;
    private int debugRate;
    private int dataBits;
    private int stopBits;
    private int parity;

    public ArduinoPlugIn() {
        addEvents(DataIn.class);
        this.portName = "COM3";
        this.debugRate = 9600;
        this.dataBits = SerialPort.DATABITS_8;
        this.stopBits = SerialPort.STOPBITS_1;
        this.parity = SerialPort.PARITY_NONE;
    }

    public void init() throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException {
        ArduinoPlugIn.portId = CommPortIdentifier.getPortIdentifier(getPortName());
        port = (SerialPort) portId.open("arduino", getTimeOut());
        input = port.getInputStream();
        output = port.getOutputStream();
        port.setSerialPortParams(getDebugRate(),
                getDataBits(),
                getStopBits(),
                getParity());
        port.addEventListener(this);
        port.notifyOnDataAvailable(true);

    }

    public static int getTimeOut() {
        return timeOut;
    }

    public static void setTimeOut(int aTIME_OUT) {
        timeOut = aTIME_OUT;
    }

    public void sendCommand(Integer command) throws IOException {
        output.write(command);

    }

    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        try {
            String data = "";
            switch (oEvent.getEventType()) {

                case SerialPortEvent.DATA_AVAILABLE:

                    while (input.available() > 0) {
                        data += (char) input.read();
                    }
                    //Notice DataIn event to listeners...
                    notify(new DataIn(data), null, true);
                    break;

            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public synchronized void closePort() {
        if (this.port != null) {
            port.removeEventListener();
            port.close();
        }
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getDebugRate() {
        return debugRate;
    }

    public void setDebugRate(int debugRate) {
        this.debugRate = debugRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }
}
