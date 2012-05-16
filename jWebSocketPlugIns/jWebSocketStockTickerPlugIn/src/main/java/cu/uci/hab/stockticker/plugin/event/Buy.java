/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.event;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author Roy
 */
public class Buy extends C2SEvent {

    private String mName;
    private String mCant;

    /**
     * @return the mCant
     */
    public String getCant() {
        return mCant;
    }

    /**
     * @param mCant the mCant to set
     */
    @ImportFromToken
    public void setCant(String mCant) {
        this.mCant = mCant;
    }

    /**
     * @return the mName
     */
    public String getName() {
        return mName;
    }

    /**
     * @param mName the mName to set
     */
    @ImportFromToken
    public void setName(String mName) {
        this.mName = mName;
    }
}
