//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket IQuota  (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.api;

import java.util.List;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public interface IQuota {

	/**
	 *
	 * @return The quota type
	 */
	public String getType();

	/**
	 *
	 *
	 * @return
	 */
	public String getIdentifier();

	/**
	 * specifies the storage engine for this quota.
	 *
	 * @param aQuotaStorage
	 */
	public void setStorage(IQuotaStorage aQuotaStorage);

	/**
	 *
	 * @return
	 */
	public IQuotaStorage getStorage();

	/**
	 * Return the quota Object
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aInstanceType
	 * @param aActions
	 * @return
	 */
	public IQuotaSingleInstance getQuota(String aInstance, String aNameSpace, String aInstanceType, String aActions);

	/**
	 * Return the quota Object
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aInstanceType
	 * @return
	 */
	public List<IQuotaSingleInstance> getQuotas(String aInstance, String aNameSpace,
			String aInstanceType);

	/**
	 * return the default reduce value on the quota settings
	 *
	 * @return
	 */
	public long getDefaultReduceValue();

	/**
	 * return the parent quota object Instance. by their uuid
	 *
	 * @param aUuid
	 * @return
	 */
	public IQuotaSingleInstance getQuota(String aUuid);

	/**
	 * return an object Instance of one particular quota. depend of the given
	 * instance
	 *
	 * @param aUuid
	 * @param aInstance
	 * @return
	 */
	public IQuotaSingleInstance getQuota(String aUuid, String aInstance);

	/**
	 *
	 * tries to reduce the current quota by the given amount, if the quota still
	 * contains (i.e. allows) the request amount the result is the new quota
	 * value, otherwise the result is less than zero, indicating that the quota
	 * is exceeded by the returned amount.
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aInstanceType
	 * @param aAmount
	 * @param aActions
	 * @return
	 */
	public long reduceQuota(String aInstance, String aNameSpace,
			String aInstanceType, String aActions, long aAmount);

	/**
	 * This tries to reduce the current quota by the default reduce value given
	 * by the administrator
	 *
	 * @param aUuid
	 * @return
	 */
	public long reduceQuota(String aUuid);

	/**
	 * Reduce the quota value by a given amount
	 *
	 * @param aUuid
	 * @param aAmount
	 * @return
	 */
	public long reduceQuota(String aUuid, long aAmount);

	/**
	 * adds the given amount to the given quota, the result is the new quota
	 * value.
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aInstanceType
	 * @param aActions
	 * @param aAmount
	 * @return
	 */
	public long increaseQuota(String aInstance, String aNameSpace,
			String aInstanceType, String aActions, long aAmount);

	/**
	 * increase the quota value by a given amount
	 *
	 * @param aUuid
	 * @param aAmount
	 * @return
	 */
	public long increaseQuota(String aUuid, long aAmount);

	/**
	 * sets the given quota to the given value. The result is the old/previous
	 * quota value.
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aInstanceType
	 * @param aActions
	 * @param aAmount
	 * @return
	 */
	public long setQuota(String aInstance, String aNameSpace,
			String aInstanceType, String aActions, long aAmount);

	/**
	 * set the quota value by a given amount
	 *
	 * @param aUuid
	 * @param aAmount
	 * @return
	 */
	public long setQuota(String aUuid, long aAmount);

	/**
	 * registers an instance at the quota class, this is required to e.g. log
	 * volume in filter quotas or limit timed access.
	 *
	 * @param aInstance
	 * @param aUuid
	 * @param aInstanceType
	 * @throws java.lang.Exception
	 */
	public void register(String aUuid, String aInstance, String aInstanceType)
			throws Exception;

	/**
	 * the same as the above register method but an instance type could be
	 * specified
	 *
	 * aInstanceType atr could take the following values that make reference to
	 * an InstanceType:
	 *
	 * U > user : this is the default value taken for the above register method
	 * G > group A > app M > modulate
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aUuid
	 * @param aAmount
	 * @param aInstanceType
	 * @param aQuotaType
	 * @param aQuotaIdentifier
	 * @param aActions
	 * @throws java.lang.Exception
	 */
	public void create(String aInstance, String aNameSpace, String aUuid,
			long aAmount, String aInstanceType, String aQuotaType,
			String aQuotaIdentifier, String aActions) throws Exception;

	/**
	 * Return an String value with the tokens or actions for the quota
	 *
	 * @param aUuid
	 * @return
	 */
	public String getActions(String aUuid);

	/**
	 * unregisters an instance from the quota class, this is required to e.g.
	 * log volume or times of access.
	 *
	 * @param aInstance
	 * @param aUuid
	 * @throws
	 * org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound
	 */
	public void unregister(String aInstance, String aUuid)
			throws ExceptionQuotaNotFound;

	/**
	 * the same as the above register method but an instance type could be
	 * specified
	 *
	 * aInstanceType atr could take the following values that make reference to
	 * an InstanceType:
	 *
	 * U > user : this is the default value taken for the above register method
	 * G > group A > app M > modulate
	 *
	 * @param aInstance
	 * @param aNameSpace
	 * @param aInstanceType
	 * @param aActions
	 * @throws
	 * org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound
	 */
	public void unregister(String aInstance,
			String aNameSpace, String aInstanceType, String aActions)
			throws ExceptionQuotaNotFound;

	/**
	 * returns a list of registered instances (e.g. users) to this Quota class.
	 *
	 * @param aNamespace
	 * @param aId
	 * @return
	 */
	public List<String> getRegisteredInstances(String aNamespace, String aId);

	/**
	 * return and string list with all quota registed to a given namespace
	 *
	 * @param aNamespace
	 * @return
	 */
	public List<String> getRegisterdQuotas(String aNamespace);

	/**
	 * return the quota uuid for the given parrameters. All parameters are
	 * mandatory
	 *
	 * @param aQuotaIdentifier
	 * @param aNamespace
	 * @param aInstance
	 * @param aInstanceType
	 * @param aActions
	 * @return
	 */
	public String getQuotaUuid(String aQuotaIdentifier, String aNamespace, String aInstance,
			String aInstanceType, String aActions);
}
