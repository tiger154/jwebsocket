/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.api;

/**
 *
 * @author armando
 */
public abstract class Position {

	protected int mX;
	protected int mY;
	protected int mZ;

	public int getX() {
		return this.mX;
	}

	public void setX(int aX) {
		this.mX = aX;
	}

	public int getY() {
		return this.mY;
	}

	public void setY(int aY) {
		this.mY = aY;
	}

	public int getZ() {
		return this.mY;
	}

	public void setZ(int aY) {
		this.mY = aY;
	}
}
