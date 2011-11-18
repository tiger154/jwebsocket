//	---------------------------------------------------------------------------
//	jWebSocket - Proxy Plug-In
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
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


package org.jwebsocket.plugins.proxy;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;

class Endpoint {

	private InetSocketAddress mAddress;
	private boolean mIsSSLEnabled;

	public Endpoint(String aHost, int aPort, boolean aSSL) throws MalformedURLException {
		mIsSSLEnabled = aSSL;
		mAddress = new InetSocketAddress(aHost, aPort);
	}

	/**
	 * @return the mAddress
	 */
	public InetSocketAddress getAddress() {
		return mAddress;
	}

	/**
	 * @return the mEnableSSL
	 */
	public boolean isSSLEnabled() {
		return mIsSSLEnabled;
	}
}
