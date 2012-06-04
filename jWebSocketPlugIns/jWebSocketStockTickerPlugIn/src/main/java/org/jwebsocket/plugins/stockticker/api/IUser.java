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
public interface IUser {
    
    void setUser(String aUser);
    
    void setPass(String aPass);
    
    String getUser();
    
    String getPass();
        
}
