/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.objects;

import org.jwebsocket.gaming.pingpong.events.MovePlayer;
import org.jwebsocket.gaming.api.Player;

/**
 *
 * @author armando
 */
public class PingpongPlayer extends Player {
// Propiedades

	private String mPlayerName;                           //Nombre del jugador
	private String mPlayerJoystick;			 //posicion de los mando(PlayerLeft-PlayerRight)
	PingpongStage mPingpongStege;
	private int mMoveBarKey, mMoveBarMuse;

	public PingpongPlayer(String aPlayerName, String aPlayerJoystick,
			int aPlayerScore, PingpongStage aPingpongStage) {
		this.mPlayerName = aPlayerName;
		this.mPlayerJoystick = aPlayerJoystick;
		this.mPingpongStege = aPingpongStage;
		setPlayerScore(aPlayerScore);
		this.mMoveBarKey = Math.round(aPingpongStage.getStageHeigth() / 12);
		this.mMoveBarMuse = Math.round(aPingpongStage.getStageHeigth() / 24);
		this.addEvents(MovePlayer.class);

		initPlayer(aPingpongStage);
	}

	private void initPlayer(PingpongStage aPingpongStage) {
		// inicializando las dimenciones de mis raquetas
		int lWidth = Math.round(aPingpongStage.getStageWidth() / 25);
		int lHeigth = Math.round(aPingpongStage.getStageHeigth() / 5);

		// inicializando las Posiciones X - Y
		int lPosX = 0;
		int lPosY = 0;
		if (mPlayerJoystick.equals("playLeft")) {
			lPosX = aPingpongStage.getStageBorder();
			lPosY = Math.round(aPingpongStage.mStageCenterY - lHeigth / 2);
		} else if (mPlayerJoystick.equals("playRight")) {
			lPosX = aPingpongStage.getStageBorder()
					+ aPingpongStage.getStageWidth() - lWidth - 8;
			lPosY = Math.round(aPingpongStage.mStageCenterY - lHeigth / 2);

		}
		setDimension(new PingpongDimension(lWidth, lHeigth));
		setPosition(new PingpongPosition(lPosX, lPosY));
	}

	public String getPlayerName() {
		return this.mPlayerName;
	}

	public String getPlayerJoystick() {
		return this.mPlayerJoystick;
	}

	public void move(int aEvent, String aValue) {
		int lPixel = 0;
		if (aValue.equals("k")) {
			if (aEvent == 38) {
				lPixel = -mMoveBarKey;
			} else if (aEvent == 40) {
				lPixel = mMoveBarKey;
			}
		} else {
			if (aEvent == 38) {
				lPixel = -mMoveBarMuse;
			} else if (aEvent == 40) {
				lPixel = mMoveBarMuse;
			}
		}
		if (getPosition().getY() + lPixel > mPingpongStege.getStageBorderBottom()
				- getDimension().getHeight()) {
			getPosition().setY(mPingpongStege.getStageBorderBottom()
					- getDimension().getHeight());
		} else if (getPosition().getY() + lPixel
				< mPingpongStege.getStageBorderTop()) {
			getPosition().setY(mPingpongStege.getStageBorderTop());
		} else {
			getPosition().setY(getPosition().getY() + lPixel);
		}

	}
}
