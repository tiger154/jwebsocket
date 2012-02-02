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
public class Edge extends BaseEvent {

	public Edge(PingpongMatch aPingpongMatch) {
		super(aPingpongMatch);
	}
}
