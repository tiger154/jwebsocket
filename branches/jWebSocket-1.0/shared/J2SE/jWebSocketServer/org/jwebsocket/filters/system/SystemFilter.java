//	---------------------------------------------------------------------------
//	jWebSocket - System Filter (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.filters.system;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class SystemFilter extends TokenFilter {

	private static Logger mLog = Logging.getLogger();
	private static final List<String> mSupportedEncodings;

	static {
		mSupportedEncodings = new FastList<String>();
		mSupportedEncodings.add("base64");
		mSupportedEncodings.add("zipBase64");
	}

	public static List<String> getSupportedEncodings() {
		return mSupportedEncodings;
	}

	/**
	 *
	 * @param aConfig
	 */
	public SystemFilter(FilterConfiguration aConfig) {
		super(aConfig);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating system filter...");
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	@Override
	public void processTokenIn(FilterResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			String lOut = aToken.getLogString();
			mLog.debug("Filtering incoming token from "
					+ (aConnector != null ? aConnector.getId() : "[not given]")
					+ " (" + lOut.length() + "b): " + Logging.getTokenStr(lOut) + "...");
		}

		// processing decoding
		Map<String, String> lEnc = aToken.getMap("enc");
		if (null != lEnc && !lEnc.isEmpty()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing decoding...");
			}
			for (Iterator<String> lIt = lEnc.keySet().iterator(); lIt.hasNext();) {
				String lAttr = lIt.next();
				String lFormat = lEnc.get(lAttr);
				String lValue = aToken.getString(lAttr);

				List lUserEncodingFormats = (List) aConnector.getVar(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY);
				try {
					if (!lUserEncodingFormats.contains(lFormat)) {
						mLog.error("Invalid encoding format '" + lFormat + "' received. Message rejected!");
						aResponse.rejectMessage();
					} else if ("base64".equals(lFormat)) {
						aToken.setString(lAttr, new String(Tools.base64Decode(lValue), "UTF-8"));
					} else if ("zipBase64".equals(lFormat)) {
						aToken.setString(lAttr, new String(Tools.unzip(lValue.getBytes(), Tools.ENC_BASE64), "UTF-8"));
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "trying to decode '" + lAttr + "' value in '"
							+ lFormat + "' format..."));
					aResponse.rejectMessage();
				}
			}
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	@Override
	public void processTokenOut(FilterResponse aResponse,
			WebSocketConnector aSource, WebSocketConnector aTarget,
			Token aToken) {
		if (mLog.isDebugEnabled()) {
			String lOut = aToken.getLogString();
			mLog.debug("Filtering outgoing token from "
					+ (aSource != null ? aSource.getId() : "[not given]")
					+ " to " + (aTarget != null ? aTarget.getId() : "[not given]")
					+ " (" + lOut.length() + "b): " + Logging.getTokenStr(lOut) + "...");
		}

		// processing encoding
		Map<String, String> lEnc = aToken.getMap("enc");
		if (null != lEnc && !lEnc.isEmpty()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing encoding...");
			}
			for (Iterator<String> lIt = lEnc.keySet().iterator(); lIt.hasNext();) {
				String lAttr = lIt.next();
				String lFormat = lEnc.get(lAttr);
				String lValue = aToken.getString(lAttr);

				List lUserEncodingFormats = (List) aTarget.getVar(
						JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY);
				try {
					if (!lUserEncodingFormats.contains(lFormat)) {
						mLog.error("Invalid encoding format '"
								+ lFormat
								+ "' received (not supported). Message rejected!");
						aResponse.rejectMessage();
					} else if ("base64".equals(lFormat)) {
						aToken.setString(lAttr, Tools.base64Encode(lValue.getBytes()));
					} else if ("zipBase64".equals(lFormat)) {
						aToken.setString(lAttr, new String(Tools.zip(
								lValue.getBytes(), Tools.ENC_BASE64), "UTF-8"));
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx,
							"trying to encode '" + lAttr + "' value to '"
							+ lFormat + "' format..."));
					aResponse.rejectMessage();
				}
			}
		}
	}
}
