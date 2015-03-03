//	---------------------------------------------------------------------------
//	jWebSocket - Headers (Community Edition, CE)
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
package org.jwebsocket.kit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 *
 * @author alexanderschulze
 */
public class Headers {

	/**
	 *
	 */
	public static final String HOST = "Host";
	/**
	 *
	 */
	public static final String UPGRADE = "Upgrade";
	/**
	 *
	 */
	public static final String CONNECTION = "Connection";
	/**
	 *
	 */
	public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
	/**
	 *
	 */
	public static final String ORIGIN = "Origin";
	/**
	 *
	 */
	public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
	/**
	 *
	 */
	public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
	/**
	 *
	 */
	public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
	/**
	 *
	 */
	public static final String SET_COOKIE = "Set-Cookie";
	private Map<String, Object> mFields = new FastMap<String, Object>();
	private String mFirstLine = null;
	private byte[] mTrailingBytes = null;

	/**
	 *
	 * @param aVersion
	 * @param aIS
	 * @throws WebSocketException
	 */
	public void readFromStream(int aVersion, InputStream aIS) throws WebSocketException {
		mFields.put(SET_COOKIE, new FastList<String>());

		// the header is complete when the first empty line is detected
		boolean lHeaderComplete = false;

		// signal if we are still within the header
		boolean lInHeader = true;
		int lLineNo = 0;
		ByteArrayOutputStream lBuffer = new ByteArrayOutputStream(512);
		ByteArrayOutputStream lTrailing = new ByteArrayOutputStream(16);

		int lA, lB = -1;
		while (!lHeaderComplete) {
			lA = lB;
			try {
				lB = aIS.read();
			} catch (IOException ex) {
				throw new WebSocketException("Error on reading stream: " + ex.getMessage());
			}
			if (lB < 0) {
				return;
			}
			lBuffer.write(lB);
			if (!lInHeader) {
				lTrailing.write(lB);
				if (lTrailing.size() == 16) {
					lHeaderComplete = true;
				}
			} else if (0x0D == lA && 0x0A == lB) {
				String lLine;
				try {
					lLine = lBuffer.toString("UTF-8");
				} catch (UnsupportedEncodingException ex) {
					throw new WebSocketException("Error on on converting string: " + ex.getMessage());
				}
				// if the line is empty, the header is complete
				if (lLine.trim().equals("")) {
					// the header is finished
					lInHeader = false;
					// if not hixie the header is complete now
					lHeaderComplete = !WebSocketProtocolAbstraction.isHixieVersion(aVersion);
				} else {
					if (0 == lLineNo) {
						mFirstLine = lLine;
					} else {
						String[] lKeyVal = lLine.split(":", 2);
						if (2 == lKeyVal.length) {
							if (SET_COOKIE.equals(lKeyVal[0].trim())) {
								((List) mFields.get(SET_COOKIE)).add(lKeyVal[1].trim());
							} else {
								mFields.put(lKeyVal[0].trim(), lKeyVal[1].trim());
							}
						}
					}
					lLineNo++;
				}
				// if end of line reset the line buffer
				lBuffer.reset();
			}
		}
	}

	/**
	 * @return the mFields
	 */
	public Map<String, Object> getFields() {
		return mFields;
	}

	/**
	 *
	 * @param aField
	 * @return
	 */
	public Object getField(String aField) {
		if (null != mFields) {
			return mFields.get(aField);
		}
		return null;
	}

	/**
	 * @return the mFirstLine
	 */
	public String getFirstLine() {
		return mFirstLine;
	}

	/**
	 * Returns the trailing bytes of the hixie header. Kept for backward
	 * compatibility with older browsers, flash-bridge and other clients.
	 *
	 * @return the trailing bytes (16) of the header.
	 */
	public byte[] getTrailingBytes() {
		return mTrailingBytes;
	}

	/**
	 *
	 * @return
	 */
	public boolean isValid() {
		// TODO: improve header validation based on protocol version and header content!
		return (mFirstLine != null && mFields.size() > 0);
	}
}
