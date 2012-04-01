/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.api;

/**
 *
 * @author armando
 */
public abstract class Player extends MatchObject {

	protected int mPlayerScore;

	public int getPlayerScore() {
		return this.mPlayerScore;
	}

	public void setPlayerScore(int aPlayerScore) {
		this.mPlayerScore = aPlayerScore;
	}
}
