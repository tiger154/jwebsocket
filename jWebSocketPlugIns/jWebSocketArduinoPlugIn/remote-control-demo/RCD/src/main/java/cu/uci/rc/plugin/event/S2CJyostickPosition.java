package cu.uci.rc.plugin.event;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author xdariel
 */
public class S2CJyostickPosition extends S2CEvent {

    private Integer x;
    private Integer y;

    public S2CJyostickPosition(Integer x, Integer y) {
        super();
        this.setId("s2cJyostickPosition");        
        this.x = x;
        this.y = y;

    }



   
    @Override
    public void writeToToken(Token token) {
       token.setInteger("x", getX());
       token.setInteger("y", getY());
    }

  
    public Integer getX() {
        return x;
    }

   
    public void setX(Integer x) {
        this.x = x;
    }

  
    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
