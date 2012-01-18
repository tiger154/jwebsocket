
package cu.uci.arduino.plugin.event;

import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author xdariel
 */
public class DataIn extends Event {
    private String mData;

    public DataIn(String aData) {
        this.mData = aData;
    }

    public String getData() {
        return mData;
    }
}
