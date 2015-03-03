//	---------------------------------------------------------------------------
//	jWebSocket - ChannelLifeCycleException (Community Edition, CE)
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
package org.jwebsocket.plugins.channels;

/**
 * Channel lifecycle exception
 *
 * @author puran
 * @version $Id:$
 */
public class ChannelLifeCycleException extends Exception {

	/**
	 *
	 * @param aError
	 */
	public ChannelLifeCycleException(String aError) {
		super(aError);
	}

	/**
	 *
	 * @param aError
	 */
	public ChannelLifeCycleException(Throwable aError) {
		super(aError);
	}

	/**
	 *
	 * @param aError
	 * @param aThrowable
	 */
	public ChannelLifeCycleException(String aError, Throwable aThrowable) {
		super(aError, aThrowable);
	}
	private static final long serialVersionUID = 1L;
}
