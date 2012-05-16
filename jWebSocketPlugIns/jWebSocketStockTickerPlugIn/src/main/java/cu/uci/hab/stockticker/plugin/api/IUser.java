/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.api;

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
