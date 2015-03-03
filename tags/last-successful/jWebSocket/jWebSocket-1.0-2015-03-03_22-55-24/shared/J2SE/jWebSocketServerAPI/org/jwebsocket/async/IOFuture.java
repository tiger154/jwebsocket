// ---------------------------------------------------------------------------
// jWebSocket - IOFuture (Community Edition, CE)
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

import org.jwebsocket.api.WebSocketConnector;

/**
 * The result of an asynchronous {@link WebSocketConnector} I/O operation. <p>
 * This is to support asynchronous I/O operations in <tt>jWebSocket</tt>. It
 * means any I/O calls will return immediately with no guarantee that the
 * requested I/O operation has been completed at the end of the call. Instead,
 * you will be returned with a {@link IOFuture} instance which gives you the
 * information about the result or status of the <tt>jWebSocket</tt> I/O
 * operation. <p> A {@link IOFuture} is either <em>uncompleted</em> or
 * <em>completed</em>. When an I/O operation begins, a new future object is
 * created. The new future is uncompleted initially - it is neither succeeded,
 * failed, nor cancelled because the I/O operation is not finished yet. If the
 * I/O operation is finished either successfully, with failure, or by
 * cancellation, the future is marked as completed with more specific
 * information, such as the cause of the failure. Please note that even failure
 * and cancellation belong to the completed state.
 *
 * <pre>
 *                                      +---------------------------+
 *                                      | Completed successfully    |
 *                                      +---------------------------+
 *                                 +---->      isDone() = <b>true</b>|
 * +--------------------------+    |    |   isSuccess() = <b>true</b>|
 * |        Uncompleted       |    |    +===========================+
 * +--------------------------+    |    | Completed with failure    |
 * |      isDone() = <b>false</b>  |    +---------------------------+
 * |   isSuccess() = false    |----+---->   isDone() = <b>true</b>  |
 * | isCancelled() = false    |    |    | getCause() = <b>non-null</b>|
 * |    getCause() = null     |    |    +===========================+
 * +--------------------------+    |    | Completed by cancellation |
 *                                 |    +---------------------------+
 *                                 +---->      isDone() = <b>true</b>|
 *                                      | isCancelled() = <b>true</b>|
 *                                      +---------------------------+
 * </pre>
 *
 * Various methods are provided to let you check if the I/O operation has been
 * completed, wait for the completion, and retrieve the result of the I/O
 * operation. It also allows you to add {@link IOFutureListener}s so you can get
 * notified when the I/O operation is completed.
 *
 * @author <a href="http://www.purans.net/">Puran Singh</a>
 */
public interface IOFuture {

	/**
	 * Returns a connector where the I/O operation associated with this future
	 * takes place.
	 *
	 * @return
	 */
	WebSocketConnector getConnector();

	/**
	 * Returns {@code true} if and only if this future is complete, regardless
	 * of whether the operation was successful, failed, or cancelled.
	 *
	 * @return
	 */
	boolean isDone();

	/**
	 * Returns {@code true} if and only if this future was cancelled by a
	 * {@link #cancel()} method.
	 *
	 * @return
	 */
	boolean isCancelled();

	/**
	 * Returns {@code true} if and only if the I/O operation was completed
	 * successfully.
	 *
	 * @return
	 */
	boolean isSuccess();

	/**
	 * Returns the cause of the failed I/O operation if the I/O operation has
	 * failed.
	 *
	 * @return the cause of the failure. {@code null} if succeeded or this
	 * future is not completed yet.
	 */
	Throwable getCause();

	/**
	 * Cancels the I/O operation associated with this future and notifies all
	 * listeners if canceled successfully.
	 *
	 * @return {@code true} if and only if the operation has been canceled.
	 * {@code false} if the operation can't be canceled or is already completed.
	 */
	boolean cancel();

	/**
	 * Marks this future as a success and notifies all listeners.
	 *
	 * @return {@code true} if and only if successfully marked this future as a
	 * success. Otherwise {@code false} because this future is already marked as
	 * either a success or a failure.
	 */
	boolean setSuccess();

	/**
	 * Marks this future as a failure and notifies all listeners.
	 *
	 * @param cause
	 * @return {@code true} if and only if successfully marked this future as a
	 * failure. Otherwise {@code false} because this future is already marked as
	 * either a success or a failure.
	 */
	boolean setFailure(Throwable cause);

	/**
	 * Notifies the progress of the operation to the listeners that implements
	 * {@link ChannelFutureProgressListener}. Please note that this method will
	 * not do anything and return {@code false} if this future is complete
	 * already.
	 *
	 * @param amount
	 * @param current
	 * @param total
	 * @return {@code true} if and only if notification was made.
	 */
	boolean setProgress(long amount, long current, long total);

	/**
	 * Adds the specified listener to this future. The specified listener is
	 * notified when this future is {@linkplain #isDone() done}. If this future
	 * is already completed, the specified listener is notified immediately.
	 *
	 * @param listener
	 */
	void addListener(IOFutureListener listener);

	/**
	 * Removes the specified listener from this future. The specified listener
	 * is no longer notified when this future is {@linkplain #isDone() done}. If
	 * the specified listener is not associated with this future, this method
	 * does nothing and returns silently.
	 *
	 * @param listener
	 */
	void removeListener(IOFutureListener listener);
}
