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
public class PlayerRight extends BaseEvent {

	public PlayerRight(PingpongMatch aPingpongMatch) {
		super(aPingpongMatch);
	}
}
