/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.objects;

import org.jwebsocket.gaming.Dimension;
import org.jwebsocket.gaming.MatchObject;
import org.jwebsocket.gaming.Position;

/**
 *
 * @author armando
 */
public final class PingpongMatchObject extends MatchObject {

	public PingpongMatchObject(Position aPosition, Dimension aDimension) {
		setDimension(aDimension);
		setPosition(aPosition);
	}
}
