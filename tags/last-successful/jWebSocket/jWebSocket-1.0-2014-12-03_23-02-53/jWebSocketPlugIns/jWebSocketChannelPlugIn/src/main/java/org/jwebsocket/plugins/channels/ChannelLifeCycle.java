//	---------------------------------------------------------------------------
//	jWebSocket - ChannelLifeCycle (Community Edition, CE)
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
 * Lifecycle interface for a channel
 *
 * @author puran
 * @version $Id: ChannelLifeCycle.java 1270 2010-12-23 08:53:06Z fivefeetfurther
 * $
 */
public interface ChannelLifeCycle {

	/**
	 *
	 * @throws ChannelLifeCycleException
	 */
	void init() throws ChannelLifeCycleException;

	/**
	 *
	 * @param user
	 * @throws ChannelLifeCycleException
	 */
	void start(String user) throws ChannelLifeCycleException;

	/**
	 *
	 * @param user
	 * @throws ChannelLifeCycleException
	 */
	void stop(String user) throws ChannelLifeCycleException;
}
