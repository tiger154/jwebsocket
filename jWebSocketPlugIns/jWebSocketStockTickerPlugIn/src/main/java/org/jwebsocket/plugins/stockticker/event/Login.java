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
public class Login extends C2SEvent {

	private String mUser;
	private String mPass;

	/**
	 *
	 * @param aUser
	 */
	@ImportFromToken
	public void setUser(String aUser) {
		mUser = aUser;
	}

	/**
	 *
	 * @param aPass
	 */
	@ImportFromToken
	public void setPass(String aPass) {
		mPass = aPass;
	}

	/**
	 *
	 * @return
	 */
	public String getUser() {
		return mUser;
	}

	/**
	 *
	 * @return
	 */
	public String getPass() {
		return mPass;
	}
}
