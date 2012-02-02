/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.pingpong.objects;

import org.jwebsocket.gaming.Dimension;

/**
 *
 * @author armando
 */
public class PingpongDimension extends Dimension {

	public PingpongDimension(int aDimenWidth, int aDimenHeigth) {
		setHeight(aDimenHeigth);
		setWidth(aDimenWidth);
	}
}
