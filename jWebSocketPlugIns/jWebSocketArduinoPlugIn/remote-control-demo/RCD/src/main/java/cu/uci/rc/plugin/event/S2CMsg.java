package cu.uci.rc.plugin.event;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author xdariel
 */
public class S2CMsg extends S2CEvent {

    private String msg;

    public S2CMsg(String msg) {
        super();
        this.setId("s2cMsg");        
        this.msg = msg;

    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

   
    @Override
    public void writeToToken(Token token) {
       token.setString("msg", getMsg());
    }
}
