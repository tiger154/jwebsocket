//	---------------------------------------------------------------------------
//	jWebSocket - In- and Outbound Stream (Community Edition, CE)
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
package org.jwebsocket.plugins.streaming;

import java.util.Date;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketStream;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.BaseServer;

/**
 * implements a stream on which connectors can be registered and unregistered.
 * The fundamental streaming capabilities are provided by the
 * <tt>BaseStream</tt>
 * class. The <tt>BaseStream</tt> implements an internal queue to which messages
 * can be posted. The message then are broadcasted to the registered clients.
 * Therefore the <tt>BaseStream</tt> class maintains a list of clients. A
 * certain client can register at or unregister from the stream. Basically
 * streams send their messages only to clients that are registered at a stream.
 *
 * @author Alexander Schulze
 */
public class BaseStream implements WebSocketStream {

	private static final Logger mLog = Logging.getLogger();
	private final List<WebSocketConnector> mConnectors
			= new FastList<WebSocketConnector>();
	private boolean mIsRunning = false;
	private String mStreamID = null;
	private final List<Object> mQueue = new FastList<Object>();
	private Thread mQueueThread = null;

	/**
	 * creates a new stream with a certain id.
	 *
	 * @param aStreamID
	 */
	public BaseStream(String aStreamID) {
		this.mStreamID = aStreamID;
	}

	/**
	 *
	 * @param aTimeout
	 */
	@Override
	public void startStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Base stream...");
		}
		QueueProcessor lQueueProcessor = new QueueProcessor();
		mQueueThread = new Thread(lQueueProcessor, "jWebSocket Streaming Plug-in, Base QueueProcessor");
		mQueueThread.start();
	}

	/**
	 *
	 * @param aTimeout
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Base stream...");
		}
		long lStarted = new Date().getTime();
		mIsRunning = false;
		synchronized (mQueue) {
			// trigger sender thread to terminate
			mQueue.notify();
		}
		try {
			mQueueThread.join(aTimeout);
		} catch (InterruptedException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			long lDuration = new Date().getTime() - lStarted;
			if (mQueueThread.isAlive()) {
				mLog.warn("Base stream did not stopped after " + lDuration + "ms.");
			} else {
				mLog.debug("Base stream stopped after " + lDuration + "ms.");
			}
		}
	}

	/**
	 * registers a connector at the stream. After this operation the stream will
	 * send new messages to this client as well.
	 *
	 * @param aConnector
	 */
	public void registerConnector(WebSocketConnector aConnector) {
		if (aConnector != null) {
			mConnectors.add(aConnector);
		}
	}

	/**
	 * checks if a certain connector is registered at the stream.
	 *
	 * @param aConnector
	 * @return <tt>true</tt> if the connector is already registered otherwise
	 * <tt>false</tt>.
	 */
	public boolean isConnectorRegistered(WebSocketConnector aConnector) {
		return (aConnector != null && mConnectors.indexOf(aConnector) >= 0);
	}

	/**
	 * unregisters a connector from the stream. After this operation the stream
	 * will no longer new messages to this client.
	 *
	 * @param aConnector
	 */
	public void unregisterConnector(WebSocketConnector aConnector) {
		if (aConnector != null) {
			mConnectors.remove(aConnector);
		}
	}

	/**
	 * registers all connectors of the given server at the stream. After this
	 * operation the stream will send new messages to all clients on the given
	 * server.
	 *
	 * @param aServer
	 */
	public void registerAllConnectors(BaseServer aServer) {
		// TODO: to be implemented!
	}

	/**
	 * unregisters all connectors of the given server from the stream. After
	 * this operation the stream will no longer send new messages to any clients
	 * on the given server.
	 *
	 * @param aServer
	 */
	public void unregisterAllConnectors(BaseServer aServer) {
		// TODO: to be implemented!
	}

	/**
	 * puts a data packet into the stream queue.
	 *
	 * @param aObject
	 */
	public void put(Object aObject) {
		synchronized (mQueue) {
			// add the queue item into the queue
			mQueue.add(aObject);
			// trigger sender thread
			mQueue.notify();
		}
	}

	/**
	 * sends a message from the queue to a certain connector.
	 *
	 * @param aConnector
	 * @param aObject
	 */
	protected void processConnector(WebSocketConnector aConnector, Object aObject) {
		try {
			aConnector.sendPacket(new RawPacket(aObject.toString()));
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
	}

	/**
	 * iterates through all registered connectors and runs
	 * <tt>processConnector</tt> for each.
	 *
	 * @param aObject
	 */
	protected void processItem(Object aObject) {
		for (WebSocketConnector lConnector : mConnectors) {
			processConnector(lConnector, aObject);
		}
	}

	private class QueueProcessor implements Runnable {

		@Override
		public void run() {
			mIsRunning = true;
			Thread.currentThread().setName("jWebSocket StreamingPlugIn Queue");

			while (mIsRunning) {
				synchronized (mQueue) {
					if (mQueue.size() > 0) {
						Object lObject = mQueue.remove(0);
						processItem(lObject);
					} else {
						try {
							mQueue.wait();
						} catch (InterruptedException lEx) {
							mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
						}
					}
				}
			}

		}
	}

	/**
	 * returns the id of the stream.
	 *
	 * @return the streamID
	 */
	public String getStreamID() {
		return mStreamID;
	}
}
