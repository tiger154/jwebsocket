//	---------------------------------------------------------------------------
//	jWebSocket - VerifyPIN (Community Edition, CE)
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
package org.jwebsocket.plugins.jc.commands;

import org.jwebsocket.plugins.jc.api.APDU;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class VerifyPIN implements APDU {

	private byte[] mPIN;

	/**
	 *
	 * @param aPIN
	 */
	public VerifyPIN(byte[] aPIN) {
		this.mPIN = aPIN;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public byte[] getBytes() {

		byte[] lAPDU = new byte[5 + mPIN.length];
		lAPDU[0] = (byte) 0x90;
		lAPDU[1] = (byte) 0x01;
		lAPDU[2] = (byte) 0x00;
		lAPDU[3] = (byte) 0x00;
		lAPDU[4] = (byte) mPIN.length;

		System.arraycopy(mPIN, 0, lAPDU, 5, mPIN.length);

		return lAPDU;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public byte[] getData() {
		return mPIN;
	}
}
