/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.events;

import cu.uci.hab.pingpong.objects.PingpongMatch;

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
