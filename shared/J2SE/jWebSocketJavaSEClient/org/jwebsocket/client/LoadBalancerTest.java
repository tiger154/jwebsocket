//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Test (Community Edition, CE)
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
package org.jwebsocket.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.sample.SampleServicePlugIn;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.IsAlreadyConnectedException;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Rolando Betancourt Toucet
 */
public class LoadBalancerTest {

	private static int mCurrentServices = 4;
	private static int mCurrentConnections = 12;
	private static String mEndpointId = null;
	private static String mClusterAlias = null;

	/**
	 *
	 * @param args
	 * @throws IsAlreadyConnectedException
	 * @throws WebSocketException
	 */
	public static void main(String[] args) throws IsAlreadyConnectedException, WebSocketException {
		final List<BaseTokenClient> lServices = new ArrayList();
		final List<BaseTokenClient> lClients = new ArrayList();

		try {
			//Add services to cluster1
			for (int lPos = 0; lPos < mCurrentServices; lPos++) {
				final BaseTokenClient lClient = new BaseTokenClient();

				lClient.open("ws://localhost:8787/jWebSocket/jWebSocket");
				Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "registerServiceEndPoint");
				lToken.setString("clusterAlias", "service1");
				lClient.sendToken(lToken);

				SampleServicePlugIn lSamplePlugIn = new SampleServicePlugIn(lClient,
						"org.jwebsocket.plugins.sampleservice1");
				lServices.add(lClient);
				Thread.sleep(50);
			}

			//Add services to cluster2
			for (int lPos = 0; lPos < mCurrentServices; lPos++) {
				final BaseTokenClient lClient = new BaseTokenClient();

				lClient.open("ws://localhost:8787/jWebSocket/jWebSocket");
				Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "registerServiceEndPoint");
				lToken.setString("clusterAlias", "service2");
				lClient.sendToken(lToken);

				SampleServicePlugIn lSamplePlugIn = new SampleServicePlugIn(lClient,
						"org.jwebsocket.plugins.sampleservice2");
				lServices.add(lClient);
				Thread.sleep(50);
			}

			//Add connections
			for (int lPos = 0; lPos < mCurrentConnections; lPos++) {
				final BaseTokenClient lClient = new BaseTokenClient();
				lClient.addTokenClientListener(new WebSocketClientTokenListener() {
					@Override
					public void processToken(WebSocketClientEvent aEvent, Token aToken) {
						if (aToken.getType().equals("response")) {
							System.out.println(aToken.toString());
						}
					}

					@Override
					public void processOpening(WebSocketClientEvent aEvent) {
					}

					@Override
					public void processOpened(WebSocketClientEvent aEvent) {
					}

					@Override
					public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
					}

					@Override
					public void processClosed(WebSocketClientEvent aEvent) {
					}

					@Override
					public void processReconnecting(WebSocketClientEvent aEvent) {
					}
				});

				lClient.open("ws://localhost:8787/jWebSocket/jWebSocket");
				Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.sampleservice1", "echo");
				lToken.setString("data", "load balancer test");
				lClient.sendToken(lToken);

				lClients.add(lClient);
				Thread.sleep(50);
			}

			//Test getStickyRoutes, getClusterEndPointsInfo and deregisterServiceEndPoint
			final BaseTokenClient lClient = new BaseTokenClient();
			lClient.addTokenClientListener(new WebSocketClientTokenListener() {
				@Override
				public void processToken(WebSocketClientEvent aEvent, Token aToken) {
					System.out.println(aToken.toString());
					if (aToken.getString("reqType").equals("getStickyRoutes")) {
						List<Map<String, String>> lStickyRoutes = aToken.getList("routes");
						Map<String, String> lEntry = lStickyRoutes.get(0);
						mEndpointId = lStickyRoutes.get(0).get("serviceId");
						mClusterAlias = lStickyRoutes.get(0).get("clusterAlias");
					}
				}

				@Override
				public void processOpening(WebSocketClientEvent aEvent) {
				}

				@Override
				public void processOpened(WebSocketClientEvent aEvent) {
				}

				@Override
				public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
				}

				@Override
				public void processClosed(WebSocketClientEvent aEvent) {
				}

				@Override
				public void processReconnecting(WebSocketClientEvent aEvent) {
				}
			});

			lClient.open("ws://localhost:8787/jWebSocket/jWebSocket");
			Token lTokenSticky = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "getStickyRoutes");
			lClient.sendToken(lTokenSticky);
			Thread.sleep(50);

			Token lTokenInfo = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "getClusterEndPointsInfo");
			lClient.sendToken(lTokenInfo);
			Thread.sleep(50);

			Token lTokenDeregister = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "deregisterServiceEndPoint");
			lTokenDeregister.setString("epId", mEndpointId);
			lTokenDeregister.setString("clusterAlias", mClusterAlias);
			//lClient.sendToken(lTokenDeregister);
			Thread.sleep(50);

			lClient.sendToken(lTokenInfo);
			Thread.sleep(10000);

			Token lTokenShutdown = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "shutdownEndpoint");
			lTokenShutdown.setString("epId", mEndpointId);
			lTokenShutdown.setString("clusterAlias", mClusterAlias);
			System.out.println(mEndpointId);
			lClient.sendToken(lTokenShutdown);

		} catch (Exception lEx) {
		}
	}
}
