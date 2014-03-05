//	---------------------------------------------------------------------------
//	jWebSocket - PingpongBall (Community Edition, CE)
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
package org.jwebsocket.gaming.pingpong.objects;

import org.jwebsocket.gaming.pingpong.events.Counter;
import org.jwebsocket.gaming.pingpong.events.GameOver;
import org.jwebsocket.gaming.pingpong.events.Left;
import org.jwebsocket.gaming.pingpong.events.MoveBall;
import org.jwebsocket.gaming.pingpong.events.PlayerLeft;
import org.jwebsocket.gaming.pingpong.events.PlayerRight;
import org.jwebsocket.gaming.pingpong.events.Right;
import org.jwebsocket.gaming.pingpong.events.Edge;
import org.jwebsocket.gaming.pingpong.events.Score;
import org.jwebsocket.gaming.pingpong.events.Sound;
import org.jwebsocket.gaming.api.Ball;

/**
 *
 * @author armando
 */
public final class PingpongBall extends Ball {

	private int mBallMoveX, mBallMoveY, mBallX, mBallY;
	private int mBallSpeed;
	private final PingpongStage mPingpongStage;

	/**
	 *
	 * @param aPingpongStage
	 */
	public PingpongBall(PingpongStage aPingpongStage) {
		this.mBallMoveX = 0;
		this.mBallMoveY = 0;
		this.mBallSpeed = Math.round(aPingpongStage.getStageWidth() / 300);
		this.mPingpongStage = aPingpongStage;

		this.addEvents(Edge.class);
		this.addEvents(Left.class);
		this.addEvents(GameOver.class);
		this.addEvents(Right.class);
		this.addEvents(PlayerLeft.class);
		this.addEvents(PlayerRight.class);
		this.addEvents(MoveBall.class);
		this.addEvents(Score.class);
		this.addEvents(Counter.class);
		this.addEvents(Sound.class);

		initBall();
	}

	/**
	 *
	 */
	public void initBall() {
		mBallSpeed = Math.round(mPingpongStage.getStageWidth() / 40);

		// inicializando las dimenciones de Ball	
		int ballsize = Math.round(mPingpongStage.getStageWidth() / 30);
		// inicializando las Posiciones X - Y
		mBallX = Math.round(mPingpongStage.getStageCenterX() - 2 - ballsize / 2);
		mBallY = Math.round(mPingpongStage.getStageCenterY() - 2 - ballsize / 2);

		setDimension(new PingpongDimension(ballsize, ballsize));
		setPosition(new PingpongPosition(mBallX, mBallY));

	}

	/**
	 *
	 * @return
	 */
	public int getMoveBallX() {
		return this.mBallMoveX;
	}

	/**
	 *
	 * @param aMoveBallX
	 */
	public void setMoveBallX(int aMoveBallX) {
		this.mBallMoveX = aMoveBallX;
	}

	/**
	 *
	 * @return
	 */
	public int getMoveBallY() {
		return this.mBallMoveY;
	}

	/**
	 *
	 * @param ballMoveY
	 */
	public void setMoveBallY(int ballMoveY) {
		this.mBallMoveY = ballMoveY;
	}

	/**
	 *
	 * @return
	 */
	public int getSpeedBall() {
		return this.mBallSpeed;
	}

	/**
	 *
	 * @return
	 */
	public int getBallX() {
		return this.mBallX;
	}

	/**
	 *
	 * @return
	 */
	public int getBallY() {
		return this.mBallY;
	}
}
