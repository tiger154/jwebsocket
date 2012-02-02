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
public class Left extends BaseEvent {

	public Left(PingpongMatch aPingpongMatch) {
		super(aPingpongMatch);
	}
}
