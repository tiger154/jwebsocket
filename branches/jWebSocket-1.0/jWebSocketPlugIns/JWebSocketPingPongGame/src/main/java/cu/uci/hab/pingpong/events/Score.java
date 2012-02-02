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
public class Score extends BaseEvent {

	public Score(PingpongMatch aPingpongMatch) {
		super(aPingpongMatch);
	}
}
