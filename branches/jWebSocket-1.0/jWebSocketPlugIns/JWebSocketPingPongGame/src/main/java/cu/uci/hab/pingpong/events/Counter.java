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
public class Counter extends BaseEvent {

	int mCount;

	public Counter(PingpongMatch aPingpongMatch, int aCount) {
		super(aPingpongMatch);
		this.mCount = aCount;
	}

	public int getCounter() {
		return this.mCount;
	}
}
