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
public interface IService {

    Boolean createUser(IUser aUser);

    Boolean login(IUser aUser);
    
    List<IRecord> listRecords();
    
    Boolean sell(String aName,String aCant,String aUserLogin);
    
    Boolean buy(String aName,String aCant,String aUserLogin);
    
    List<IPurchasing> readBuy(String aUser);
    
    List<String> showComb(String aUser);
    
    Integer chart(String aUserLogin ,String aName);
}
