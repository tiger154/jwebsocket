//	---------------------------------------------------------------------------
//	jWebSocket - SMS Provider implementation for SMSTrade (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author mayra, aschulze
 */
public class ProviderSmstrade extends BaseSMSProvider implements ISMSProvider {

	private static Logger mLog = Logging.getLogger();
	private String mDebug;
	private String mCost;
	private String mMessageId;
	private String mCount;
	private String mDlr;
	private String mRef;
	private String mConcatSms;
	private String mRoute;
	private String mSendDate;
	private String mMessageType;
	private String mKey = "";

	/**
	 *
	 * @param aMessageId
	 * @param aCount
	 * @param aDlr
	 */
	public ProviderSmstrade(String aMessageId, String aCount, String aDlr) {
		mMessageId = aMessageId;
		mCount = aCount;
		mDlr = aDlr;
	}

	/**
	 *
	 */
	public ProviderSmstrade() {
		mMessageId = "1";
		mCount = "1";
		mDlr = "1";
	}

	/**
	 * @return the Key
	 */
	public String getKey() {
		return mKey;
	}

	/**
	 * @param aKey
	 */
	public void setKey(String aKey) {
		mKey = aKey;
	}

	private String getErrorMessage(String aCode) {
		String lRes = "undefined, please refer to manual.";
		if ("10".equals(aCode)) {
			lRes = "Receiver number not valid, parameter 'to', use a valid format, e.g. 491701231231.";
		} else if ("20".equals(aCode)) {
			lRes = "Sender number not valid, parameter 'from', use max 11 characters of text or max 16 integer digits.";
		} else if ("30".equals(aCode)) {
			lRes = "Message text not valid, parameter 'message', use max 160 characters of text or concatenate SMS.'";
		} else if ("31".equals(aCode)) {
			lRes = "Message type not valid, parameter 'messagetype', remove message type or use one of the following types: flash, unicode, binary, voice.";
		} else if ("40".equals(aCode)) {
			lRes = "SMS route not valid, parameter 'route', the following routes are valid: basic, gold, direct.";
		} else if ("50".equals(aCode)) {
			lRes = "Identification failed, parameter 'key', check the gateway key.";
		} else if ("60".equals(aCode)) {
			lRes = "Not enough balance in account, recharge your balance.";
		} else if ("70".equals(aCode)) {
			lRes = "Network does not support the route, parameter 'route', choose a different route.";
		} else if ("71".equals(aCode)) {
			lRes = "Feature is not possible by the route, parameter 'route', choose a different route.";
		} else if ("80".equals(aCode)) {
			lRes = "Handover to SMSC failed, choose a different route or contact support for further information.";
		} else if ("100".equals(aCode)) {
			lRes = "SMS has been sent successfully";
		}
		return lRes;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	@Override
	public Token sendSms(Token aToken) {
		Token lRes = TokenFactory.createToken();

		lRes.setInteger("code", -1);
		lRes.setString("msg", "undefined");

		// building data
		String lFrom = aToken.getString("from");
		String lTo = aToken.getString("to");
		String lMessage = aToken.getString("message");
		String lFromEnc = lFrom;
		String lToEnc = lTo;
		String lMessageEnc = lMessage;
		String lState = aToken.getString("state");

		// validate target phone number
		lTo = trimPhoneNumber(lTo);

		URL lURL;
		URLConnection lConn = null;

		BufferedReader lReader = null;

		try {
			lToEnc = URLEncoder.encode(lTo, "UTF-8");
			lFromEnc = URLEncoder.encode(lFrom, "UTF-8");
			lMessageEnc = URLEncoder.encode(lMessage, "UTF-8");
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " at parsing arguments: " + lEx.getMessage());
		}

		String lURLString = "http://gateway.smstrade.de/?key=" + getKey()
				+ "&from=" + lFromEnc
				+ "&to=" + lToEnc
				+ "&message=" + lMessageEnc
				+ "&route=" + lState
				+ "&message_id=" + this.mMessageId;


		if (mLog.isDebugEnabled()) {
			mLog.debug("Establishing connection to SMS provider 'SMSTrade' (type: " + lState + ", from: " + lFrom + ", to: " + lTo + ", message: '" + lMessage + "')...");
		}
		try {
			lURL = new URL(lURLString);
			lConn = lURL.openConnection();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " getting http connection: " + lEx.getMessage());
		}

