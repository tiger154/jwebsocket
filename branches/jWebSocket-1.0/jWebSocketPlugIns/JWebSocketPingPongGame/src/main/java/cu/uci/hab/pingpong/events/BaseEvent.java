/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.events;

import cu.uci.hab.pingpong.objects.PingpongMatch;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author kyberneees
 */
public abstract class BaseEvent extends Event {

	private PingpongMatch mPingpongMatch;

	public BaseEvent(PingpongMatch aPingpongMatch) {
		this.mPingpongMatch = aPingpongMatch;
	}

	public PingpongMatch getPingpongMatch() {
		return this.mPingpongMatch;
	}
}
