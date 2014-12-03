//	---------------------------------------------------------------------------
//	jWebSocket - SMS Provider implementation for SMSTrade (Community Edition, CE)
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
 * Provides the base implementation to send SMS text message through the
 * &quot;smstrade.de&quot; provider.
 *
 * @author mayra, Alexander Schulze
 */
public class ProviderSmstrade extends BaseSMSProvider implements ISMSProvider {

	private static final Logger mLog = Logging.getLogger();
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
	 * Constructor of the &quot;smstrade&quot; provider with given parameters.
	 *
	 * @param aMessageId activation of the return of the message ID.
	 * @param aCount activation of the return of number of SMS.
	 * @param aDlr activation of the receiving of a delivery report for this
	 * SMS.
	 */
	public ProviderSmstrade(String aMessageId, String aCount, String aDlr) {
		mMessageId = aMessageId;
		mCount = aCount;
		mDlr = aDlr;
	}

	/**
	 * Constructor of the &quot;smstrade&quot; provider with default parameters.
	 */
	public ProviderSmstrade() {
		mMessageId = "1";
		mCount = "1";
		mDlr = "1";
	}

	/**
	 * Returns the personal identification code.
	 *
	 * @return the personal identification code.
	 */
	public String getKey() {
		return mKey;
	}

	/**
	 * Sets the value of the personal identification code.
	 *
	 * @param aKey the personal identification code
	 */
	public void setKey(String aKey) {
		mKey = aKey;
	}

