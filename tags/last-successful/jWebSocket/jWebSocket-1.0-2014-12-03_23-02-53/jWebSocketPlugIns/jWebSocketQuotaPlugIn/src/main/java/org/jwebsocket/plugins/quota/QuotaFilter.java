//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Filter (Community Edition, CE)
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
package org.jwebsocket.plugins.quota;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.utils.QuotaCacheManager;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.QuotaProvider;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaFilter extends TokenFilter {

	private static final Logger mLog = Logging.getLogger();
	private IQuotaProvider mQuotaProvider;
	private QuotaCacheManager mCache;

	/**
	 *
	 * @param configuration
	 */
	public QuotaFilter(FilterConfiguration configuration) {
		super(configuration);

		if (mLog.isInfoEnabled()) {
			mLog.info("Quota Filter successfully instantiated.");
		}
	}

	@Override
	public void systemStarted() throws Exception {
		super.systemStarted();

		mQuotaProvider = (QuotaProvider) JWebSocketBeanFactory.getInstance().getBean("quotaProvider");
		mCache = (QuotaCacheManager) JWebSocketBeanFactory.getInstance().getBean("quotaCacheManager");
	}

	@Override
	public void processTokenIn(FilterResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		super.processTokenIn(aResponse, aConnector, aToken);

		String lNS = aToken.getNS();
		String lType = aToken.getType();

		//Only proccess when the token not come for the SystemPlugin
		if ((!SystemPlugIn.NS_SYSTEM.equals(lNS))) {

			String lUsername = aConnector.getUsername();
			if (isIgnoredUser(lUsername)) {
				return;
			}

			//Getting all active quota types
			Map<String, IQuota> lQuotas = mQuotaProvider.getActiveQuotas();

			for (Map.Entry<String, IQuota> entry : lQuotas.entrySet()) {

				IQuota lQuotaObj = entry.getValue();
				List<IQuotaSingleInstance> lQuotaList;
				String lCacheItemId = lQuotaObj.getIdentifier()
						+ "_" + lNS + "_" + lUsername;

				if (mCache.checkForAvailableQuota(lCacheItemId)) {
					/**
					 * The getQuotas method return a list with all quotas for
					 * this user for this namespace in one single query.
					 */
					lQuotaList = lQuotaObj.getQuotas(lUsername, lNS, "User");

					if (lQuotaList.size() > 0) {
						mCache.add(lCacheItemId, true);
					} else {
						//if there is not quota for this namespace 
						mCache.add(lCacheItemId, false);
						continue;
					}
				} else {
					continue;
				}

				for (IQuotaSingleInstance lQSingle : lQuotaList) {

					String lActions = lQSingle.getActions();

                    //if the actual token or action is not limited by the quota 
					//pass to the other quotaType
					if (!lActions.equals("*")) {
						if (lActions.indexOf(lType) == -1) {
							continue;
						}
					}

					long lQValue = lQSingle.getvalue();

					if (lQValue <= 0) {
						Token lResponse = getServer().createResponse(aToken);
						lResponse.setCode(-1);
						lResponse.setString("msg", "Acces not allowed to " + lNS
								+ " due to quota limmitation exceed for user: "
								+ lUsername);
						getServer().sendToken(aConnector, lResponse);
						aResponse.rejectMessage();

						if (mLog.isDebugEnabled()) {
							mLog.debug("Quota(" + lQuotaObj.getType() + ") limit"
									+ " exceeded for user: " + lUsername
									+ ", on namespace:" + lNS + ". Access not allowed!");
						}

					}
				}
			}
		}
	}

	private boolean isIgnoredUser(String aUser) {
		if (QuotaHelper.ignoredUsers().indexOf(aUser) != -1) {
			return true;
		}
		return false;
	}
}
