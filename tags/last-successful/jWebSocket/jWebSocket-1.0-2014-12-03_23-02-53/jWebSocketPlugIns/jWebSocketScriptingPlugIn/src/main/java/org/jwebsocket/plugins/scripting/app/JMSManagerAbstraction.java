//	---------------------------------------------------------------------------
//	jWebSocket - JMSManagerAbstraction for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.util.JMSManager;

/**
 * JMS based messaging abstraction for Script applications
 *
 * @author Rolando Santamaria Maso
 */
public class JMSManagerAbstraction extends JMSManager {

	private BaseScriptApp mScriptApp;

	/**
	 *
	 * @param aScriptApp
	 */
	public JMSManagerAbstraction(BaseScriptApp aScriptApp) {
		this(aScriptApp, false);
	}

	/**
	 *
	 * @param aScriptApp
	 * @param aUseTransaction
	 */
	public JMSManagerAbstraction(BaseScriptApp aScriptApp, boolean aUseTransaction) {
		this(aScriptApp, aUseTransaction, null);
	}

	/**
	 *
	 * @param aScriptApp
	 * @param aUseTransaction
	 * @param aConn
	 */
	public JMSManagerAbstraction(BaseScriptApp aScriptApp, boolean aUseTransaction, Connection aConn) {
		this(aScriptApp, aUseTransaction, aConn, null);
	}

	/**
	 *
	 * @param aScriptApp
	 * @param aUseTransaction
	 * @param aConn
	 * @param aDefaultDestination
	 */
	public JMSManagerAbstraction(BaseScriptApp aScriptApp, boolean aUseTransaction, Connection aConn,
			String aDefaultDestination) {
		super(aUseTransaction, (null == aConn)
				? ((TokenServer) aScriptApp.getWebSocketServer()).getJMSManager().getConnection()
				: aConn, aDefaultDestination);

		mScriptApp = aScriptApp;
	}

	/**
	 * Subscribe to default destination
	 *
	 * @param aCallback
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(Object aCallback) throws JMSException, Exception {
		return subscribeTo(getDefaultDestination(), aCallback);
	}

	/**
	 * Subscribe to a target destination
	 *
	 * @param aDestination
	 * @param aCallback
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribeTo(String aDestination, Object aCallback) throws JMSException, Exception {
		return subscribeTo(aDestination, aCallback, null);
	}

	/**
	 * Subscribe to default destination
	 *
	 * @param aCallback
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(Object aCallback,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return subscribeTo(getDefaultDestination(), aCallback, aDurableSubscription, aSubscriptionId);
	}

	/**
	 * Subscribe to a target destination
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribeTo(String aDestination, Object aCallback,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return subscribeTo(aDestination, aCallback, null, aDurableSubscription, aSubscriptionId);
	}

	/**
	 * Subscribe to default destination
	 *
	 * @param aCallback
	 * @param aSelector
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(Object aCallback, String aSelector) throws JMSException, Exception {
		return subscribeTo(getDefaultDestination(), aCallback, aSelector);
	}

	/**
	 * Subscribe to a target destination using a message selector
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aSelector
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribeTo(String aDestination, Object aCallback,
			String aSelector) throws JMSException, Exception {
		return subscribeTo(aDestination, aCallback, aSelector, false, null);
	}

	/**
	 * Subscribe to default destination using a message selector
	 *
	 * @param aCallback
	 * @param aSelector
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(Object aCallback, String aSelector,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return subscribeTo(getDefaultDestination(), aCallback, aSelector,
				aDurableSubscription, aSubscriptionId);
	}

	/**
	 * Subscribe to a target destination using a message selector
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aSelector
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribeTo(String aDestination, Object aCallback, String aSelector,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return super.subscribe(aDestination, (MessageListener) mScriptApp.cast(aCallback,
				MessageListener.class), aSelector, aDurableSubscription, aSubscriptionId);
	}
}
