/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.api;

import org.jwebsocket.eventmodel.observable.ObservableObject;

/**
 *
 * @author armando
 */
public abstract class MatchObject extends ObservableObject {

	protected Position mPosition;
	protected Dimension mDimension;

	public Position getPosition() {
		return this.mPosition;
	}

	public void setPosition(Position aPosition) {
		this.mPosition = aPosition;
	}

	public Dimension getDimension() {
		return this.mDimension;
	}

	public void setDimension(Dimension aDimension) {
		this.mDimension = aDimension;
	}
}