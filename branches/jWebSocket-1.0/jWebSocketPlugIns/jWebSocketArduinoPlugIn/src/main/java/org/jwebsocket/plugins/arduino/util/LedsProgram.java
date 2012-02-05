// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.plugins.arduino.util;

/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
public class LedsProgram {

	//Becomes an integer from 0 to 15 representing the state of each LED
	public static Boolean[] parseLedState(Integer aState) {

		Boolean[] lStatus = new Boolean[4];
		switch (aState) {
			case 15:
				lStatus[0] = true;
				lStatus[1] = true;
				lStatus[2] = true;
				lStatus[3] = true;
				break;
			case 14:
				lStatus[0] = true;
				lStatus[1] = true;
				lStatus[2] = true;
				lStatus[3] = false;
				break;
			case 13:
				lStatus[0] = true;
				lStatus[1] = true;
				lStatus[2] = false;
				lStatus[3] = true;
				break;
			case 12:
				lStatus[0] = true;
				lStatus[1] = true;
				lStatus[2] = false;
				lStatus[3] = false;
				break;
			case 11:
				lStatus[0] = true;
				lStatus[1] = false;
				lStatus[2] = true;
				lStatus[3] = true;
				break;
			case 10:
				lStatus[0] = true;
				lStatus[1] = false;
				lStatus[2] = true;
				lStatus[3] = false;
				break;
			case 9:
				lStatus[0] = true;
				lStatus[1] = false;
				lStatus[2] = false;
				lStatus[3] = true;
				break;
			case 8:
				lStatus[0] = true;
				lStatus[1] = false;
				lStatus[2] = false;
				lStatus[3] = false;
				break;
			case 7:
				lStatus[0] = false;
				lStatus[1] = true;
				lStatus[2] = true;
				lStatus[3] = true;
				break;
			case 6:
				lStatus[0] = false;
				lStatus[1] = true;
				lStatus[2] = true;
				lStatus[3] = false;
				break;
			case 5:
				lStatus[0] = false;
				lStatus[1] = true;
				lStatus[2] = false;
				lStatus[3] = true;
				break;
			case 4:
				lStatus[0] = false;
				lStatus[1] = true;
				lStatus[2] = false;
				lStatus[3] = false;
				break;
			case 3:
				lStatus[0] = false;
				lStatus[1] = false;
				lStatus[2] = true;
				lStatus[3] = true;
				break;
			case 2:
				lStatus[0] = false;
				lStatus[1] = false;
				lStatus[2] = true;
				lStatus[3] = false;
				break;
			case 1:
				lStatus[0] = false;
				lStatus[1] = false;
				lStatus[2] = false;
				lStatus[3] = true;
				break;
			case 0:
				lStatus[0] = false;
				lStatus[1] = false;
				lStatus[2] = false;
				lStatus[3] = false;
				break;
			default:
				lStatus = null;
				break;

		}
		return lStatus;
	}
}
