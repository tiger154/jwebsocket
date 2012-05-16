/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.event;

import cu.uci.hab.stockticker.plugin.api.IUser;
import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author Roy
 */
public class CreateUser extends C2SEvent implements IUser {

    private String mUser;
    private String mPass;
    

    @Override
    @ImportFromToken
    public void setUser(String aUser) {
        mUser = aUser;
    }

    @Override
    @ImportFromToken
    public void setPass(String aPass) {
        mPass = aPass;
    }

    @Override
    public String getUser() {
        return mUser;
    }

    @Override
    public String getPass() {
        return mPass;
    }
}
