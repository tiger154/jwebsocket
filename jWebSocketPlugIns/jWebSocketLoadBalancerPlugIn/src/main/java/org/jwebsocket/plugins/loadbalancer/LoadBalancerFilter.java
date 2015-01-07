//      ---------------------------------------------------------------------------
//	jWebSocket Load Balancer Filter (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.loadbalancer;

import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.token.Token;
import org.springframework.util.Assert;

/**
 * Load balancer filter captures the packets and forwards them to the load balancer plug-in.
 *
 * @author Rolando Betancourt Toucet
 * @author Rolando Santamaria Maso
 */
public class LoadBalancerFilter extends TokenFilter {

	private static final Logger mLog = Logging.getLogger();
	/**
	 * Load balancer plug-in id.
	 */
	private final String mLoadBalancerPlugInId;
	/**
	 * Load balancer plug-in instance.
	 */
	private LoadBalancerPlugIn mLoadBalancerPlugIn;

	/**
	 *
	 * @param aConfiguration
	 */
	public LoadBalancerFilter(FilterConfiguration aConfiguration) {
		super(aConfiguration);
		mLoadBalancerPlugInId = getFilterConfiguration().getSettings().get("loadbalancer_plugin_id");
		Assert.notNull(mLoadBalancerPlugInId, "Missing LoadBalancerFilter 'loadbalancer_plugin_id' "
				+ "setting!");
	}

	/**
	 * Automatically redirects incoming tokens to the load balancer plug-in.
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	@Override
	public void processTokenIn(FilterResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lTargetId = aToken.getString("targetId");
		String lAction = aToken.getString("action");
		if ("forward.json".equals(lAction) && null != lTargetId) {
			if (mLoadBalancerPlugIn.isAliasSupported(lTargetId)) {
				String lJson = aToken.getString("data");
				if (null != lJson) {
					aToken = JSONProcessor.JSONStringToToken(lJson);
				}
			}
		}

		ICluster lCluster = mLoadBalancerPlugIn.getClusterByNamespace(aToken.getNS());
		if (null != lCluster) {
			// redirect token
			if (null != aToken.getInteger("utid", null)) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Redirecting incoming token to the load balancer plug-in...");
				}
				mLoadBalancerPlugIn.sendToService(aConnector, aToken, lCluster);

				// stop token propagation
				aResponse.rejectMessage();
			}
		}
	}

	@Override
	public void systemStarted() throws Exception {
		mLoadBalancerPlugIn = (LoadBalancerPlugIn) getServer().getPlugInById(mLoadBalancerPlugInId);
		Assert.notNull(mLoadBalancerPlugIn, "Unable to start the LoadBalancerFilter because "
				+ "the LoadBalancer plug-in '" + mLoadBalancerPlugInId + "' was not found!");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Filter started successfully!");
		}
	}
}
