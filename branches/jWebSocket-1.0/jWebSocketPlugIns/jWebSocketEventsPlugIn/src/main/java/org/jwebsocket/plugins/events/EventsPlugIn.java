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
import org.json.JSONArray;
import org.json.JSONObject;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.eventmodel.api.ISecureComponent;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.em.ConnectorStarted;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.EngineStarted;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
 *
 * @author kyberneees
 */
public class EventsPlugIn extends TokenPlugIn implements ISecureComponent {

	private String xmlConfigFile;
	private EventModel em;
	private static BeanFactory beanFactory;
	private static Logger mLog = Logging.getLogger(EventsPlugIn.class);
	//IWebSocketSecureObject fields
	private boolean securityEnabled = false;
	private Set<String> roles = new FastSet<String>();
	private Set<String> users = new FastSet<String>();
	private Set<String> ipAddresses = new FastSet<String>();

	/**
	 * @return The Spring IOC bean factory singleton instance
	 */
	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * @param aBeanFactory The Spring IOC bean factory singleton instance to set
	 */
	public static void setBeanFactory(BeanFactory aBeanFactory) {
		beanFactory = aBeanFactory;
	}

	/**
	 *
	 * @param configuration 
	 * @throws Exception
	 */
	public EventsPlugIn(PluginConfiguration configuration) throws Exception {
		super(configuration);
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Creating the events plug-in instance...");
		}
		this.setNamespace(configuration.getNamespace());

		//Loading configuration
		JSONObject config = getJSON("config", new JSONObject());

		//Setting fields values
		xmlConfigFile = config.getString("xml_config");
		if (config.has("security_enabled")) {
			securityEnabled = config.getBoolean("security_enabled");
		}
		if (config.has("ip_addresses")) {
			JSONArray ips = config.getJSONArray("ip_addresses");
			for (int i = 0; i < ips.length(); i++) {
				ipAddresses.add(ips.get(i).toString());
			}
		}
		if (config.has("roles")) {
			JSONArray r = config.getJSONArray("roles");
			for (int i = 0; i < r.length(); i++) {
				roles.add(r.get(i).toString());
			}
		}
		if (config.has("users")) {
			JSONArray u = config.getJSONArray("users");
			for (int i = 0; i < u.length(); i++) {
				users.add(u.get(i).toString());
			}
		}

		//Calling the init method
		initialize();
	}

	/**
	 * Initialize the EventsPlugIn
	 */
	public void initialize() {
		try {
			//Creating the Spring Bean Factory
			//String lPath = JWebSocketConfig.getConfigFolder(getXmlConfigFile());
			beanFactory = new XmlBeanFactory(new FileSystemResource(getXmlConfigFile()));

			//Getting the EventModel service instance
			setEm((EventModel) beanFactory.getBean("EventModel"));

			//Initializing the event model
			getEm().setParent(this);
			getEm().initialize();
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
			getEm().notify(e, null, true);
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
			getEm().notify(e, null, true);
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
			getEm().notify(e, null, true);
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
		getEm().processEvent(aEvent, null);
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
			getEm().notify(e, null, true);
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
	public String getXmlConfigFile() {
		return xmlConfigFile;
	}

	/**
	 * @param xmlConfigFile The path to the XML root file to set
	 */
	public void setXmlConfigFile(String xmlConfigFile) {
		this.xmlConfigFile = xmlConfigFile;
	}
}
