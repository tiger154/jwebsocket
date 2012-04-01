/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.api;

/**
 *
 * @author armando
 */
public abstract class Dimension {

	private int mWidth;
	private int mHeight;
	private int mDeep;

	public int getDeep() {
		return mDeep;
	}

	public void setDeep(int aDeep) {
		this.mDeep = aDeep;
	}

	public float getWidth() {
		return this.mWidth;
	}

	public void setWidth(int aWidth) {
		this.mWidth = aWidth;
	}

	public int getHeight() {
		return this.mHeight;
	}

	public void setHeight(int aHeigth) {
		this.mHeight = aHeigth;
	}
}