		if (null == lConn) {
			lRes.setString("msg", "Error in http connection to provider SMSTrade.");
			return lRes;
		}

		// get Response
		try {
			lReader = new BufferedReader(
					new InputStreamReader(
					lConn.getInputStream()));
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " opening result: " + lEx.getMessage());
		}

		String lLine;
		int lIdx = 0;
		try {
			while ((lLine = lReader.readLine()) != null) {
				if (lIdx == 0) {
					lRes.setString("provider_code", lLine);
					if ("100".equals(lLine)) {
						lRes.setInteger("code", 0);
					}
					lRes.setString("msg", getErrorMessage(lLine));
				} else if (lIdx == 1) {
					lRes.setString("messageId", lLine);
				}
				lIdx++;
			}
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " reading result: " + lEx.getMessage());
		}
		try {
			lReader.close();
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " closing reader: " + lEx.getMessage());
		}

		return lRes;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	@Override
	public Token longerSms(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	@Override
	public Token gsmSms(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	@Override
	public Token bulkSms(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return the debug
	 */
	public String getDebug() {
		return mDebug;
	}

	/**
	 * @param aDebug the debug to set
	 */
	public void setDebug(String aDebug) {
		this.mDebug = aDebug;
	}

	/**
	 * @return the cost
	 */
	public String getCost() {
		return mCost;
	}

	/**
	 * @return the messagge_id
	 */
	public String getMessaggeId() {
		return mMessageId;
	}

	/**
	 * @return the count
	 */
	public String getCount() {
		return mCount;
	}

	/**
	 * @return the dlr
	 */
	public String getDlr() {
		return mDlr;
	}

	/**
	 *
	 * @return
	 */
	public String getRef() {
		return mRef;
	}

	/**
	 *
	 * @return
	 */
	public String getConcatSms() {
		return mConcatSms;
	}

	/**
	 *
	 * @return
	 */
	public String getRoute() {
		return mRoute;
	}

	/**
	 *
	 * @return
	 */
	public String getSendDate() {
		return mSendDate;
	}

	/**
	 *
	 * @return
	 */
	public String getMessageType() {
		return mMessageType;
	}

	/**
	 *
	 * @param aCost
	 */
	public void setCost(String aCost) {
		this.mCost = aCost;
	}

	/**
	 *
	 * @param aMessaggeId
	 */
	public void setMessaggeId(String aMessaggeId) {
		this.mMessageId = aMessaggeId;
	}

	/**
	 * @param aCount the count to set
	 */
	public void setCount(String aCount) {
		this.mCount = aCount;
	}

	/**
	 * @param aDlr the dlr to set
	 */
	public void setDlr(String aDlr) {
		this.mDlr = aDlr;
	}

	/**
	 *
	 * @param aRef
	 */
	public void setRef(String aRef) {
		this.mRef = aRef;
	}

	/**
	 *
	 * @param aConcatSms
	 */
	public void setConcatSms(String aConcatSms) {
		this.mConcatSms = aConcatSms;
	}

	/**
	 *
	 * @param aRoute
	 */
	public void setRoute(String aRoute) {
		this.mRoute = aRoute;
	}

	/**
	 *
	 * @param aSendDate
	 */
	public void setSendDate(String aSendDate) {
		this.mSendDate = aSendDate;
	}

	/**
	 *
	 * @param aMessageType
	 */
	public void setMessageType(String aMessageType) {
		this.mMessageType = aMessageType;
	}
}
