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
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.eventmodel.filter.validator.Argument;
import javolution.util.FastSet;
import java.util.Set;
import javolution.util.FastList;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.api.ISecureComponent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.validation.Validator;

/**
 *
 * @author kyberneees
 */
public class C2SEventDefinition implements IInitializable, ISecureComponent, ITokenizable {

	private String id;
	private String ns;
	private Set<Argument> incomingArgsValidation = new FastSet<Argument>();
	private Set<Argument> outgoingArgsValidation = new FastSet<Argument>();
	private boolean responseRequired = false;
	private boolean responseToOwnerConnector = false;
	private boolean responseAsync = true;
	private boolean cacheEnabled = false;
	private boolean cachePrivate = false;
	private boolean notificationConcurrent = false;
	private int cacheTime = 0;
	private boolean securityEnabled = false;
	private Set<String> roles = new FastSet<String>();
	private Set<String> users = new FastSet<String>();
	private Set<String> ipAddresses = new FastSet<String>();
	private Validator validator;
	private Integer timeout = 100;

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() {
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void shutdown() {
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public int hashCode() {
		return ((null != id) ? id.hashCode() : 0)
				+ incomingArgsValidation.hashCode()
				+ outgoingArgsValidation.hashCode()
				+ ((responseRequired) ? 1 : 0)
				+ ((responseToOwnerConnector) ? 1 : 0)
				+ ((responseAsync) ? 1 : 0)
				+ ((cacheEnabled) ? 1 : 0)
				+ ((cachePrivate) ? 1 : 0)
				+ ((notificationConcurrent) ? 1 : 0)
				+ cacheTime
				+ ((null != validator) ? validator.hashCode() : 0)
				+ ((securityEnabled) ? 1 : 0)
				+ roles.hashCode()
				+ users.hashCode()
				+ ((null != ns) ? ns.hashCode() : 0)
				+ ((null != ipAddresses) ? ipAddresses.hashCode() : 0)
				+ timeout;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final C2SEventDefinition other = (C2SEventDefinition) obj;
		if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
			return false;
		}
		if (this.incomingArgsValidation != other.getIncomingArgsValidation() && (this.incomingArgsValidation == null || !this.incomingArgsValidation.equals(other.getIncomingArgsValidation()))) {
			return false;
		}
		if (this.outgoingArgsValidation != other.getOutgoingArgsValidation() && (this.outgoingArgsValidation == null || !this.outgoingArgsValidation.equals(other.getOutgoingArgsValidation()))) {
			return false;
		}
		if (this.responseRequired != other.isResponseRequired()) {
			return false;
		}
		if (this.responseAsync != other.isResponseAsync()) {
			return false;
		}
		if (this.responseToOwnerConnector != other.isResponseToOwnerConnector()) {
			return false;
		}
		if (this.cacheEnabled != other.isCacheEnabled()) {
			return false;
		}
		if (this.cachePrivate != other.isCachePrivate()) {
			return false;
		}
		if (this.notificationConcurrent != other.isNotificationConcurrent()) {
			return false;
		}
		if (this.cacheTime != other.getCacheTime()) {
			return false;
		}
		if (this.timeout != other.getTimeout()) {
			return false;
		}
		if (this.validator != other.getValidator()) {
			return false;
		}
		if (this.securityEnabled != other.isSecurityEnabled()) {
			return false;
		}
		if (this.roles != other.getRoles() && (this.roles == null || !this.roles.equals(other.getRoles()))) {
			return false;
		}
		if (this.users != other.getRoles() && (this.users == null || !this.users.equals(other.getUsers()))) {
			return false;
		}
		if ((this.ns == null) ? (other.getNs() != null) : !this.ns.equals(other.getNs())) {
			return false;
		}
		if ((this.ipAddresses == null) ? (other.getIpAddresses() != null) : !this.ipAddresses.equals(other.getIpAddresses())) {
			return false;
		}
		return true;
	}

	/**
	 * @return The C2SEventDefinition identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The C2SEventDefinition identifier to set
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setId(String id) {
		this.id = id;

		return this;
	}

	/**
	 * @return The C2SEventDefinition validation rules for incoming arguments
	 */
	public Set<Argument> getIncomingArgsValidation() {
		return incomingArgsValidation;
	}

	/**
	 * @param incomingArgsValidation The C2SEventDefinition validation rules for incoming arguments to set
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setIncomingArgsValidation(Set<Argument> incomingArgsValidation) {
		this.incomingArgsValidation.addAll(incomingArgsValidation);

		return this;
	}

	/**
	 * @return The C2SEventDefinition validation rules for outgoing arguments
	 */
	public Set<Argument> getOutgoingArgsValidation() {
		return outgoingArgsValidation;
	}

	/**
	 * @param outgoingArgsValidation The C2SEventDefinition validation rules for outgoing arguments to set
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setOutgoingArgsValidation(Set<Argument> outgoingArgsValidation) {
		this.outgoingArgsValidation.addAll(outgoingArgsValidation);

		return this;
	}

	/**
	 * Indicate if a response is required for the target event
	 * 
	 * @return the responseRequired
	 */
	public boolean isResponseRequired() {
		return responseRequired;
	}

	/**
	 * @param responseRequired Indicate if a response is required for the target event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setResponseRequired(boolean responseRequired) {
		this.responseRequired = responseRequired;

		return this;
	}

	/**
	 * @return Indicate if the cache is enabled for this event
	 */
	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	/**
	 * 
	 * @return TRUE is the cache is private by user, FALSE otherwise
	 */
	public boolean isCachePrivate() {
		return cachePrivate;
	}

	/**
	 * 
	 * @param cachePrivate TRUE is the cache is private by user, FALSE otherwise
	 */
	public void setCachePrivate(boolean cachePrivate) {
		this.cachePrivate = cachePrivate;
	}

	/**
	 * @param cacheEnabled Indicate if the cache is enabled for this event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;

		return this;
	}

	/**
	 * @return The time in seconds to store in cache the response of this event
	 */
	public int getCacheTime() {
		return cacheTime;
	}

	/**
	 * @param cacheTime The time in seconds to store in cache the response of this event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;

		return this;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getRoles() {
		return roles;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	/**
	 * @return The class associated to this event
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Event> getEventClass() throws Exception {
		return (Class<? extends Event>) Class.forName(getNs());
	}

	/**
	 * @return The class name associated to the event
	 */
	public String getNs() {
		return ns;
	}

	/**
	 * @param ns The class name associated to the event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setNs(String ns) {
		this.ns = ns;

		return this;
	}

	/**
	 * Indicate if the response for the event need to be send to 
	 * the owner WebSocketConnector
	 * 
	 * @return <tt>TRUE</tt> if the response need to be send to the 
	 * owner WebSocketConnector, <tt>FALSE</tt> otherwise
	 */
	public boolean isResponseToOwnerConnector() {
		return responseToOwnerConnector;
	}

	/**
	 * @param responseToOwnerConnector Indicate if the response for the event need to be send to 
	 * the owner WebSocketConnector
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setResponseToOwnerConnector(boolean responseToOwnerConnector) {
		this.responseToOwnerConnector = responseToOwnerConnector;

		return this;
	}

	/**
	 * @return <tt>TRUE</tt> if the listeners notification can be do it 
	 * in threads, <tt>FALSE</tt> otherwise
	 */
	public boolean isNotificationConcurrent() {
		return notificationConcurrent;
	}

	/**
	 * @param notificationConcurrent Indicate if the listeners notification can be do it in threads
	 * @return C2SEventDefinition provide a fluent interface 
	 */
	public C2SEventDefinition setNotificationConcurrent(boolean notificationConcurrent) {
		this.notificationConcurrent = notificationConcurrent;

		return this;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getIpAddresses() {
		return ipAddresses;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setIpAddresses(Set<String> ipAddresses) {
		this.ipAddresses.addAll(ipAddresses);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getUsers() {
		return users;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setUsers(Set<String> users) {
		this.users.addAll(users);
	}

	/**
	 * @return <tt>TRUE</tt> if the response delivery to the client need to be
	 * asynchronous, <tt>FALSE</tt> otherwise 
	 */
	public boolean isResponseAsync() {
		return responseAsync;
	}

	/**
	 * @param responseAsync Indicate if the response delivery to the client need to be asynchronous
	 * @return C2SEventDefinition provide a fluent interface 
	 */
	public C2SEventDefinition setResponseAsync(boolean responseAsync) {
		this.responseAsync = responseAsync;

		return this;
	}

	/**
	 * @return The event validator
	 */
	public Validator getValidator() {
		return validator;
	}

	/**
	 * @param validator The event validator to set
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	/**
	 * @return The timeout for client requests (milliseconds)
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout The timeout for client requests (milliseconds)
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * 
	 * {@inheritDoc } 
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("type", getId());
		aToken.setBoolean("isCacheEnabled", isCacheEnabled());
		aToken.setBoolean("isCachePrivate", isCachePrivate());
		aToken.setBoolean("isSecurityEnabled", isSecurityEnabled());
		aToken.setInteger("cacheTime", getCacheTime());
		aToken.setInteger("timeout", getTimeout());

		FastList<String> lRoles = new FastList();
		for (String r : getRoles()) {
			lRoles.add(r);
		}
		aToken.setList("roles", lRoles);

		FastList<String> lUsers = new FastList();
		for (String u : getUsers()) {
			lUsers.add(u);
		}
		aToken.setList("users", lUsers);

		FastList<String> lIpAddresses = new FastList();
		for (String ip : getIpAddresses()) {
			lIpAddresses.add(ip);
		}
		aToken.setList("ip_addresses", lIpAddresses);

		FastList<Token> lIncomingArgs = new FastList();
		Token lArg;
		for (Argument lArgument : getIncomingArgsValidation()) {
			lArg = TokenFactory.createToken();
			lArg.setString("name", lArgument.getName());
			lArg.setString("type", lArgument.getType());
			lArg.setBoolean("optional", lArgument.isOptional());

			lIncomingArgs.add(lArg);
		}
		aToken.setList("incomingArgsValidation", lIncomingArgs);

		FastList<Token> lOutgoingArgs = new FastList();
		for (Argument a : getOutgoingArgsValidation()) {
			lArg = TokenFactory.createToken();
			lArg.setString("name", a.getName());
			lArg.setString("type", a.getType());
			lArg.setBoolean("optional", a.isOptional());

			lOutgoingArgs.add(lArg);
		}
		aToken.setList("outgoingArgsValidation", lOutgoingArgs);
	}
}
