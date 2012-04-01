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
