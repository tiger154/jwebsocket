//	---------------------------------------------------------------------------
//	jWebSocket - IPacketDeliveryListener
//	Copyright (c) 2012 Innotrade GmbH
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
package org.jwebsocket.api;

/**
 *
 * @author kyberneees
 */
public interface IPacketDeliveryListener {

	/**
	 * Returns the timeout of the request.
	 *
	 * @return
	 */
	long getTimeout();

	/**
	 * Is fired when the given response timeout is exceeded.
	 *
	 */
	void OnTimeout();

	/**
	 * Is fired if the packet has been delivered successfully.
	 *
	 */
	void OnSuccess();

	/**
	 * Is fired if the packet delivery has failed.
	 *
	 * @param lEx The failure exception
	 */
	void OnFailure(Exception lEx);
}
