//	---------------------------------------------------------------------------
//	jWebSocket - ISystemLifecycle (Community Edition, CE)
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
package org.jwebsocket.api;

/**
 *
 * @author Rolando Santamaria Maso
 * @author Alexander Schulze
 */
public interface ISystemLifecycle {

	/**
	 * Called when the jWebSocket server system is starting.
	 *
	 * @throws Exception
	 */
	void systemStarting() throws Exception;

	/**
	 * Called when the full jWebSocket server system has been started.
	 *
	 * @throws Exception
	 */
	void systemStarted() throws Exception;

	/**
	 * Called when the jWebSocket server system is going to be stopped.
	 *
	 * @throws Exception
	 */
	void systemStopping() throws Exception;

	/**
	 * Called when the jWebSocket server system has been stopped.
	 *
	 * @throws Exception
	 */
	void systemStopped() throws Exception;
}
