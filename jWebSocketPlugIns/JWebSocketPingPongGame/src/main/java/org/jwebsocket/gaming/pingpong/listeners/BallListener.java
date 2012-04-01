/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.listeners;

import org.jwebsocket.gaming.pingpong.events.Counter;
import org.jwebsocket.gaming.pingpong.events.GameOver;
import org.jwebsocket.gaming.pingpong.events.Left;
import org.jwebsocket.gaming.pingpong.events.MoveBall;
import org.jwebsocket.gaming.pingpong.events.PlayerLeft;
import org.jwebsocket.gaming.pingpong.events.PlayerRight;
import org.jwebsocket.gaming.pingpong.events.Right;
import org.jwebsocket.gaming.pingpong.events.Edge;
import org.jwebsocket.gaming.pingpong.events.MovePlayer;
import org.jwebsocket.gaming.pingpong.events.Score;
import org.jwebsocket.gaming.pingpong.events.Sound;
import org.jwebsocket.gaming.pingpong.objects.PingpongMatch;
import org.jwebsocket.gaming.pingpong.plugin.PingPongPlugIn;
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
