/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.rc.plugin.event;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author xdariel
 */
public class S2CLedState extends S2CEvent {

    private Boolean blue;
    private Boolean red;
    private Boolean green;
    private Boolean yellow;

    public S2CLedState(Boolean blue, Boolean red, Boolean green, Boolean yellow) {
        super();
        this.setId("s2cLedState");
        this.blue = blue;
        this.red = red;
        this.green = green;
        this.yellow = yellow;
    }

    public Boolean getBlue() {
        return blue;
    }

    public Boolean getRed() {
        return red;
    }

    public Boolean getGreen() {
        return green;
    }

    public Boolean getYellow() {
        return yellow;
    }

    @Override
    public void writeToToken(Token token) {
        token.setBoolean("blue", getBlue());
        token.setBoolean("red", getRed());
        token.setBoolean("green", getGreen());
        token.setBoolean("yellow", getYellow());
    }
}
