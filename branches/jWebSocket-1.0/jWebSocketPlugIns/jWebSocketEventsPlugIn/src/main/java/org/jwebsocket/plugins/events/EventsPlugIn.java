//	---------------------------------------------------------------------------
//	jWebSocket - EventsPlugIn (Community Edition, CE)
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
package org.jwebsocket.plugins.events;

import java.util.Set;
import javolution.util.FastSet;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.eventmodel.api.IEventModelBuilder;
import org.jwebsocket.eventmodel.api.IServerSecureComponent;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.em.ConnectorStarted;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.EngineStarted;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class EventsPlugIn extends TokenPlugIn implements IServerSecureComponent {

	private String mConfigFile;
	private EventModel mEm;
	private static final Logger mLog = Logging.getLogger();
	// IWebSocketSecureObject fields
	private boolean mSecurityEnabled = false;
	private Set<String> mRoles = new FastSet<String>();
	private Set<String> mUsers = new FastSet<String>();
	private Set<String> mIpAddresses = new FastSet<String>();
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket EventsPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket EventsPlugIn - Community Edition";

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public EventsPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		setAuthenticationMethod(TokenPlugIn.AUTHENTICATION_METHOD_SPRING);
		setNamespace(aConfiguration.getNamespace());

		if (mLog.isDebugEnabled()) {
			mLog.debug("Creating EventsPlugIn instance for '" + getNamespace() + "' application...");
		}

		//Calling the init method
		initialize();
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

	/**
	 * Initialize the EventsPlugIn
	 */
	public void initialize() {
		try {
			// Load application jars
			if (getSettings().containsKey("jars")) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading jars for '" + getNamespace() + "' application...");
				}

				String[] lJars = getString("jars").split(",");
				for (String lJar : lJars) {
					JWebSocketFactory.getClassLoader().addFile(JWebSocketConfig.getLibsFolder(lJar));
					if (mLog.isDebugEnabled()) {
						mLog.debug("Loading jar: '" + lJar + "'...");
					}
				}
			}

			//Getting the EventModel builder
			IEventModelBuilder lEventModelBuilder;

			if (getSettings().containsKey("em_builder")) {
				String lClass = getString("em_buidler");
				lEventModelBuilder = (IEventModelBuilder) Class.forName(lClass).newInstance();
			} else {
				//Setting the Spring EventModel builder by default
				lEventModelBuilder = SpringEventModelBuilder.class.newInstance();
			}
			//Setting the EventModel instance
			mEm = lEventModelBuilder.build(this);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "initializing " + getNamespace() + "-application"));
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		// Engine started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Engine.started (id '" + aEngine.getId() + "') event notification...");
			}
			EventModel lEM = getEm();
			if (null != lEM) {
				EngineStarted lEvent = (EngineStarted) lEM.getEventFactory().idToEvent("engine.started");
				lEvent.setEngine(aEngine);
				lEvent.initialize();
				mEm.notify(lEvent, null, true);
			} else {
				mLog.error("EventModel instance not available on engine start "
						+ "for " + getNamespace()
						+ "-application, probably wrong Spring configuration.");
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "engine started"));
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		//Engine started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Engine.stopped(" + aEngine.toString() + ") event notification...");
			}
			EventModel lEM = getEm();
			if (null != lEM) {
				EngineStopped lEvent = (EngineStopped) lEM.getEventFactory().idToEvent("engine.stopped");
				lEvent.setEngine(aEngine);
				lEvent.initialize();
				mEm.notify(lEvent, null, true);
			} else {
				mLog.error("EventModel instance not available on engine stop "
						+ "for " + getNamespace()
						+ "-application, probably wrong Spring configuration.");
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "engine stopped"));
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		//Connector started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Connector.started (" + aConnector.toString() + ") event notification...");
			}
			EventModel lEM = getEm();
			if (null != lEM) {
				ConnectorStarted lEvent =
						(ConnectorStarted) lEM.getEventFactory().idToEvent("connector.started");
				lEvent.setConnector(aConnector);
				lEvent.initialize();
				mEm.notify(lEvent, null, true);
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "connector started"));
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		C2SEvent lEvent = null;
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing token as event: '" + aToken.getType() + "'...");
			}
			lEvent = getEm().getEventFactory().tokenToEvent(aToken);
			lEvent.setConnector(aConnector);

			//Initializing the event...
			lEvent.initialize();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "process token"));
		}

		processEvent(aConnector, lEvent);
		aResponse.abortChain();
	}

	/**
	 * Process incoming events from the client
	 *
	 * @param aConnector The client connector
	 * @param aEvent The event from the client
	 */
	public void processEvent(WebSocketConnector aConnector, C2SEvent aEvent) {
		mEm.processEvent(aEvent, null);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		//Connector stopped event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Connector.stopped (" + aConnector.toString() + ") event notification...");
			}
			EventModel lEM = getEm();
			if (null != lEM) {
				ConnectorStopped lEvent =
						(ConnectorStopped) lEM.getEventFactory().idToEvent("connector.stopped");
				lEvent.setConnector(aConnector);
				lEvent.setCloseReason(aCloseReason);
				lEvent.initialize();
				mEm.notify(lEvent, null, true);
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "connector stopped"));
		}
	}

	/**
	 * @return The EventModel instance
	 */
	public EventModel getEm() {
		return mEm;
	}

	/**
	 * @param aEm
	 */
	public void setEm(EventModel aEm) {
		this.mEm = aEm;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public boolean isSecurityEnabled() {
		return mSecurityEnabled;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setSecurityEnabled(boolean aSecurityEnabled) {
		this.mSecurityEnabled = aSecurityEnabled;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getRoles() {
		return mRoles;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setRoles(Set<String> aRoles) {
		this.mRoles.addAll(aRoles);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getUsers() {
		return mUsers;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setUsers(Set<String> aUsers) {
		this.mUsers.addAll(aUsers);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getIpAddresses() {
		return mIpAddresses;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setIpAddresses(Set<String> aIpAddresses) {
		this.mIpAddresses.addAll(aIpAddresses);
	}

	/**
	 * @return The path to the XML root file
	 */
	public String getConfigFile() {
		return mConfigFile;
	}
}
