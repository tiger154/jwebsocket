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
