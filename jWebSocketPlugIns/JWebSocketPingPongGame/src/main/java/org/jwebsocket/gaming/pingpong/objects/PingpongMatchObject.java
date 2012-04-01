/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.objects;

import org.jwebsocket.gaming.api.Dimension;
import org.jwebsocket.gaming.api.MatchObject;
import org.jwebsocket.gaming.api.Position;

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
