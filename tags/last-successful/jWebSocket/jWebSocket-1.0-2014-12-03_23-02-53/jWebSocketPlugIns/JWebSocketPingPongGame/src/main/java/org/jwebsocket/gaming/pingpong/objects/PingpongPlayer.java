//	---------------------------------------------------------------------------
//	jWebSocket - PingpongPlayer (Community Edition, CE)
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

	/**
	 *
	 * @param aPlayerName
	 * @param aPlayerJoystick
	 * @param aPlayerScore
	 * @param aPingpongStage
	 */
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

	/**
	 *
	 * @return
	 */
	public String getPlayerName() {
		return this.mPlayerName;
	}

	/**
	 *
	 * @return
	 */
	public String getPlayerJoystick() {
		return this.mPlayerJoystick;
	}

	/**
	 *
	 * @param aEvent
	 * @param aValue
	 */
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
