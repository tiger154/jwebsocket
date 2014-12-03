//	---------------------------------------------------------------------------
//	jWebSocket - OnResponse (Community Edition, CE)
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
package org.jwebsocket.eventmodel.s2c;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class OnResponse {

	private Object mContext;
	private String mRequiredType;
	private double mSentTime;
	private double mProcessingTime;
	private double mElapsedTime;

	/**
	 *
	 */
	public OnResponse() {
		mContext = null;
	}

	/**
	 *
	 * @param aContext The context to use by the callbacks
	 */
	public OnResponse(Object aContext) {
		mContext = aContext;
	}

	/**
	 * Callback used to handle the success response from the client
	 *
	 * @param aResponse The response returned by the client-side
	 * @param aFrom The target client connector
	 */
	public void success(Object aResponse, String aFrom) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Callback used to handle the failure response from the client
	 *
	 * @param aReason The reason of why the s2c call has failed
	 * @param aFrom The target client connector
	 */
	public void failure(FailureReason aReason, String aFrom) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Execute custom validations in client responses
	 *
	 * @param aResponse The response to validate
	 * @param aFrom The target client connector
	 * @return
	 */
	public boolean isValid(Object aResponse, String aFrom) {
		return true;
	}

	/**
	 * @return The context to use by the callbacks
	 */
	public Object getContext() {
		return mContext;
	}

	/**
	 * @param aContext The context to use by the callbacks
	 */
	public void setContext(Object aContext) {
		this.mContext = aContext;
	}

	/**
	 * @return The required response type
	 */
	public String getRequiredType() {
		return mRequiredType;
	}

	/**
	 * @param aRequiredType The required response type to set
	 */
	public void setRequiredType(String aRequiredType) {
		this.mRequiredType = aRequiredType;
	}

	/**
	 * @return The time in nanoseconds from the sent point
	 */
	public double getSentTime() {
		return mSentTime;
	}

	/**
	 * @param aSentTime The time in nanoseconds from the sent point
	 */
	public void setSentTime(double aSentTime) {
		this.mSentTime = aSentTime;
	}

	/**
	 * @return Time required by the client to process the event <p> Time unit in
	 * nanoseconds or milliseconds depending of the client
	 */
	public double getProcessingTime() {
		return mProcessingTime;
	}

	/**
	 * @param aProcessingTime Time required by the client to process the event
	 */
	public void setProcessingTime(double aProcessingTime) {
		mProcessingTime = aProcessingTime;

		//TransactionContext support
		if (null != mContext && mContext instanceof TransactionContext) {
			((TransactionContext) mContext).setProcessingTime(aProcessingTime);
		}
	}

	/**
	 * @return The complete time in nanoseconds passed from the "sent" time mark
	 * to the "response received" time mark
	 */
	public double getElapsedTime() {
		return mElapsedTime;
	}

	/**
	 * @param aElapsedTime The complete time in nanoseconds passed from the
	 * "sent" time mark to the "response received" time mark
	 */
	public void setElapsedTime(double aElapsedTime) {
		this.mElapsedTime = aElapsedTime;

		//TransactionContext support
		if (null != mContext && mContext instanceof TransactionContext) {
			((TransactionContext) mContext).setElapsedTime(aElapsedTime);
		}
	}
}
