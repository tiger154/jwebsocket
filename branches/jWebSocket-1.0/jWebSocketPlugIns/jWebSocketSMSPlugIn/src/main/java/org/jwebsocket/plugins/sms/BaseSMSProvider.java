//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMS Plug-In, base features for all providers
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.sms;

/**
 *
 * @author Alexander Schulze
 */
public class BaseSMSProvider {

	public String trimPhoneNumber(String aNumber) {
		StringBuilder lRes = new StringBuilder(aNumber.length());
		for (int i = 0; i < aNumber.length(); i++) {
			char lC = aNumber.charAt(i);
			if (lC >= '0' && lC <= '9'
					| lC == '+') {
				lRes.append(lC);
			}
		}
		return lRes.toString();
	}
}
