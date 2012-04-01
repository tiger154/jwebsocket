/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.api;

import java.util.Date;

/**
 *
 * @author armando
 */
public abstract class Match {

	protected Date mCreationDate;
	protected boolean mState;

	public Date getCreationDate() {
		return this.mCreationDate;
	}

	public void setCreationDate(Date aCreationDate) {
		this.mCreationDate = aCreationDate;
	}

	public boolean getState() {
		return this.mState;
	}

	public void setState(boolean state) {
		this.mState = state;
	}
}
