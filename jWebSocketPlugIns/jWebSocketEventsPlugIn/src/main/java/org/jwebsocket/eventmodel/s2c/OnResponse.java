//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.s2c;

/**
 *
 * @author kyberneees
 */
public class OnResponse {

	private Object mContext;
	private String mRequiredType;
	private double mSentTime;
	private double mProcessingTime;
	private double mElapsedTime;

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
	 * @return Time required by the client to process the event
	 * <p>
	 * Time unit in nanoseconds or milliseconds depending of the client
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
	 * @return The complete time in nanoseconds passed from the "sent" time mark to 
	 * the "response received" time mark
	 */
	public double getElapsedTime() {
		return mElapsedTime;
	}

	/**
	 * @param aElapsedTime The complete time in nanoseconds passed from the "sent" 
	 * time mark to the "response received" time mark
	 */
	public void setElapsedTime(double aElapsedTime) {
		this.mElapsedTime = aElapsedTime;

		//TransactionContext support
		if (null != mContext && mContext instanceof TransactionContext) {
			((TransactionContext) mContext).setElapsedTime(aElapsedTime);
		}
	}
}
