//	---------------------------------------------------------------------------
//	jWebSocket - BallListener (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
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

	/**
	 *
	 * @param aPlugIn
	 */
	public BallListener(PingPongPlugIn aPlugIn) {
		this.mPlugIn = aPlugIn;
	}

	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Edge aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(MoveBall aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(PlayerLeft aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(PlayerRight aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Right aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Left aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.moveBall(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(GameOver aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.gameOver(aEvent.getPingpongMatch(), aEvent.getBoolean());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Score aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.scoreUpdate(aEvent.getPingpongMatch());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Counter aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.initCounter(aEvent.getPingpongMatch(), aEvent.getCounter());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(MovePlayer aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.playerUpdate(aEvent.getPingpongMatch(), aEvent.getJoystick());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Sound aEvent, ResponseEvent aResponseEvent) {
		mPlugIn.sound(aEvent.getPingpongMatch(), aEvent.getSound());
	}
}
