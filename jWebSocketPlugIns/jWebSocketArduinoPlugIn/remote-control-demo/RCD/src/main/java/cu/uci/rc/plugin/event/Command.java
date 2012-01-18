/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.rc.plugin.event;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author xdariel
 */
public class Command extends C2SEvent {
    private Integer order;

  
    public Integer getOrder() {
        return order;
    }

    @ImportFromToken
    public void setOrder(Integer order) {
        this.order = order;
    }
    
    
}
