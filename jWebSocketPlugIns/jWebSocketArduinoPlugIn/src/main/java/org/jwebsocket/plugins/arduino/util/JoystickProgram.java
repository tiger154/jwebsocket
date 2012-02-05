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
public class JoystickProgram {

	public static Integer[] refineValue(Integer aX, Integer aY) {
		//refine x value and y value in interval: 30-42
		if (aX >= 30 && aX <= 43 && aY >= 30 && aY <= 42) {
			if (aX == 43) {
				aX = 42;
			}
			return new Integer[]{aX - 36, aY - 36};
		}
		return null;
	}

	public static Integer[] treatValues(String aData) {
		Integer lX = Integer.valueOf(aData.split(",")[0]);
		String lTempY = aData.split(",")[1];
		Integer lY = Integer.valueOf(lTempY.substring(0, lTempY.length() - 1));

		return refineValue(lX, lY);
	}
}
