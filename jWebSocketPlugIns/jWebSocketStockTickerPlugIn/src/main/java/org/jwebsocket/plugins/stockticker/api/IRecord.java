/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.stockticker.api;

import java.util.List;

/**
 *
 * @author Roy
 */
public interface IRecord {

    Integer getId();

    void setId(Integer mId);

    String getName();

    void setName(String aName);

    Double getBid();

    void setBid(Double aBid);

    Double getPrice();

    void setPrice(Double aPrice);

    Double getAsk();

    void setAsk(Double aAsk);

    Double getChng();

    void setChng(Double aChng);

    Integer getTrend();

    void setTrend(Integer aTrend);
    
    List<Double> getHistoy();
    
    void setHistoy(List<Double> mHistoy) ;
}
