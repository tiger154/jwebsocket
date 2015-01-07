/*
 * Copyright 2014 aschulze.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwebsocket.plugins.jms.gateway;

import java.io.IOException;
import org.apache.activemq.command.BrokerInfo;
import org.apache.activemq.command.ConnectionControl;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.command.WireFormatInfo;
import org.apache.activemq.transport.TransportListener;
import org.apache.log4j.Logger;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.plugins.jms.JMSPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author aschulze
 */
public class JMSTransportListener implements TransportListener {

	private static final Logger mLog = Logger.getLogger(JMSTransportListener.class);
	private static JMSPlugIn mJMSPlugIn = null;
	private boolean mIsConnected = false;

	/**
	 * @return the mIsConnected
	 */
	public boolean isConnected() {
		return mIsConnected;
	}

	/**
	 *
	 * @param aJMSPlugIn
	 */
	public JMSTransportListener(JMSPlugIn aJMSPlugIn) {
		if (null == mJMSPlugIn) {
			mJMSPlugIn = aJMSPlugIn;
		}
	}

	/**
	 *
	 * @param aObject
	 */
	@Override
	public void onCommand(Object aObject) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Transport Command: " + aObject.toString());
		}
		boolean lBroadcast = true;
		final Token lToken = TokenFactory.createToken(mJMSPlugIn.getNamespace(), "event");
		lToken.setString("name", aObject.getClass().getSimpleName());
		if (aObject instanceof WireFormatInfo) {
			WireFormatInfo lWireFormatInfo = (WireFormatInfo) aObject;
			lToken.setInteger("version", lWireFormatInfo.getVersion());
			/*
			 Endpoint lEndpoint;
			 lEndpoint = lWireFormatInfo.getFrom();
			 lToken.setString("from", lEndpoint != null ? lEndpoint.getName() : "[unknown]");
			 lEndpoint = lWireFormatInfo.getTo();
			 lToken.setString("to", lEndpoint != null ? lEndpoint.getName() : "[unknown]");
			 */
		} else if (aObject instanceof BrokerInfo) {
			BrokerInfo lBrokerInfo = (BrokerInfo) aObject;
			lToken.setString("brokerURL", lBrokerInfo.getBrokerURL());
		} else if (aObject instanceof ConnectionControl) {
			ConnectionControl lConnectionControl = (ConnectionControl) aObject;
			lToken.setString("connectedBrokers", lConnectionControl.getConnectedBrokers());
		} else if (aObject instanceof MessageDispatch) {
			MessageDispatch lMessageDispatch = (MessageDispatch) aObject;
			lToken.setString("qualifiedName", lMessageDispatch.getDestination().getQualifiedName());
			mIsConnected = true;
			lBroadcast = false;
		}
		// CAUTION! THE FOLLOWING CODE LEADS TO A BLOCKING BEHAVIOR IN THE BROADCAST
		// DO WE NEED THIS MESSAGES BEING BROADCASTED?
		if (lBroadcast) {
			new Thread() {

				@Override
				public void run() {
					mJMSPlugIn.broadcastToken(null, lToken,
							new BroadcastOptions(
									false, // lIsSenderIncluded,
									false // lIsResponseRequested)
							));
				}
			}.start();
		}
	}

	/**
	 *
	 * @param aIOEx
	 */
	@Override
	public void onException(IOException aIOEx) {
		mLog.error("Transport Exception: " + aIOEx.toString());
		Token lToken = TokenFactory.createToken(mJMSPlugIn.getNamespace(), "event");
		lToken.setString("name", "BrokerException");
		lToken.setString("exception", aIOEx.getClass().getSimpleName());
		lToken.setString("message", aIOEx.getMessage());
		mJMSPlugIn.broadcastToken(null, lToken,
				new BroadcastOptions(
						false, // lIsSenderIncluded, 
						false // lIsResponseRequested)
				));
	}

	/**
	 *
	 */
	@Override
	public void transportInterupted() {
		mIsConnected = false;
		mLog.error("Transport interrupted!");
		Token lToken = TokenFactory.createToken(mJMSPlugIn.getNamespace(), "event");
		lToken.setString("name", "BrokerTransportInterrupted");
		lToken.setString("gatewayTopic", mJMSPlugIn.getSpringSettings().getGatewayTopic());
		lToken.setString("endpointId", mJMSPlugIn.getSpringSettings().getEndPointId());
		lToken.setString("brokerURI", mJMSPlugIn.getSpringSettings().getBrokerURI());
		mJMSPlugIn.broadcastToken(null, lToken,
				new BroadcastOptions(
						false, // lIsSenderIncluded, 
						false // lIsResponseRequested)
				));
	}

	/**
	 *
	 */
	@Override
	public void transportResumed() {
		if (mLog.isInfoEnabled()) {
			mLog.info("Transport resumed!");
		}
		mIsConnected = true;
		Token lToken = TokenFactory.createToken(mJMSPlugIn.getNamespace(), "event");
		lToken.setString("name", "BrokerTransportResumed");
		lToken.setString("gatewayTopic", mJMSPlugIn.getSpringSettings().getGatewayTopic());
		lToken.setString("endpointId", mJMSPlugIn.getSpringSettings().getEndPointId());
		lToken.setString("brokerURI", mJMSPlugIn.getSpringSettings().getBrokerURI());
		mJMSPlugIn.broadcastToken(null, lToken,
				new BroadcastOptions(
						false, // lIsSenderIncluded, 
						false // lIsResponseRequested)
				));
	}
	
}
