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
public class GameOver extends BaseEvent {

	boolean mBool;

	public GameOver(PingpongMatch aPingpongMatch, boolean aBool) {
		super(aPingpongMatch);
		this.mBool = aBool;
	}

	public boolean getBoolean() {
		return this.mBool;
	}
}
