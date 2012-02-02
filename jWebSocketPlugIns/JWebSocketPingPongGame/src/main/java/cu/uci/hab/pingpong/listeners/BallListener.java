/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.listeners;

import cu.uci.hab.pingpong.events.Counter;
import cu.uci.hab.pingpong.events.GameOver;
import cu.uci.hab.pingpong.events.Left;
import cu.uci.hab.pingpong.events.MoveBall;
import cu.uci.hab.pingpong.events.PlayerLeft;
import cu.uci.hab.pingpong.events.PlayerRight;
import cu.uci.hab.pingpong.events.Right;
import cu.uci.hab.pingpong.events.Edge;
import cu.uci.hab.pingpong.events.MovePlayer;
import cu.uci.hab.pingpong.events.Score;
import cu.uci.hab.pingpong.events.Sound;
import cu.uci.hab.pingpong.objects.PingpongMatch;
import cu.uci.hab.pingpong.plugin.PingPongPlugIn;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author armando
 */
public class BallListener implements IListener {

	PingPongPlugIn mPlugIn;

	public BallListener(PingPongPlugIn aPlugIn) {
		this.mPlugIn = aPlugIn;
	}

	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public void processEvent(Edge aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(MoveBall aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(PlayerLeft aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(PlayerRight aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(Right aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(Left aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(GameOver aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.gameOver(aEvent.getPingpongMatch(), aEvent.getBoolean());
	}

	public void processEvent(Score aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.scoreUpdate((PingpongMatch) aEvent.getPingpongMatch());
	}

	public void processEvent(Counter aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.initCounter(aEvent.getPingpongMatch(), aEvent.getCounter());
	}

	public void processEvent(MovePlayer aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.playerUpdate(aEvent.getPingpongMatch(), aEvent.getJoystick());
	}

	public void processEvent(Sound aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.sound(aEvent.getPingpongMatch(), aEvent.getSound());
	}
}
