/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.objects;

import org.jwebsocket.gaming.Position;

/**
 *
 * @author armando
 */
public final class PingpongPosition extends Position {

	public PingpongPosition(int aPosX, int aPosY) {
		setX(aPosX);
		setY(aPosY);
	}
}
