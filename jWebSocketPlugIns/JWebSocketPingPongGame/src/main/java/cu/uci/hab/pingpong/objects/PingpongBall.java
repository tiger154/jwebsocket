/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.objects;

import cu.uci.hab.pingpong.events.Counter;
import cu.uci.hab.pingpong.events.GameOver;
import cu.uci.hab.pingpong.events.Left;
import cu.uci.hab.pingpong.events.MoveBall;
import cu.uci.hab.pingpong.events.PlayerLeft;
import cu.uci.hab.pingpong.events.PlayerRight;
import cu.uci.hab.pingpong.events.Right;
import cu.uci.hab.pingpong.events.Edge;
import cu.uci.hab.pingpong.events.Score;
import cu.uci.hab.pingpong.events.Sound;
import org.jwebsocket.gaming.Ball;

/**
 *
 * @author armando
 */
public final class PingpongBall extends Ball {

	private int mBallMoveX, mBallMoveY, mBallX, mBallY;
	private int mBallSpeed;
	private PingpongStage mPingpongStage;

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

	public int getMoveBallX() {
		return this.mBallMoveX;
	}

	public void setMoveBallX(int aMoveBallX) {
		this.mBallMoveX = aMoveBallX;
	}

	public int getMoveBallY() {
		return this.mBallMoveY;
	}

	public void setMoveBallY(int ballMoveY) {
		this.mBallMoveY = ballMoveY;
	}

	public int getSpeedBall() {
		return this.mBallSpeed;
	}

	public int getBallX() {
		return this.mBallX;
	}

	public int getBallY() {
		return this.mBallY;
	}
}
