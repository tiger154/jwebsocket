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
package org.jwebsocket.eventmodel.plugin.system;

import javolution.util.FastList;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.event.system.GetPlugInAPI;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.system.ClientCacheAspectStatus;
import org.jwebsocket.eventmodel.event.system.GetPlugInList;
import org.jwebsocket.eventmodel.event.system.HasPlugIn;
import org.jwebsocket.eventmodel.filter.cache.CacheFilter;
import org.jwebsocket.eventmodel.filter.validator.Argument;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class SystemPlugIn extends EventModelPlugIn {

	private static Logger mLog = Logging.getLogger(SystemPlugIn.class);

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * Return the plug-ins identifiers list
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(GetPlugInList aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Exporting the plugIns identifiers list...");
		}

		FastList<String> plugInIdentifiers = new FastList<String>();
		for (IEventModelPlugIn p : getEm().getPlugIns()) {
			plugInIdentifiers.add(p.getId());
		}

		aResponseEvent.getArgs().setList("identifiers", plugInIdentifiers);
	}

	/**
	 * Return the plug-ins identifiers list
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(ClientCacheAspectStatus aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Setting the client cache aspect status ...");
		}

		aEvent.getConnector().setVar(CacheFilter.CLIENT_CACHE_ASPECT_STATUS, aEvent.isEnabled());
	}

	/**
	 * Indicate if a plug-in exists using it identifier
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(HasPlugIn aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		boolean has = false;
		for (IEventModelPlugIn p : getEm().getPlugIns()) {
			if (p.getId().equals(aEvent.getPluginId())) {
				has = true;
				break;
			}
		}

		aResponseEvent.getArgs().setBoolean("has", has);
	}

	/**
	 * Return a plug-in API using it identifier
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(GetPlugInAPI aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		String aPlugInId = aEvent.getPluginId();
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Exporting API for '" + aPlugInId + "' plugIn...");
		}

		IEventModelPlugIn plugIn = getEm().getPlugIn(aPlugInId);
		Token api = TokenFactory.createToken();
		Token method, arg;
		C2SEventDefinition def = null;
		FastList<String> roles, users, ipAddresses;
		FastList<Token> incomingArgs, outgoingArgs;

		try {
			for (String key : plugIn.getClientAPI().keySet()) {
				String aEventId = getEm().getEventFactory().
						eventToString(plugIn.getClientAPI().get(key));

				/**
				 * Getting events plug-in API definition
				 */
				def = getEm().getEventFactory().getEventDefinitions().getDefinition(aEventId);

				method = TokenFactory.createToken();
				method.setString("type", aEventId);
				method.setBoolean("isCacheEnabled", def.isCacheEnabled());
				method.setBoolean("isCachePrivate", def.isCachePrivate());
				method.setBoolean("isSecurityEnabled", def.isSecurityEnabled());
				method.setInteger("cacheTime", def.getCacheTime());
				method.setInteger("timeout", def.getTimeout());
				roles = new FastList<String>();
				for (String r : def.getRoles()) {
					roles.add(r);
				}
				method.setList("roles", roles);
				users = new FastList<String>();
				for (String u : def.getUsers()) {
					users.add(u);
				}
				method.setList("users", users);
				ipAddresses = new FastList<String>();
				for (String ip : def.getIpAddresses()) {
					ipAddresses.add(ip);
				}
				method.setList("ip_addresses", ipAddresses);

				incomingArgs = new FastList<Token>();
				for (Argument a : def.getIncomingArgsValidation()) {
					arg = TokenFactory.createToken();
					arg.setString("name", a.getName());
					arg.setString("type", a.getType());
					arg.setBoolean("optional", a.isOptional());

					incomingArgs.add(arg);
				}
				method.setList("incomingArgsValidation", incomingArgs);

				outgoingArgs = new FastList<Token>();
				for (Argument a : def.getOutgoingArgsValidation()) {
					arg = TokenFactory.createToken();
					arg.setString("name", a.getName());
					arg.setString("type", a.getType());
					arg.setBoolean("optional", a.isOptional());

					outgoingArgs.add(arg);
				}
				method.setList("outgoingArgsValidation", outgoingArgs);
				api.setToken(key, method);
			}

			//PlugIn id
			aResponseEvent.getArgs().setString("id", plugIn.getId());
			//PlugIn API
			aResponseEvent.getArgs().setToken("api", api);

		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}
}
