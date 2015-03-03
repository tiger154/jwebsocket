//	---------------------------------------------------------------------------
//	jWebSocket - C2SEventDefinition (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event;

import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.eventmodel.api.IServerSecureComponent;
import org.jwebsocket.eventmodel.filter.validator.Argument;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.token.Token;
import org.springframework.validation.Validator;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class C2SEventDefinition implements IInitializable, IServerSecureComponent, ITokenizable {

	private String mId;
	private String mNs;
	private final Set<Argument> mIncomingArgsValidation = new FastSet<Argument>();
	private final Set<Argument> mOutgoingArgsValidation = new FastSet<Argument>();
	private boolean mResponseRequired = false;
	private boolean mResponseToOwnerConnector = true;
	private boolean mResponseAsync = false;
	private boolean mCacheEnabled = false;
	private boolean mCachePrivate = true;
	private boolean mNotificationConcurrent = false;
	private int mCacheTime = 0;
	private boolean mSecurityEnabled = false;
	private Set<String> mRoles = new FastSet<String>();
	private final Set<String> mUsers = new FastSet<String>();
	private final Set<String> mIpAddresses = new FastSet<String>();
	private Validator mValidator;
	private Integer mTimeout = 3000;

	/**
	 *
	 * @param mId
	 * @param mNs
	 */
	public C2SEventDefinition(String mId, String mNs) {
		this.mId = mId;
		this.mNs = mNs;
	}

	/**
	 *
	 */
	public C2SEventDefinition() {
	}

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
		return ((null != mId) ? mId.hashCode() : 0)
				+ mIncomingArgsValidation.hashCode()
				+ mOutgoingArgsValidation.hashCode()
				+ ((mResponseRequired) ? 1 : 0)
				+ ((mResponseToOwnerConnector) ? 1 : 0)
				+ ((mResponseAsync) ? 1 : 0)
				+ ((mCacheEnabled) ? 1 : 0)
				+ ((mCachePrivate) ? 1 : 0)
				+ ((mNotificationConcurrent) ? 1 : 0)
				+ mCacheTime
				+ ((null != mValidator) ? mValidator.hashCode() : 0)
				+ ((mSecurityEnabled) ? 1 : 0)
				+ mRoles.hashCode()
				+ mUsers.hashCode()
				+ ((null != mNs) ? mNs.hashCode() : 0)
				+ ((null != mIpAddresses) ? mIpAddresses.hashCode() : 0)
				+ mTimeout;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aObj
	 */
	@Override
	public boolean equals(Object aObj) {
		if (aObj == null) {
			return false;
		}
		if (getClass() != aObj.getClass()) {
			return false;
		}
		final C2SEventDefinition other = (C2SEventDefinition) aObj;
		if ((this.mId == null) ? (other.getId() != null) : !this.mId.equals(other.getId())) {
			return false;
		}
		if (this.mIncomingArgsValidation != other.getIncomingArgsValidation() && (this.mIncomingArgsValidation == null || !this.mIncomingArgsValidation.equals(other.getIncomingArgsValidation()))) {
			return false;
		}
		if (this.mOutgoingArgsValidation != other.getOutgoingArgsValidation() && (this.mOutgoingArgsValidation == null || !this.mOutgoingArgsValidation.equals(other.getOutgoingArgsValidation()))) {
			return false;
		}
		if (this.mResponseRequired != other.isResponseRequired()) {
			return false;
		}
		if (this.mResponseAsync != other.isResponseAsync()) {
			return false;
		}
		if (this.mResponseToOwnerConnector != other.isResponseToOwnerConnector()) {
			return false;
		}
		if (this.mCacheEnabled != other.isCacheEnabled()) {
			return false;
		}
		if (this.mCachePrivate != other.isCachePrivate()) {
			return false;
		}
		if (this.mNotificationConcurrent != other.isNotificationConcurrent()) {
			return false;
		}
		if (this.mCacheTime != other.getCacheTime()) {
			return false;
		}
		if (this.mTimeout != other.getTimeout()) {
			return false;
		}
		if (this.mValidator != other.getValidator()) {
			return false;
		}
		if (this.mSecurityEnabled != other.isSecurityEnabled()) {
			return false;
		}
		if (this.mRoles != other.getRoles() && (this.mRoles == null || !this.mRoles.equals(other.getRoles()))) {
			return false;
		}
		if (this.mUsers != other.getRoles() && (this.mUsers == null || !this.mUsers.equals(other.getUsers()))) {
			return false;
		}
		if ((this.mNs == null) ? (other.getNs() != null) : !this.mNs.equals(other.getNs())) {
			return false;
		}
		if ((this.mIpAddresses == null) ? (other.getIpAddresses() != null) : !this.mIpAddresses.equals(other.getIpAddresses())) {
			return false;
		}
		return true;
	}

	/**
	 * @return The C2SEventDefinition identifier
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param aId The C2SEventDefinition identifier to set
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setId(String aId) {
		this.mId = aId;

		return this;
	}

	/**
	 * @return The C2SEventDefinition validation rules for incoming arguments
	 */
	public Set<Argument> getIncomingArgsValidation() {
		return mIncomingArgsValidation;
	}

	/**
	 * @param aIncomingArgsValidation
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setIncomingArgsValidation(Set<Argument> aIncomingArgsValidation) {
		this.mIncomingArgsValidation.addAll(aIncomingArgsValidation);

		return this;
	}

	/**
	 * @return The C2SEventDefinition validation rules for outgoing arguments
	 */
	public Set<Argument> getOutgoingArgsValidation() {
		return mOutgoingArgsValidation;
	}

	/**
	 * @param aOutgoingArgsValidation The C2SEventDefinition validation rules
	 * for outgoing arguments to set
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setOutgoingArgsValidation(Set<Argument> aOutgoingArgsValidation) {
		this.mOutgoingArgsValidation.addAll(aOutgoingArgsValidation);

		return this;
	}

	/**
	 * Indicate if a response is required for the target event
	 *
	 * @return the responseRequired
	 */
	public boolean isResponseRequired() {
		return mResponseRequired;
	}

	/**
	 * @param aResponseRequired Indicate if a response is required for the
	 * target event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setResponseRequired(boolean aResponseRequired) {
		this.mResponseRequired = aResponseRequired;

		return this;
	}

	/**
	 * @return Indicate if the cache is enabled for this event
	 */
	public boolean isCacheEnabled() {
		return mCacheEnabled;
	}

	/**
	 *
	 * @return TRUE is the cache is private by user, FALSE otherwise
	 */
	public boolean isCachePrivate() {
		return mCachePrivate;
	}

	/**
	 *
	 * @param aCachePrivate TRUE is the cache is private by user, FALSE
	 * otherwise
	 */
	public void setCachePrivate(boolean aCachePrivate) {
		this.mCachePrivate = aCachePrivate;
	}

	/**
	 * @param aCacheEnabled Indicate if the cache is enabled for this event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setCacheEnabled(boolean aCacheEnabled) {
		this.mCacheEnabled = aCacheEnabled;

		return this;
	}

	/**
	 * @return The time in seconds to store in cache the response of this event
	 */
	public int getCacheTime() {
		return mCacheTime;
	}

	/**
	 * @param aCacheTime The time in seconds to store in cache the response of
	 * this event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setCacheTime(int aCacheTime) {
		this.mCacheTime = aCacheTime;

		return this;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean isSecurityEnabled() {
		return mSecurityEnabled;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setSecurityEnabled(boolean aSecurityEnabled) {
		this.mSecurityEnabled = aSecurityEnabled;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getRoles() {
		return mRoles;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setRoles(Set<String> aRoles) {
		this.mRoles = aRoles;
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
		return mNs;
	}

	/**
	 * @param aNs The class name associated to the event
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setNs(String aNs) {
		this.mNs = aNs;

		return this;
	}

	/**
	 * Indicate if the response for the event need to be send to the owner
	 * WebSocketConnector
	 *
	 * @return <tt>TRUE</tt> if the response need to be send to the owner
	 * WebSocketConnector, <tt>FALSE</tt> otherwise
	 */
	public boolean isResponseToOwnerConnector() {
		return mResponseToOwnerConnector;
	}

	/**
	 * @param aResponseToOwnerConnector Indicate if the response for the event
	 * need to be send to the owner WebSocketConnector
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setResponseToOwnerConnector(boolean aResponseToOwnerConnector) {
		this.mResponseToOwnerConnector = aResponseToOwnerConnector;

		return this;
	}

	/**
	 * @return <tt>TRUE</tt> if the listeners notification can be do it in
	 * threads, <tt>FALSE</tt> otherwise
	 */
	public boolean isNotificationConcurrent() {
		return mNotificationConcurrent;
	}

	/**
	 * @param aNotificationConcurrent Indicate if the listeners notification can
	 * be do it in threads
	 * @return C2SEventDefinition provide a fluent interface
	 */
	public C2SEventDefinition setNotificationConcurrent(boolean aNotificationConcurrent) {
		this.mNotificationConcurrent = aNotificationConcurrent;

		return this;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getIpAddresses() {
		return mIpAddresses;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setIpAddresses(Set<String> aIpAddresses) {
		this.mIpAddresses.addAll(aIpAddresses);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Set<String> getUsers() {
		return mUsers;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setUsers(Set<String> aUsers) {
		this.mUsers.addAll(aUsers);
	}

	/**
	 * @return <tt>TRUE</tt> if the response delivery to the client need to be
	 * asynchronous, <tt>FALSE</tt> otherwise
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean isResponseAsync() {
		return mResponseAsync;
	}

	/**
	 * @param aResponseAsync Indicate if the response delivery to the client
	 * need to be asynchronous
	 * @return C2SEventDefinition provide a fluent interface
	 *
	 * @deprecated
	 */
	@Deprecated
	public C2SEventDefinition setResponseAsync(boolean aResponseAsync) {
		this.mResponseAsync = aResponseAsync;

		return this;
	}

	/**
	 * @return The event validator
	 */
	public Validator getValidator() {
		return mValidator;
	}

	/**
	 * @param aValidator The event validator to set
	 */
	public void setValidator(Validator aValidator) {
		this.mValidator = aValidator;
	}

	/**
	 * @return The timeout for client requests (milliseconds)
	 */
	public Integer getTimeout() {
		return mTimeout;
	}

	/**
	 * @param aTimeout The timeout for client requests (milliseconds)
	 */
	public void setTimeout(Integer aTimeout) {
		this.mTimeout = aTimeout;
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

		FastList<String> lRoles = new FastList<String>();
		for (String r : getRoles()) {
			lRoles.add(r);
		}
		aToken.setList("roles", lRoles);

		FastList<String> lUsers = new FastList<String>();
		for (String u : getUsers()) {
			lUsers.add(u);
		}
		aToken.setList("users", lUsers);

		FastList<String> lIpAddresses = new FastList<String>();
		for (String ip : getIpAddresses()) {
			lIpAddresses.add(ip);
		}
		aToken.setList("ip_addresses", lIpAddresses);

		FastList<Map<String,Object>> lIncomingArgs = new FastList<Map<String,Object>>();
		Map<String,Object> lArg;
		for (Argument lArgument : getIncomingArgsValidation()) {
			lArg = new FastMap<String,Object>();
			lArg.put("name", lArgument.getName());
			lArg.put("type", lArgument.getType());
			lArg.put("optional", lArgument.isOptional());

			lIncomingArgs.add(lArg);
		}
		aToken.setList("incomingArgsValidation", lIncomingArgs);

		FastList<Map<String,Object>> lOutgoingArgs = new FastList<Map<String,Object>>();
		for (Argument a : getOutgoingArgsValidation()) {
			lArg = new FastMap<String,Object>();
			lArg.put("name", a.getName());
			lArg.put("type", a.getType());
			lArg.put("optional", a.isOptional());

			lOutgoingArgs.add(lArg);
		}
		aToken.setList("outgoingArgsValidation", lOutgoingArgs);
	}
}
