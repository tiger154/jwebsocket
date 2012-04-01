/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.objects;

import org.jwebsocket.gaming.api.Stage;

/**
 *
 * @author armando
 */
public class PingpongStage extends Stage {
//Constante

	public int mStageWidth;                       //Ancho del esenario
	public int mStageHeigth;                      //largo del esenario
	public int mStageBorder;                      //Border del esenario
	public int mStageBorderTop;                   //Borde del Tope superior
	public int mStageBorderBottom;                //Borde del Tope inferior
	public int mStageBorderLeft;                  //Borde del Tope izquierdo
	public int mStageBorderRaight;                //Borde del Tope derecho
	public int mStageCenterX;                     //Centro del esenario en X
	public int mStageCenterY;                     //Centro del esenario en Y

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

	public int getStageWidth() {
		return mStageWidth;
	}

	public int getStageHeigth() {
		return mStageHeigth;
	}

	public int getStageBorder() {
		return mStageBorder;
	}

	public int getStageBorderTop() {
		return mStageBorderTop;
	}

	public int getStageBorderBottom() {
		return mStageBorderBottom;
	}

	public int getStageBorderLeft() {
		return mStageBorderLeft;
	}

	public int getStageBorderRaight() {
		return mStageBorderRaight;
	}

	public int getStageCenterX() {
		return mStageCenterX;
	}

	public int getStageCenterY() {
		return mStageCenterY;
	}
}
