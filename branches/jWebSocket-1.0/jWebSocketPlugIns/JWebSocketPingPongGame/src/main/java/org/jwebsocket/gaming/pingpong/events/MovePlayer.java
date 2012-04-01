/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.events;

import org.jwebsocket.gaming.pingpong.objects.PingpongMatch;

/**
 *
 * @author armando
 */
public class MovePlayer extends BaseEvent {

	String mJoystick;

	public MovePlayer(PingpongMatch aPingpongMatch, String aJoystick) {
		super(aPingpongMatch);
		this.mJoystick = aJoystick;
	}

	public String getJoystick() {
		return this.mJoystick;
	}
}
