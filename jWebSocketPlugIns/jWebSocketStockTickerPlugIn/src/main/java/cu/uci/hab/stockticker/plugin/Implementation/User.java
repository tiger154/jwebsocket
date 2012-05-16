/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.Implementation;

import cu.uci.hab.stockticker.plugin.api.IUser;
import org.jwebsocket.eventmodel.annotation.ImportFromToken;

/**
 *
 * @author Roy
 */
public class User implements IUser {

    private String mUser;
    private String mPass;
//    private List<IPurchasing> mPurchasing;

    public User(String mUser, String mPass) {
        this.mUser = mUser;
        this.mPass = mPass;
    }

    /**
     * @return the mUser
     */
    @Override
    public String getUser() {
        return mUser;
    }

    /**
     * @param mUser the mUser to set
     */
    @Override
    @ImportFromToken
    public void setUser(String mUser) {
        this.mUser = mUser;
    }

    /**
     * @return the mPass
     */
    @Override
    public String getPass() {
        return mPass;
    }

    /**
     * @param mPass the mPass to set
     */
    @Override
    @ImportFromToken
    public void setPass(String mPass) {
        this.mPass = mPass;
    }
}
