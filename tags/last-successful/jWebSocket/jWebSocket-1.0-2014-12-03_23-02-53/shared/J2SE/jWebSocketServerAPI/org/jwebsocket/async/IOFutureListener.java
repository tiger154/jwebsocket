// ---------------------------------------------------------------------------
// jWebSocket - IOFutureListener (Community Edition, CE)
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
package org.jwebsocket.async;

import java.util.EventListener;
import org.jwebsocket.api.WebSocketConnector;

/**
 * Listens to the result of a {@link IOFuture}. The result of the asynchronous
 * {@link WebSocketConnector} I/O operation is notified once this listener is
 * added by calling {@link IOFuture#addListener(IOFutureListener)}.
 *
 * <h3>Return the control to the caller quickly</h3>
 *
 * {@link #operationComplete(IOFuture)} is directly called by an I/O thread.
 * Therefore, performing a time consuming task or a blocking operation in the
 * handler method can cause an unexpected pause during I/O. If you need to
 * perform a blocking operation on I/O completion, try to execute the operation
 * in a different thread using a thread pool.
 *
 * @author <a href="http://purans.net/">Puran Singh</a>
 * @version $Id: IOFutureListener.java 1049 2010-09-20 05:07:32Z
 * mailtopuran@gmail.com $
 */
public interface IOFutureListener extends EventListener {

	/**
	 * Invoked when the I/O operation associated with the {@link IOFuture} has
	 * been completed.
	 *
	 * @param future the source {@link IOFuture} which called this callback
	 * @throws Exception
	 */
	void operationComplete(IOFuture future) throws Exception;
}
