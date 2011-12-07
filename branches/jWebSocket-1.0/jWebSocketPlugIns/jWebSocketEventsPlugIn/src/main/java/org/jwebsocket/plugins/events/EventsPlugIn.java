//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.events;

import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.eventmodel.api.ISecureComponent;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.em.ConnectorStarted;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.EngineStarted;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 *
 * @author kyberneees
 */
public class EventsPlugIn extends TokenPlugIn implements ISecureComponent {

	private String configFile;
	private EventModel em;
	private static Logger mLog = Logging.getLogger(EventsPlugIn.class);
	//IWebSocketSecureObject fields
	private boolean securityEnabled = false;
	private Set<String> roles = new FastSet<String>();
	private Set<String> users = new FastSet<String>();
	private Set<String> ipAddresses = new FastSet<String>();

	/**
	 * @return The Spring IOC bean factory singleton instance
	 */
	public BeanFactory getBeanFactory() {
		return JWebSocketBeanFactory.getInstance(getNamespace());
	}

	/**
	 *
	 * @param configuration 
	 * @throws Exception
	 */
	public EventsPlugIn(PluginConfiguration configuration) throws Exception {
		super(configuration);

		this.setNamespace(configuration.getNamespace());

		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Creating EventsPlugIn instance for application '" + getNamespace() + "'...");
		}

		//Calling the init method
		initialize();
	}

	/**
	 * Initialize the EventsPlugIn
	 */
	public void initialize() {
		try {
			//Load application jars
			if (getSettings().containsKey("jars")) {
				if (mLog.isDebugEnabled()) {
					mLog.debug(">> Loading jars for '" + getNamespace() + "' application...");
				}

				String[] lJars = getString("jars").split(",");
				for (int i = 0; i < lJars.length; i++) {
					JWebSocketFactory.getClassLoader().add(JWebSocketConfig.getLibsFolder(lJars[i]));
					if (mLog.isDebugEnabled()) {
						mLog.debug(">> Loading jar '" + lJars[i] + "'...");
					}
				}
			}

			//Loading plug-in beans
			String lPath = JWebSocketConfig.getConfigFolder("EventsPlugIn/" + getNamespace() + "-application/bootstrap.xml");
			JWebSocketBeanFactory.load(getNamespace(), lPath, JWebSocketFactory.getClassLoader());

			//Getting the EventModel service instance
			em = (EventModel) getBeanFactory().getBean("EventModel");

			//Initializing the event model
			em.setParent(this);
			em.initialize();
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		//Engine started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Engine.started(" + aEngine.toString() + ") event notification...");
			}
			EngineStarted e = (EngineStarted) getEm().getEventFactory().stringToEvent("engine.started");
			e.setEngine(aEngine);
			e.initialize();
			em.notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
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
				mLog.debug(">> Engine.stopped(" + aEngine.toString() + ") event notification...");
			}
			EngineStopped e = (EngineStopped) getEm().getEventFactory().stringToEvent("engine.stopped");
			e.setEngine(aEngine);
			e.initialize();
			em.notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
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
				mLog.debug(">> Connector.started(" + aConnector.toString() + ") event notification...");
			}
			ConnectorStarted e = (ConnectorStarted) getEm().getEventFactory().stringToEvent("connector.started");
			e.setConnector(aConnector);
			e.initialize();
			em.notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			C2SEvent e = null;
			try {
				if (mLog.isDebugEnabled()) {
					mLog.debug(">> Processing token as event: '" + aToken.getType() + "'...");
				}
				e = getEm().getEventFactory().tokenToEvent(aToken);
				e.setConnector(aConnector);
				e.initialize();
			} catch (Exception ex) {
				mLog.error(ex.toString(), ex);
			}

			processEvent(aConnector, e);
			aResponse.abortChain();
		}
	}

	/**
	 * Process incoming events from the client
	 * 
	 * @param aConnector The client connector
	 * @param aEvent The event from the client
	 */
	public void processEvent(WebSocketConnector aConnector, C2SEvent aEvent) {
		em.processEvent(aEvent, null);
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
				mLog.debug(">> Connector.stopped(" + aConnector.toString() + ") event notification...");
			}
			ConnectorStopped e = (ConnectorStopped) getEm().getEventFactory().stringToEvent("connector.stopped");
			e.setConnector(aConnector);
			e.setCloseReason(aCloseReason);
			e.initialize();
			em.notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	/**
	 * @return The EventModel instance
	 */
	public EventModel getEm() {
		return em;
	}

	/**
	 * @param em The EventModel instance to set
	 */
	public void setEm(EventModel em) {
		this.em = em;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getRoles() {
		return roles;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setRoles(Set<String> roles) {
		this.roles.addAll(roles);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getUsers() {
		return users;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setUsers(Set<String> users) {
		this.users.addAll(users);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getIpAddresses() {
		return ipAddresses;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void setIpAddresses(Set<String> ipAddresses) {
		this.ipAddresses.addAll(ipAddresses);
	}

	/**
	 * @return The path to the XML root file
	 */
	public String getConfigFile() {
		return configFile;
	}
}
