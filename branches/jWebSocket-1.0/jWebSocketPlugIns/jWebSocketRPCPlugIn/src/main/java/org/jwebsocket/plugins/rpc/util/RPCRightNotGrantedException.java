//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.rpc.util;

/**
 * Exception when a client try to call a method without the right
 * @author Quentin Ambard
 */
public class RPCRightNotGrantedException extends Exception {

	private String mMethod;
	private String mParameters;

	public RPCRightNotGrantedException() {
	}

	public RPCRightNotGrantedException(String aMethod, String aParameters) {
		mMethod = aMethod;
		mParameters = aParameters;
	}

	@Override
	public String getMessage() {
		return "the user does not have the right to call the rpc method " 
				+ mMethod
				+ "(" + mParameters + ").";
	}
}
