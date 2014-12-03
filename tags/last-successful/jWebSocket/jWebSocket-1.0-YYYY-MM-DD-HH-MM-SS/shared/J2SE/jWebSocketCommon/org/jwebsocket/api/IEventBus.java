//	---------------------------------------------------------------------------
//	jWebSocket - IEventBus Interface (Community Edition, CE)
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

import org.jwebsocket.token.Token;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 * Component for event-based communication inspired on Vertx EventBus concept
 * http://vertx.io/core_manual_java.html#the-event-bus
 *
 * @author kyberneees
 */
public interface IEventBus {

	public interface IRegistration {

		/**
		 * Get name-space
		 *
		 * @return
		 */
		public String getNS();

		/**
		 * Cancel handler registration
		 */
		public void cancel();

		/**
		 * Get handler reference
		 *
		 * @return
		 */
		public IHandler getHandler();
	}

	public interface IHandler extends WebSocketResponseTokenListener {

		/**
		 * Reply token
		 *
		 * @param aResponse
		 */
		void reply(Token aResponse);

		/**
		 * Reply token
		 *
		 * @param aResponse
		 * @param aHandler
		 */
		void reply(Token aResponse, IHandler aHandler);

		void setEventBus(IEventBus aEB);

		IEventBus getEventBus();

		/**
		 * Called when a token is received on a target name-space
		 *
		 * @param aToken
		 */
		void OnMessage(Token aToken);
	}

	public interface IExceptionHandler {

		/**
		 * Handle uncaught exceptions during handlers invocation
		 *
		 * @param lEx
		 */
		void handle(Exception lEx);
	}
	
	/**
	 * Publish a token
	 *
	 * @param aToken
	 * @return
	 */
	IEventBus publish(Token aToken);

	/**
	 * Register a handler to a target name-space
	 *
	 * @param aNS
	 * @param aHandler
	 * @return
	 */
	IRegistration register(String aNS, IHandler aHandler);

	/**
	 * Send a token
	 *
	 * @param aToken
	 * @return
	 */
	IEventBus send(Token aToken);

	/**
	 * Send a token with reply processing
	 *
	 * @param aToken
	 * @param aHandler
	 * @return
	 */
	IEventBus send(Token aToken, IHandler aHandler);

	/**
	 * Create response Token message
	 *
	 * @param aInToken The request Token
	 * @return The response Token
	 */
	Token createResponse(Token aInToken);

	/**
	 * Create error response Token message
	 *
	 * @param aInToken The request Token
	 * @return The response Token
	 */
	Token createErrorResponse(Token aInToken);

	/**
	 * Set the EventBus exception handler to allow developers control of
	 * uncaught handlers exception during invocation.
	 *
	 * @param aHandler
	 */
	void setExceptionHandler(IExceptionHandler aHandler);
}
