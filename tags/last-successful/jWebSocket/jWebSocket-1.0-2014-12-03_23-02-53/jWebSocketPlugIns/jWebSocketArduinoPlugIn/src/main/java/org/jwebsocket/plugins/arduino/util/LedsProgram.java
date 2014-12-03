// ---------------------------------------------------------------------------
// jWebSocket - LedsProgram (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
package org.jwebsocket.plugins.arduino.util;

/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
public class LedsProgram {

	/**
	 * Becomes an integer from 0 to 15 representing the state of each LED
	 *
	 *
	 * @param aState
	 * @return
	 */
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
