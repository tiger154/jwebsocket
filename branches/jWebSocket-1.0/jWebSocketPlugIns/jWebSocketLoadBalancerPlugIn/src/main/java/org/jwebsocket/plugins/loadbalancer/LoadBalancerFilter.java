//      ---------------------------------------------------------------------------
//	jWebSocket Load Balancer Filter (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 *
 * @author rbetancourt
 */
public class LoadBalancerFilter extends TokenFilter {

	private static Logger mLog = Logging.getLogger();
	private String LOADBALANCER_ID = null;
	private LoadBalancerPlugIn mLoadBalancerPlugIn;

	/**
	 *
	 * @param aConfiguration
	 */
	public LoadBalancerFilter(FilterConfiguration aConfiguration) {
		super(aConfiguration);
		LOADBALANCER_ID = getFilterConfiguration().getSettings().get("load_balancer_id");
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating load balancer filter...");
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	@Override
	public void processTokenIn(FilterResponse aResponse,
		WebSocketConnector aConnector, Token aToken) {
		mLoadBalancerPlugIn = (mLoadBalancerPlugIn == null
			? (LoadBalancerPlugIn) getServer().getPlugInById(LOADBALANCER_ID) : mLoadBalancerPlugIn);

		if (mLoadBalancerPlugIn.supportsNamespace(aToken.getNS())) {
			mLoadBalancerPlugIn.processToken(new PlugInResponse(), aConnector, aToken);
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	@Override
	public void processTokenOut(FilterResponse aResponse,
		WebSocketConnector aSource, WebSocketConnector aTarget,
		Token aToken) {
	}
}
