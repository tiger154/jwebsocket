//	---------------------------------------------------------------------------
//	jWebSocket - Streaming Plug-In (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

import java.util.Collections;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketStream;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * implements the stream control plug-in to manage the various underlying
 * streams. Streams are instantiated by the application and registered at the
 * streaming plug-in. The streaming plug-in only can control streams but not
 * instantiate new streams.
 *
 * @author aschulze
 */
public class StreamingPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private final static String NS_STREAMING =
			JWebSocketServerConstants.NS_BASE + ".plugins.streaming";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket StreaminglugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket StreamingPlugIn - Community Edition";
	private Map<String, BaseStream> mStreams = new FastMap<String, BaseStream>();
	private boolean mStreamsInitialized = false;
	private TimeStream mTimeStream = null;
	private MonitorStream mMonitorStream = null;
	private StressStream mStressStream = null;
	private JDBCStream mJDBCStream = null;
	private StatisticStream mStatisticStream = null;

	/**
	 * create a new instance of the streaming plug-in and set the default name
	 * space for the plug-in.
	 *
	 * @param aConfiguration
	 */
	public StreamingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating streaming plug-in...");
		}

		// specify default name space for streaming plugin
		this.setNamespace(NS_STREAMING);

		if (mLog.isInfoEnabled()) {
			mLog.info("Streaming plug-in successfully instantiated.");
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_STREAMING;
	}

	private void startStreams() {
		if (!mStreamsInitialized) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting registered streams...");
			}
			TokenServer lTokenServer = getServer();
			if (lTokenServer != null) {
				// create the stream for the time stream demo
				mTimeStream = new TimeStream("timeStream", lTokenServer);
				addStream(mTimeStream);
				// create the stream for the monitor stream demo
				mMonitorStream = new MonitorStream("monitorStream", lTokenServer);
				addStream(mMonitorStream);
				// create the stream for the monitor stream demo
				mStressStream = new StressStream("stressStream", lTokenServer);
				addStream(mStressStream);
				// create the stream for the monitor stream demo
				mJDBCStream = new JDBCStream("jdbcStream", lTokenServer);
				addStream(mJDBCStream);
				// create the stream for the statistics stream demo
				mStatisticStream = new StatisticStream("statisticStream", lTokenServer);
				addStream(mStatisticStream);
				mStreamsInitialized = true;
			}
		}
	}

	private void stopStreams() {
		if (mStreamsInitialized) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Stopping registered streams...");
			}
			TokenServer lTokenServer = getServer();
			if (lTokenServer != null) {
				// stop the stream for the time stream demo
				if (mTimeStream != null) {
					mTimeStream.stopStream(3000);
				}
				// stop the stream for the monitor stream demo
				if (mMonitorStream != null) {
					mMonitorStream.stopStream(3000);
				}
				// stop the stream for the stress stream demo
				if (mStressStream != null) {
					mStressStream.stopStream(3000);
				}
				// stop the stream for the JDBC stream demo
				if (mJDBCStream != null) {
					mJDBCStream.stopStream(3000);
				}
				// stop the stream for the statisticStream stream demo
				if (mStatisticStream != null) {
					mStatisticStream.stopStream(3000);
				}
				mTimeStream = null;
				mMonitorStream = null;
				mStressStream = null;
				mJDBCStream = null;
				mStatisticStream = null;
				mStreamsInitialized = false;
			}
		}
	}

	/**
	 * adds a new stream to the mapo of streams. The stream must not be null and
	 * must have a valid and unqiue id.
	 *
	 * @param aStream
	 */
	public void addStream(BaseStream aStream) {
		if (aStream != null && aStream.getStreamID() != null) {
			mStreams.put(aStream.getStreamID(), aStream);
		}
	}

	/**
	 *
	 * @param aStreamId
	 * @return
	 */
	public WebSocketStream getStream(String aStreamId) {
		return mStreams.get(aStreamId);
	}

	@Override
	public void processToken(PlugInResponse aAction, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("register")) {
				registerConnector(aConnector, aToken);
			} else if (lType.equals("unregister")) {
				unregisterConnector(aConnector, aToken);
			} else if (lType.equals("controlStream")) {
				controlStream(aConnector, aToken);
			}
		}
	}

	/**
	 * registers a connector at a certain stream.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void registerConnector(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'register'...");
		}

		BaseStream lStream = null;
		String lStreamID = aToken.getString("stream");
		if (lStreamID != null) {
			lStream = mStreams.get(lStreamID);
		}

		if (lStream != null) {
			if (!lStream.isConnectorRegistered(aConnector)) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Registering client at stream '" + lStreamID + "'...");
				}
				lStream.registerConnector(aConnector);
			} else {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "Client is already registered at stream '" + lStreamID + "'.");
			}
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Stream '" + lStreamID + "' not found.");
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * unregisters a connector from a certain stream.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void unregisterConnector(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'unregister'...");
		}

		BaseStream lStream = null;
		String lStreamID = aToken.getString("stream");
		if (lStreamID != null) {
			lStream = mStreams.get(lStreamID);
		}

		if (lStream != null) {
			if (lStream.isConnectorRegistered(aConnector)) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Unregistering client from stream '" + lStreamID + "'...");
				}
				lStream.unregisterConnector(aConnector);
			} else {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "Client is not registered at stream '" + lStreamID + "'.");
			}
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Stream '" + lStreamID + "' not found.");
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * unregisters a connector from a certain stream.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void controlStream(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'controlStream'...");
		}

		TokenStream lStream = null;
		String lStreamID = aToken.getString("stream");
		if (lStreamID != null) {
			lStream = (TokenStream) mStreams.get(lStreamID);
		}

		if (lStream != null) {
			lStream.control(aToken);
		}
		// else...
		// todo: error handling
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// if a connector terminates, unregister it from all streams.
		for (BaseStream lStream : mStreams.values()) {
			try {
				lStream.unregisterConnector(aConnector);
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping conncector"));
			}
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		startStreams();
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		stopStreams();
	}

	/**
	 * @return the map streams
	 */
	public Map<String, BaseStream> getStreams() {
		return Collections.unmodifiableMap(mStreams);
	}
}
