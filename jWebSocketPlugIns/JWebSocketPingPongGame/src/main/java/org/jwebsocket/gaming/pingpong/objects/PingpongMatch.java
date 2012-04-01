/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.objects;

import org.jwebsocket.gaming.pingpong.listeners.BallListener;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.gaming.api.Match;

/**
 *
 * @author armando
 */
public class PingpongMatch extends Match implements Runnable {

	private List<PingpongPlayer> mPingpongPlayerList;
	private PingpongBall mBall;
	private Thread mThread;
	private boolean mStop, mGameOver;
	private int mSpeed, mActServ, mScore, mCont;
	private PingpongStage mPingpongStage;
	private BallListener mBallListener;
	private List<Class<? extends Event>> mEventsB, mEventsP;

	public PingpongMatch(PingPongPlugIn aUserPlugIn, PingpongStage aPingpongStage,
			PingpongPlayer aPingpongPlayer1, PingpongPlayer aPingpongPlayer2) {
		this.mPingpongPlayerList = new ArrayList<PingpongPlayer>(2);
		this.mBall = new PingpongBall(aPingpongStage);
		this.mBall = new PingpongBall(aPingpongStage);
		this.mThread = new Thread(this);
		this.mGameOver = false;
		this.mStop = true;
		this.mSpeed = 50;
		this.mPingpongStage = aPingpongStage;
		this.mScore = 10;
		this.mActServ = 1;
		this.mBallListener = new BallListener(aUserPlugIn);
		this.mCont = 0;

		mEventsB = new LinkedList<Class<? extends Event>>();
		mEventsP = new LinkedList<Class<? extends Event>>();
		mEventsB.add(Edge.class);
		mEventsB.add(Left.class);
		mEventsB.add(GameOver.class);
		mEventsB.add(Right.class);
		mEventsB.add(PlayerLeft.class);
		mEventsB.add(PlayerRight.class);
		mEventsB.add(MoveBall.class);
		mEventsB.add(Score.class);
		mEventsB.add(Counter.class);
		mEventsB.add(Sound.class);
		mEventsP.add(MovePlayer.class);

		try {
			mBall.on(mEventsB, mBallListener);
			aPingpongPlayer1.on(mEventsP, mBallListener);
			aPingpongPlayer2.on(mEventsP, mBallListener);

		} catch (Exception ex) {
			Logger.getLogger(PingpongMatch.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.mPingpongPlayerList.add(aPingpongPlayer1);
		this.mPingpongPlayerList.add(aPingpongPlayer2);

	}

	// Se devuelve la lista de los jugadores
	public List<PingpongPlayer> getPingpongPlayerList() {
		return mPingpongPlayerList;
	}

	// Se devuelve el largo de la lista
	public int getsize() {
		return this.mPingpongPlayerList.size();
	}

	// Eliminar jugador
	public void delete() {
		this.mPingpongPlayerList.clear();
	}

	//Se devuelve un jugador por su posicion
	public PingpongPlayer getPingpongPlayer(int aPos) {
		return this.mPingpongPlayerList.get(aPos);
	}

	// Se davuelve un jugardor dado su nombre
	public PingpongPlayer getPingpongPlayer(String aUserName) {
		for (PingpongPlayer p : mPingpongPlayerList) {
			if (p.getPlayerName().equals(aUserName)) {
				return p;
			}
		}
		return null;
	}

	// Se devuelve la pelota
	public PingpongBall getBall() {
		return this.mBall;
	}

	//mover la ball
	@Override
	public void run() {
		try {
			while (true) {
				if (mCont >= 0) {
					mBall.notify(new Counter(this, mCont), null, true);
					mCont--;
				} else {
					int lLeftPlayer = (int) (mPingpongPlayerList.get(0).
							getPosition().getX() + mPingpongPlayerList.get(0).
							getDimension().getWidth());
					int lRightPlayer = (int) (mPingpongPlayerList.get(1).
							getPosition().getX() - mBall.getDimension().
							getWidth());
					int lBottomBall = mBall.getPosition().getY()
							+ mBall.getDimension().getHeight();
					mSpeed = 50;
					if (mBall.getPosition().getX()
							<= mPingpongStage.getStageBorderLeft()) {
						mPingpongPlayerList.get(1).setPlayerScore(
								mPingpongPlayerList.get(1).getPlayerScore() + 1);
						mBall.notify(new Left(this), null, true);
						mActServ = -1;
						initBall();
						if (mPingpongPlayerList.get(1).getPlayerScore()
								>= mScore) {
							mBall.notify(new GameOver(this, true), null, true);
							pause();
						}
						mSpeed = 1000;
					} else if (mBall.getPosition().getX()
							+ mBall.getDimension().getWidth() + 12
							>= mPingpongStage.getStageBorderRaight()) {
						mPingpongPlayerList.get(0).setPlayerScore(
								mPingpongPlayerList.get(0).getPlayerScore() + 1);
						mBall.notify(new Right(this), null, true);
						mActServ = 1;
						initBall();
						if (mPingpongPlayerList.get(0).getPlayerScore()
								>= mScore) {
							mBall.notify(new GameOver(this, true), null, true);
							pause();
						}
						mSpeed = 1000;
					}
					if (mBall.getPosition().getY()
							<= mPingpongStage.getStageBorderTop()
							+ 3 || mBall.getPosition().getY()
							>= mPingpongStage.getStageBorderBottom() - 20) {
						mBall.setMoveBallY(mBall.getMoveBallY() * -1);
						mBall.notify(new Edge(this), null, true);
						mBall.notify(new Sound(this, 0), null, true);
					} else if (mBall.getPosition().getX() - 2
							<= lLeftPlayer && lBottomBall
							>= mPingpongPlayerList.get(0).getPosition().getY()
							&& mBall.getPosition().getY()
							<= mPingpongPlayerList.get(0).getPosition().getY()
							+ mPingpongPlayerList.get(0).getDimension().getHeight()) {
						mBall.setMoveBallX(mBall.getSpeedBall());
						hitBall(mPingpongPlayerList.get(0));
						mBall.notify(new PlayerLeft(this), null, true);
						mBall.notify(new Sound(this, 0), null, true);
					} else if (mBall.getPosition().getX()
							>= lRightPlayer - 2 && lBottomBall
							>= mPingpongPlayerList.get(1).getPosition().getY()
							&& mBall.getPosition().getY()
							<= mPingpongPlayerList.get(1).getPosition().getY()
							+ mPingpongPlayerList.get(1).getDimension().getHeight()) {
						mBall.setMoveBallX(-mBall.getSpeedBall());
						hitBall(mPingpongPlayerList.get(1));
						mBall.notify(new PlayerRight(this), null, true);
						mBall.notify(new Sound(this, 0), null, true);

					}
					mBall.getPosition().setX(mBall.getPosition().getX()
							+ mBall.getMoveBallX());
					mBall.getPosition().setY(mBall.getPosition().getY()
							+ mBall.getMoveBallY());
					mBall.notify(new MoveBall(this), null, true);

				}
				Thread.sleep(mSpeed);
			}
		} catch (Exception ex) {
			Logger.getLogger(PingpongMatch.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//Comenzar partida
	public void NewGame() {
		mPingpongPlayerList.get(0).setPlayerScore(0);
		mPingpongPlayerList.get(1).setPlayerScore(0);
		mCont = 3;
		initBall();
		mSpeed = 1000;
		//stop=false; 


		try {
			if (!mThread.isAlive()) {
				mStop = false;
				mThread.start();
			} else {
				mBall.notify(new GameOver(this, false), null, true);
				// Thread.sleep(speed);
				pause();
			}
		} catch (Exception ex) {
			Logger.getLogger(PingpongMatch.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	// inicializo la ball
	public void initBall() {
		mGameOver = false;
		mBall.setMoveBallX(mBall.getSpeedBall() * mActServ);
		mBall.setMoveBallY(mActServ);
		mBall.initBall();


		try {
			mBall.notify(new Score(this), null, true);
			mBall.notify(new MoveBall(this), null, true);
		} catch (Exception ex) {
			Logger.getLogger(PingpongMatch.class.getName()).log(Level.SEVERE, null, ex);
		}
		mBall.getPosition().setX(mBall.getBallX() - mBall.getSpeedBall() * mActServ);
		//mBall.getPosition().setPosY(mBall.getBallY());

	}

	//
	public void hitBall(PingpongPlayer aPingpongPlayer) {
		int lTopBall = mBall.getPosition().getY();
		int lCenterBall = Math.round(lTopBall + mBall.getDimension().getHeight() / 2);
		int lBottomBall = lTopBall + mBall.getDimension().getHeight();
		int lCenterBar = Math.round(aPingpongPlayer.getPosition().getY()
				+ aPingpongPlayer.getDimension().getHeight() / 2);

		if (lBottomBall + mBall.getSpeedBall()
				< mPingpongStage.getStageBorderBottom()
				&& lTopBall - mBall.getSpeedBall()
				> mPingpongStage.getStageBorderTop()) {
			if (lCenterBall < lCenterBar
					- aPingpongPlayer.getDimension().getHeight() / 2) {
				mBall.setMoveBallY(Math.round(-mBall.getSpeedBall() * 1.5f));
			} else if (lCenterBall > lCenterBar
					+ aPingpongPlayer.getDimension().getHeight() / 2) {
				mBall.setMoveBallY(Math.round(mBall.getSpeedBall() * 1.5f));
			} else if (lCenterBall < lCenterBar
					- aPingpongPlayer.getDimension().getHeight() / 4) {
				mBall.setMoveBallY(-mBall.getSpeedBall());
			} else if (lCenterBall > lCenterBar
					+ aPingpongPlayer.getDimension().getHeight() / 4) {
				mBall.setMoveBallY(mBall.getSpeedBall());
			} else if (lCenterBall < lCenterBar
					- aPingpongPlayer.getDimension().getHeight() / 6) {
				mBall.setMoveBallY(Math.round(-mBall.getSpeedBall() / 2));
			} else if (lCenterBall > lCenterBar
					+ aPingpongPlayer.getDimension().getHeight() / 6) {
				mBall.setMoveBallY(Math.round(mBall.getSpeedBall() / 2));
			} else if (lCenterBall < lCenterBar) {
				mBall.setMoveBallY(-1);
			} else {
				mBall.setMoveBallY(1);
			}
		}

	}

	//Pause
	public void pause() {
		if (mStop) {
			mStop = false;
			mThread.resume();
		} else {
			mStop = true;
			mThread.suspend();

		}
	}

	public boolean stop() {
		return mStop;
	}

	public void destroy() {
		mThread.stop();
	}

	// mando a moverce los  player
	public void movePlayer(int aEvent, String aUsername, String aValue) {
		try {
			PingpongPlayer lPingpongPlayer = getPingpongPlayer(aUsername);
			lPingpongPlayer.move(aEvent, aValue);
			lPingpongPlayer.notify(new MovePlayer(this,
					lPingpongPlayer.getPlayerJoystick()), null, true);
		} catch (Exception ex) {
			Logger.getLogger(PingpongMatch.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
