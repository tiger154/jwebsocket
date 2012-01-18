package cu.uci.rc.plugin;

import cu.uci.arduino.plugin.ArduinoPlugIn;
import cu.uci.arduino.plugin.event.DataIn;
import cu.uci.rc.plugin.arduino.program.LedsProgram;
import cu.uci.rc.plugin.event.Command;

import cu.uci.rc.plugin.event.S2CLedState;
import cu.uci.rc.plugin.event.S2CMsg;
import cu.uci.rc.plugin.event.StartRC;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.TooManyListenersException;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.exception.MissingTokenSender;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;

/**
 *
 * @author xdariel
 */
public class RCPlugIn extends EventModelPlugIn {

    static org.apache.log4j.Logger rcLog = Logger.getLogger(RCPlugIn.class);
    private ArduinoPlugIn arduino;
    static WebSocketConnector client;

    public RCPlugIn() throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException {
        super();

        this.arduino = new ArduinoPlugIn();
    }

    @Override
    public void initialize() throws Exception {
        this.arduino.init();
        this.arduino.on(DataIn.class, this);
    }

    public void processEvent(DataIn aEvent, ResponseEvent aResponseEvent) throws MissingTokenSender {
        
        if ((int)aEvent.getData().charAt(0)>=97 && (int)aEvent.getData().charAt(0)<=112 ) {
            Boolean[] state = LedsProgram.parseLedState(aEvent.getData().charAt(0));
            ledState2Client(state[0], state[1], state[2], state[3]);
        }
      
          String ubication = aEvent.getData();
         
          message2Client(ubication);
        
        

    }

    public void processEvent(Command aEvent, C2SResponseEvent aResponseEvent) throws Exception {
        if (rcLog.isDebugEnabled()) {
            rcLog.debug(">> Processing command event. . . ");
        }
        arduino.sendCommand(aEvent.getOrder());
    }

    public void processEvent(StartRC aEvent, C2SResponseEvent aResponseEvent) throws MissingTokenSender, IOException {
        if (rcLog.isDebugEnabled()) {
            rcLog.debug(">> Processing start-rc event. . . ");
        }
        RCPlugIn.client = aEvent.getConnector();
        arduino.sendCommand(53);
    }

    private void ledState2Client(Boolean blue, Boolean red, Boolean green, Boolean yellow) throws MissingTokenSender {
        this.notifyS2CEvent(new S2CLedState(blue,
                red,
                green,
                yellow)).to(RCPlugIn.client, null);
    }

    private void message2Client(String msg) throws MissingTokenSender {
        this.notifyS2CEvent(new S2CMsg(msg)).to(RCPlugIn.client, null);
    }
}
