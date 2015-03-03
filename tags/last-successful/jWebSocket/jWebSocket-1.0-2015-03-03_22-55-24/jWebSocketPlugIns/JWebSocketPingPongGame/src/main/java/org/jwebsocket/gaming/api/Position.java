//	---------------------------------------------------------------------------
//	jWebSocket - Position (Community Edition, CE)
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
package org.jwebsocket.gaming.api;

/**
 *
 * @author armando
 */
public abstract class Position {

	/**
	 *
	 */
	protected int mX;
	/**
	 *
	 */
	protected int mY;
	/**
	 *
	 */
	protected int mZ;

	/**
	 *
	 * @return
	 */
	public int getX() {
		return this.mX;
	}

	/**
	 *
	 * @param aX
	 */
	public void setX(int aX) {
		this.mX = aX;
	}

	/**
	 *
	 * @return
	 */
	public int getY() {
		return this.mY;
	}

	/**
	 *
	 * @param aY
	 */
	public void setY(int aY) {
		this.mY = aY;
	}

	/**
	 *
	 * @return
	 */
	public int getZ() {
		return this.mY;
	}

	/**
	 *
	 * @param aY
	 */
	public void setZ(int aY) {
		this.mY = aY;
	}
}
