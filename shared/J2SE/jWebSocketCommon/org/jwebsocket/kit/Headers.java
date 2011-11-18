/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.kit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javolution.util.FastMap;

/**
 *
 * @author alexanderschulze
 */
public class Headers {

	public static final String HOST = "Host";
	public static final String UPGRADE = "Upgrade";
	public static final String CONNECTION = "Connection";
	
	public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
	public static final String SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";
	public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
	public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
	
	public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
	
	private Map<String, String> mFields = new FastMap<String, String>();
	private String mFirstLine = null;
	private byte[] mTrailingBytes = null;

	/**
	 * 
	 * @param aVersion
	 * @param aIS
	 * @throws WebSocketException
	 */
	public void readFromStream(int aVersion, InputStream aIS) throws WebSocketException {
		// the header is complete when the first empty line is detected
		boolean lHeaderComplete = false;

		// signal if we are still within the header
		boolean lInHeader = true;
		int lLineNo = 0;
		ByteArrayOutputStream lBuffer = new ByteArrayOutputStream(512);
		ByteArrayOutputStream lTrailing = new ByteArrayOutputStream(16);

		byte[] lServerResponse = new byte[16];

		int lA, lB = -1;
		while (!lHeaderComplete) {
			lA = lB;
			try {
				lB = aIS.read();
			} catch (IOException ex) {
				throw new WebSocketException("Error on reading stream: " + ex.getMessage());
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
					lInHeader = false;
					lHeaderComplete = !WebSocketProtocolAbstraction.isHixieVersion(aVersion);
				} else {
					if (0 == lLineNo) {
						mFirstLine = lLine;
					} else {
						String[] lKeyVal = lLine.split(":", 2);
						if (2 == lKeyVal.length) {
							mFields.put(lKeyVal[0].trim(), lKeyVal[1].trim());
						}
					}
					lLineNo++;

				}
				lBuffer.reset();
			}
		}
	}

	/**
	 * @return the mFields
	 */
	public Map<String, String> getFields() {
		return mFields;
	}

	/**
	 * 
	 * @param aField
	 * @return
	 */
	public String getField(String aField) {
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
	 * @return the mTrailingBytes
	 */
	public byte[] getTrailingBytes() {
		return mTrailingBytes;
	}
}
