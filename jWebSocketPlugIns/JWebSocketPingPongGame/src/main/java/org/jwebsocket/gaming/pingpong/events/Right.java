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
public class Right extends BaseEvent {

	public Right(PingpongMatch aPingpongMatch) {
		super(aPingpongMatch);
	}
}
