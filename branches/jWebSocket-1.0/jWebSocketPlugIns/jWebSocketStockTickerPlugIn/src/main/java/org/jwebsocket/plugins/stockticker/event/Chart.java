/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.stockticker.event;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author Roy
 */
public class Chart extends C2SEvent {

    private String mNamechart;

    /**
     * @return the mName
     */
    public String getNamechart() {
        return mNamechart;
    }

    /**
     * @param mName the mName to set
     */
    @ImportFromToken
    public void setNamechart(String aName) {
        this.mNamechart= aName;
    }
}