	/**
	 * Defines a specific message for each response code retrieved for the
	 * smstrade provider.
	 *
	 * @param aCode the response code.
	 * @return the response code traduced to a friendly message.
	 */
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
	 * Allows to send a SMS through the &quot;smstrade&quot; provider.
	 *
	 * @param aToken the request token object that should contain the followings
	 * attributes:
	 * <p>
	 * <ul>
	 * <li>
	 * message: SMS message text
	 * </li>
	 * <li>
	 * to: Receiver of SMS
	 * </li>
	 * <li>
	 * from: Source identifier
	 * </li>
	 * </ul>
	 * </p>
	 * @return a map with the response code from the &quot;smstrade&quot;
	 * provider
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
	 * {@inheritDoc}
	 */
	@Override
	public Token longerSms(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token gsmSms(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token bulkSms(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Returns <code>0</code> if the debug mode is deactivate or returns
	 * <code>1</code> if is active.
	 *
	 * @return the code of debug mode
	 */
	public String getDebug() {
		return mDebug;
	}

	/**
	 * Sets the value of the debugging mode attribute.
	 *
	 * @param aDebug the debug code to set
	 */
	public void setDebug(String aDebug) {
		this.mDebug = aDebug;
	}

	/**
	 * Returns <code>0</code> if the return of sending costs is deactivate or
	 * returns <code>1</code> if is active.
	 *
	 * @return the code of the output of sending cost
	 */
	public String getCost() {
		return mCost;
	}

	/**
	 * Returns <code>0</code> if the return of the message ID is deactivate or
	 * returns <code>1</code> if is active.
	 *
	 * @return the code of the output of message ID
	 */
	public String getMessaggeId() {
		return mMessageId;
	}

	/**
	 * Returns <code>0</code> if the return of the number of SMS is deactivate
	 * or returns <code>1</code> if is active.
	 *
	 * @return the code of the output of count SMS
	 */
	public String getCount() {
		return mCount;
	}

	/**
	 * Returns <code>0</code> if receiving a delivery report for the SMS is
	 * deactivate or returns <code>1</code> if is active.
	 *
	 * @return the code of the delivery reports
	 */
	public String getDlr() {
		return mDlr;
	}

	/**
	 * Returns the &quot;smstrade&quot; reference for the SMS messages.
	 *
	 * @return the &quot;smstrade&quot; message reference
	 */
	public String getRef() {
		return mRef;
	}

	/**
	 * Returns <code>0</code> if longer SMS for more than 160 characters is
	 * deactivate or returns <code>1</code> if is active.
	 *
	 * @return the code of the longer SMS activation
	 */
	public String getConcatSms() {
		return mConcatSms;
	}

	/**
	 * Returns the &quot;smstrade&quot; route used to send the SMS text
	 * messages.
	 *
	 * @return the SMS route
	 */
	public String getRoute() {
		return mRoute;
	}

	/**
	 * Returns a UNIX timestamp that represents the time-delayed to send a SMS.
	 *
	 * @return the date when will be sent the SMS
	 */
	public String getSendDate() {
		return mSendDate;
	}

	/**
	 * Returns the type of SMS message that will be send. The existing messages
	 * type are:
	 * <p>
	 * <ul>
	 * <li>
	 * flash: Messages with media content.
	 * </li>
	 * <li>
	 * binary: Data messages with binary content for instance logos, picture
	 * messages, ringtones.
	 * </li>
	 * <li>
	 * unicode: Unicode messages are required for other alphabets like Arabic,
	 * Hebrew, Cyrillic and Latin with other special characters)
	 * </li>
	 * <li>
	 * voice: The SMS is converted into a spoken message by a computer and read
	 * out on the telephone.
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @return the message type
	 */
	public String getMessageType() {
		return mMessageType;
	}

	/**
	 * Sets the value of the return of sending costs attribute.
	 *
	 * @param aCost the cost code to set
	 */
	public void setCost(String aCost) {
		this.mCost = aCost;
	}

	/**
	 * Sets the value of the return of message ID attribute.
	 *
	 * @param aMessaggeId the message ID code to set
	 */
	public void setMessaggeId(String aMessaggeId) {
		this.mMessageId = aMessaggeId;
	}

	/**
	 * Sets the value of the return of number of SMS attribute.
	 *
	 * @param aCount the count code to set
	 */
	public void setCount(String aCount) {
		this.mCount = aCount;
	}

	/**
	 * Sets the value of the receiving a delivery report for the SMS attribute.
	 *
	 * @param aDlr the dlr code to set
	 */
	public void setDlr(String aDlr) {
		this.mDlr = aDlr;
	}

	/**
	 * Sets the value of the &quot;smstrade&quot; reference for the SMS
	 * messages.
	 *
	 * @param aRef the &quot;smstrade&quot; messages reference value to set
	 */
	public void setRef(String aRef) {
		this.mRef = aRef;
	}

	/**
	 * Sets the value of longer SMS attribute.
	 *
	 * @param aConcatSms the concat code to set
	 */
	public void setConcatSms(String aConcatSms) {
		this.mConcatSms = aConcatSms;
	}

	/**
	 * Sets the value of the &quot;smstrade&quot; route to send the messages.
	 *
	 * @param aRoute the &quot;smstrade&quot; route to set
	 */
	public void setRoute(String aRoute) {
		this.mRoute = aRoute;
	}

	/**
	 * Sets the value of the time-delayed when the messages will be send.
	 *
	 * @param aSendDate the sent date value to set
	 */
	public void setSendDate(String aSendDate) {
		this.mSendDate = aSendDate;
	}

	/**
	 * Sets the value of the type of message that will be send. The allowed
	 * types are:
	 * <p>
	 * <ul>
	 * <li>
	 * flash: Messages with media content.
	 * </li>
	 * <li>
	 * binary: Data messages with binary content for instance logos, picture
	 * messages, ringtones.
	 * </li>
	 * <li>
	 * unicode: Unicode messages are required for other alphabets like Arabic,
	 * Hebrew, Cyrillic and Latin with other special characters)
	 * </li>
	 * <li>
	 * voice: The SMS is converted into a spoken message by a computer and read
	 * out on the telephone.
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param aMessageType the message type to set
	 */
	public void setMessageType(String aMessageType) {
		this.mMessageType = aMessageType;
	}
}
