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
public class Sound extends BaseEvent {

	private int mSound;

	public Sound(PingpongMatch aPingpongMatch, int aSound) {
		super(aPingpongMatch);
		this.mSound = aSound;
	}

	public int getSound() {
		return this.mSound;
	}
}
