/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.api;

import java.util.Map;

/**
 *
 * @author osvaldo
 */
public interface IQuotaProvider {

	/**
	 *
	 * @param aType
	 * @return
	 * @throws Exception
	 */
	public IQuota getQuotaByIdentifier(String aType) throws Exception;

	/**
	 *
	 * @return
	 */
	public Map<String, IQuota> getActiveQuotas();

	/**
	 *
	 * @return
	 */
	public Map<String, IQuotaStorage> getActiveStorages();

	/**
	 *
	 * @param aPos
	 * @return
	 */
	public String getIdentifier(int aPos);
}
