//	---------------------------------------------------------------------------
//	jWebSocket - PingpongGame (Community Edition, CE)
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

import java.util.ArrayList;
import java.util.List;
import org.jwebsocket.gaming.api.Game;

/**
 *
 * @author armando
 */
public class PingpongGame extends Game {

	private List<PingpongMatch> mMatchs;
	private PingpongStage mPingpongStage;

	/**
	 *
	 * @param aWidht
	 * @param aHeight
	 * @param aGameBorder
	 */
	public PingpongGame(int aWidht, int aHeight, int aGameBorder) {

		this.mMatchs = new ArrayList<PingpongMatch>();
		mPingpongStage = new PingpongStage(aWidht, aHeight, aGameBorder);

	}

	// Se adiciona a los partidos	
	/**
	 *
	 * @param aPingpongMatch
	 */
	public void addMatch(PingpongMatch aPingpongMatch) {
		this.mMatchs.add(aPingpongMatch);
	}

	// Se devuelve la lista de partidos
	/**
	 *
	 * @return
	 */
	public List<PingpongMatch> getMatchs() {
		return this.mMatchs;
	}

	//Se devuelve el largo de la lista
	/**
	 *
	 * @return
	 */
	public int getMatchsCount() {
		return this.mMatchs.size();
	}

	// se devuelve el obj esenario
	/**
	 *
	 * @return
	 */
	public PingpongStage getPingpongStage() {
		return this.mPingpongStage;
	}
	//se devuelve el partido dado un jugador

	/**
	 *
	 * @param aUserName
	 * @return
	 */
	public PingpongMatch getMatch(String aUserName) {
		for (PingpongMatch m : mMatchs) {
			for (PingpongPlayer p : m.getPingpongPlayerList()) {
				if (p.getPlayerName().equals(aUserName)) {
					return m;
				}
			}
		}
		return null;
	}

	//elimirar una partida
	/**
	 *
	 * @param aMatch
	 */
	public void deleteMatch(PingpongMatch aMatch) {
		this.mMatchs.remove(aMatch);
	}
}
