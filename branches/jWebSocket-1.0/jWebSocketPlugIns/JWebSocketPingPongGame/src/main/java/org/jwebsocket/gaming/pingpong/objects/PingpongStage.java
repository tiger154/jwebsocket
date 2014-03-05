//	---------------------------------------------------------------------------
//	jWebSocket - PingpongStage (Community Edition, CE)
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

import org.jwebsocket.gaming.api.Stage;

/**
 *
 * @author armando
 */
public class PingpongStage extends Stage {
//Constante

	/**
	 *
	 */
	public int mStageWidth;                       //Ancho del esenario
	/**
	 *
	 */
	public int mStageHeigth;                      //largo del esenario
	/**
	 *
	 */
	public int mStageBorder;                      //Border del esenario
	/**
	 *
	 */
	public int mStageBorderTop;                   //Borde del Tope superior
	/**
	 *
	 */
	public int mStageBorderBottom;                //Borde del Tope inferior
	/**
	 *
	 */
	public int mStageBorderLeft;                  //Borde del Tope izquierdo
	/**
	 *
	 */
	public int mStageBorderRaight;                //Borde del Tope derecho
	/**
	 *
	 */
	public int mStageCenterX;                     //Centro del esenario en X
	/**
	 *
	 */
	public int mStageCenterY;                     //Centro del esenario en Y

	/**
	 *
	 * @param aStageWidth
	 * @param aStageHeigth
	 * @param aStageBorder
	 */
	public PingpongStage(int aStageWidth, int aStageHeigth, int aStageBorder) {
		this.mStageWidth = aStageWidth;
		this.mStageHeigth = aStageHeigth;
		this.mStageBorder = aStageBorder;
		this.mStageBorderTop = aStageBorder + 2;
		this.mStageBorderLeft = aStageBorder + 2;
		this.mStageBorderRaight = aStageBorder + aStageWidth - 2;
		this.mStageBorderBottom = aStageBorder + aStageHeigth - 10;
		this.mStageCenterX = Math.round(aStageBorder + aStageWidth / 2);
		this.mStageCenterY = Math.round(aStageBorder + aStageHeigth / 2);
	}

	/**
	 *
	 * @return
	 */
	public int getStageWidth() {
		return mStageWidth;
	}

	/**
	 *
	 * @return
	 */
	public int getStageHeigth() {
		return mStageHeigth;
	}

	/**
	 *
	 * @return
	 */
	public int getStageBorder() {
		return mStageBorder;
	}

	/**
	 *
	 * @return
	 */
	public int getStageBorderTop() {
		return mStageBorderTop;
	}

	/**
	 *
	 * @return
	 */
	public int getStageBorderBottom() {
		return mStageBorderBottom;
	}

	/**
	 *
	 * @return
	 */
	public int getStageBorderLeft() {
		return mStageBorderLeft;
	}

	/**
	 *
	 * @return
	 */
	public int getStageBorderRaight() {
		return mStageBorderRaight;
	}

	/**
	 *
	 * @return
	 */
	public int getStageCenterX() {
		return mStageCenterX;
	}

	/**
	 *
	 * @return
	 */
	public int getStageCenterY() {
		return mStageCenterY;
	}
}
