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
public class MoveBall extends BaseEvent {

	public MoveBall(PingpongMatch aPingpongMatch) {
		super(aPingpongMatch);
	}
}
