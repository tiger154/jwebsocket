//	---------------------------------------------------------------------------
//	jWebSocket - OnResponseCallback (Community Edition, CE)
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
package org.jwebsocket.eventmodel.rrpc;

import org.jwebsocket.eventmodel.api.IRRPCOnResponseCallback;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class OnResponseCallback implements IRRPCOnResponseCallback {

	private Object context;
	private String requiredType;
	private double sentTime;
	private double processingTime;
	private double elapsedTime;

	/**
	 *
	 * @param aContext The context to use by the callbacks
	 */
	public OnResponseCallback(Object aContext) {
		context = aContext;
	}

	/**
	 * Callback used to handle the success response from the client
	 *
	 * @param response The response returned by the client-side
	 * @param from The target client connector
	 */
	@Override
	public void success(Object response, String from) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Callback used to handle the failure response from the client
	 *
	 * @param reason The reason of why the s2c call has failed
	 * @param from The target client connector
	 */
	@Override
	public void failure(FailureReason reason, String from) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Execute custom validations in client responses
	 *
	 * @param response The response to validate
	 * @param from The target client connector
	 * @return
	 */
	@Override
	public boolean isValid(Object response, String from) {
		return true;
	}

	/**
	 * @return The context to use by the callbacks
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * @param context The context to use by the callbacks
	 */
	public void setContext(Object context) {
		this.context = context;
	}

	/**
	 * @return The required response type
	 */
	@Override
	public String getRequiredType() {
		return requiredType;
	}

	/**
	 * @param requiredType The required response type to set
	 */
	@Override
	public void setRequiredType(String requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * @return The time in nanoseconds from the sent point
	 */
	@Override
	public double getSentTime() {
		return sentTime;
	}

	/**
	 * @param sentTime The time in nanoseconds from the sent point
	 */
	@Override
	public void setSentTime(double sentTime) {
		this.sentTime = sentTime;
	}

	/**
	 * @return Time required by the client to process the event
	 * <p>
	 * Time unit in nanoseconds or milliseconds depending of the client
	 */
	@Override
	public double getProcessingTime() {
		return processingTime;
	}

	/**
	 * @param processingTime Time required by the client to process the event
	 */
	@Override
	public void setProcessingTime(double processingTime) {
		this.processingTime = processingTime;

		//TransactionContext support
		if (null != context && context instanceof TransactionContext) {
			((TransactionContext) context).setProcessingTime(processingTime);
		}
	}

	/**
	 * @return The complete time in nanoseconds passed from the "sent" time mark
	 * to the "response received" time mark
	 */
	@Override
	public double getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param elapsedTime The complete time in nanoseconds passed from the
	 * "sent" time mark to the "response received" time mark
	 */
	@Override
	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;

		//TransactionContext support
		if (null != context && context instanceof TransactionContext) {
			((TransactionContext) context).setElapsedTime(elapsedTime);
		}
	}
}
