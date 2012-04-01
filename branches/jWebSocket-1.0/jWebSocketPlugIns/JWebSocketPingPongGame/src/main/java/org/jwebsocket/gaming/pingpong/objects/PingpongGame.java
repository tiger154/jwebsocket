/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

	public PingpongGame(int aWidht, int aHeight, int aGameBorder) {

		this.mMatchs = new ArrayList<PingpongMatch>();
		mPingpongStage = new PingpongStage(aWidht, aHeight, aGameBorder);

	}

	// Se adiciona a los partidos	
	public void addMatch(PingpongMatch aPingpongMatch) {
		this.mMatchs.add(aPingpongMatch);
	}

	// Se devuelve la lista de partidos
	public List<PingpongMatch> getMatchs() {
		return this.mMatchs;
	}

	//Se devuelve el largo de la lista
	public int getMatchsCount() {
		return this.mMatchs.size();
	}

	// se devuelve el obj esenario
	public PingpongStage getPingpongStage() {
		return this.mPingpongStage;
	}
	//se devuelve el partido dado un jugador

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
	public void deleteMatch(PingpongMatch aMatch) {
		this.mMatchs.remove(aMatch);
	}
}
