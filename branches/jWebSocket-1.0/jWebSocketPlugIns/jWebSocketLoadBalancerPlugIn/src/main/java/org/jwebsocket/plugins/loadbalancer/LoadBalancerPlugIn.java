//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Plug-in (Community Edition, CE)
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

import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;
import org.jwebsocket.plugins.loadbalancer.api.IClusterManager;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Provides all functionalities to load balancer for create, remove and manage services endpoints.
 *
 * @author Alexander Schulze
 * @author Rolando Betancourt Toucet
 * @author Rolando Santamaria Maso
 */
public class LoadBalancerPlugIn extends ActionPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_LOADBALANCER = JWebSocketServerConstants.NS_BASE + ".plugins.loadbalancer";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket LoadBalancerPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket Load Balancer Plug-in - Community Edition";
	/**
	 *
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 * Load balancer settings.
	 */
	protected Settings mSettings;
	/**
	 * Load balancer clusters.
	 */
	protected IClusterManager mClusterManager;
	/**
	 * Load balancer message delivery timeout.
	 */
	protected long mMessageDeliveryTimeout;

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public LoadBalancerPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Load Balancer plug-in...");
		}
		// specify default name space for load balancer plugin.
		this.setNamespace(NS_LOADBALANCER);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for load "
						+ "balancer plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.loadbalancer.settings");
				if (null != mSettings) {
					mClusterManager = mSettings.getClusterManager();
					mMessageDeliveryTimeout = mSettings.getMessageTimeout();

					if (mLog.isInfoEnabled()) {
						mLog.info("Load balancer plug-in successfully instantiated.");
					}
				} else {
					mLog.error("Don't was loaded settings correctly");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"instantiating load balancer plug-in"));
			throw lEx;
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_LOADBALANCER;
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		Iterator<ICluster> lClusters = mClusterManager.getClusters();
		while (lClusters.hasNext()) {
			ICluster lC = lClusters.next();
			String lGrantedEndPoints = lC.getGrantedEndPoints();
			if (null != lGrantedEndPoints) {
				String[] lEndPoints = lGrantedEndPoints.split(",");
				for (String lGrantedId : lEndPoints) {
					// automatically registering service endpoint
					if (aConnector.getId().equals(lGrantedId)) {
						lC.registerEndPoint(aConnector);
					}

					if (mLog.isDebugEnabled()) {
						mLog.info("Granted EndPoint('" + lGrantedId + "') has been automatically "
								+ "registered as service on cluster: " + lC.getAlias());
					}
				}
			}
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		int lRemoved = mClusterManager.removeConnectorEndPoints(aConnector.getId());

		if (mLog.isDebugEnabled()) {
			mLog.debug(lRemoved + " services where removed due to client '" + aConnector.getId()
					+ "' disconnection. Reason: " + aCloseReason);
		}
	}

	@Override
	public void processLogoff(WebSocketConnector aConnector) {
		int lRemoved = mClusterManager.removeConnectorEndPoints(aConnector.getId());

		if (mLog.isDebugEnabled()) {
			mLog.debug(lRemoved + " services where removed due to client '" + aConnector.getId()
					+ "' logoff!");
		}
	}

	/**
	 * Sends a list (of maps) with the in-formation about all clusters (e.g. per cluster:
	 * cluster-alias, number of end-points, list of endpoints in this cluster, status per endpoint
	 * etc.)
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".clustersInfo")
	public void clustersInfoAction(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);
		lResponse.setList("data", mClusterManager.getClustersInfo());

		sendToken(aConnector, lResponse);
	}

	/**
	 * Sends a list of all sticky routes managed by the load balancer, consisting of cluster-alias,
	 * client endpoint-id, service endpoint-id.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".clustersInfo")
	public void stickyRoutesAction(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);
		lResponse.setList("data", mClusterManager.getStickyRoutes());

		sendToken(aConnector, lResponse);
	}

	/**
	 * Registers a new service endpoint which is not yet specified in the load balancer
	 * configuration file.In case an endpoint id is passed which already exists in the load balancer
	 * configuration the request is rejected. In case a valid new endpoint is to be registered the
	 * internal table gets appended accordingly.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void registerServiceEndPointAction(WebSocketConnector aConnector, Token aToken) {
		String lAlias = aToken.getString("clusterAlias");
		String lPassword = aToken.getString("password");

		ICluster lCluster = mClusterManager.getClusterByAlias(lAlias);
		Assert.notNull(lCluster, "The target cluster does not exists!");

		// checking password.
		if (null != lCluster.getPassword()) {
			Assert.isTrue(lCluster.getPassword().equals(Tools.getMD5(lPassword)), "Password is invalid!");
		}

		Token lResponse = createResponse(aToken);
		IClusterEndPoint lEndPoint = lCluster.registerEndPoint(aConnector);
		Assert.notNull(lEndPoint, "The new endpoint can't be created");
		lResponse.setString("endPointId", lEndPoint.getEndPointId());

		sendToken(aConnector, lResponse);
	}

	/**
	 * De-registers a connected service endpoint. In case the endpoint is part of the load balancer
	 * configuration the internal entry in the table of end-points gets tagged as "de-registered".
	 * In case the endpoint was a dynamically added one the item in the internal table shall be
	 * removed.This method can be also used by an administration too, to restart a certain endpoint
	 * after an update.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void deregisterServiceEndPointAction(WebSocketConnector aConnector, Token aToken) {
		String lEndPointId = aToken.getString("endPointId");
		String lAlias = aToken.getString("clusterAlias");
		String lPassword = aToken.getString("password");

		// checking arguments
		Assert.notNull(lAlias, "The argument 'clusterAlias' cannot be null!");
		Assert.notNull(lEndPointId, "The argument 'endPointId' cannot be null!");

		ICluster lCluster = mClusterManager.getClusterByAlias(lAlias);
		Assert.notNull(lCluster, "The target cluster does not exists!");

		// checking password
		if (null != lCluster.getPassword()) {
			Assert.isTrue(lCluster.getPassword().equals(Tools.getMD5(lPassword)), "Password is invalid!");
		}

		IClusterEndPoint lClusterEndPoint = lCluster.getEndPoint(lEndPointId);

		Assert.notNull(lClusterEndPoint, "The target endpoint does not exists!");
		Assert.isTrue(lClusterEndPoint.getStatus().equals(EndPointStatus.ONLINE),
				"The target endpoint is not ONLINE and can't be deregistered!");

		Token lEvent = TokenFactory.createToken(NS_LOADBALANCER, "event");
		lEvent.setString("user", aConnector.getUsername());
		lEvent.setString("name", "serviceEndPointDeregistered");
		lEvent.setString("endPointId", lEndPointId);

		// sending event to endpoint connector.
		sendToken(getConnector(lClusterEndPoint.getConnectorId()), lEvent);

		// removing endpoint.
		lCluster.removeEndPoint(lClusterEndPoint);

		// acknowledge for the requester.
		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Should send a message to the referenced endpoint to gracefully shutdown. A graceful shutdown
	 * should include a clean de-registering from the load balancer plug-in. Since it can be not
	 * guaranteed that the target endpoint processes the shutdown request properly, this method
	 * shall come with a timeout, such as when this is exceeded the endpoint is automatically
	 * registered to be shut down manually by the administrator.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".shutdownEndPoint")
	public void shutdownServiceEndPointAction(final WebSocketConnector aConnector, final Token aToken) {
		String lEndPointId = aToken.getString("endPointId");
		String lAlias = aToken.getString("clusterAlias");
		String lPassword = aToken.getString("password");

		// checking arguments.
		Assert.notNull(lAlias, "The argument 'clusterAlias' cannot be null!");
		Assert.notNull(lEndPointId, "The argument 'endPointId' cannot be null!");

		ICluster lCluster = mClusterManager.getClusterByAlias(lAlias);
		Assert.notNull(lCluster, "The target cluster does not exists!");

		// checking password
		if (null != lCluster.getPassword()) {
			Assert.isTrue(lCluster.getPassword().equals(Tools.getMD5(lPassword)), "The given password is invalid!");
		}

		final IClusterEndPoint lClusterEndPoint = lCluster.getEndPoint(lEndPointId);

		Assert.notNull(lClusterEndPoint, "The target endpoint does not exists!");
		Assert.isTrue(lClusterEndPoint.getStatus().equals(EndPointStatus.ONLINE),
				"The target endpoint is not ONLINE and can't be shutdown!");

		Token lResponse = createResponse(aToken);
		lResponse.setString("endPointId", lEndPointId);

		// acknowledge for the requester.
		sendToken(aConnector, lResponse);

		// change endpoint status
		lClusterEndPoint.setStatus(EndPointStatus.SHUTTING_DOWN);

		// sending service shutdown event to endpoint connector
		Token lEvent = TokenFactory.createToken(NS_LOADBALANCER, "event");
		lEvent.setString("name", "shutdownServiceEndPoint");
		lEvent.setString("endPointId", lClusterEndPoint.getEndPointId());
		lEvent.setString("user", aConnector.getUsername());
		sendToken(getConnector(lClusterEndPoint.getConnectorId()), lEvent);

		// removing the endpoint
		lClusterEndPoint.setStatus(EndPointStatus.OFFLINE);
		lCluster.removeEndPoint(lClusterEndPoint);
	}

	/**
	 * Changes the type of algorithm used by the load balancer.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".changeBalancerAlgorithm")
	public void changeAlgorithmAction(WebSocketConnector aConnector, Token aToken) {
		Integer lAlgorithm = aToken.getInteger("algorithm");

		// checking arguments.
		Assert.notNull(lAlgorithm, "The argument 'algorithm' cannot be null!");
		Assert.isTrue((lAlgorithm > 0 && lAlgorithm < 4), "The argument 'algorithm' only must be (1, 2 or 3)!");

		// set the current algorithm.
		mClusterManager.setBalancerAlgorithm(lAlgorithm);

		String lAlgorithmName = "Round Robin [1]";

		switch (lAlgorithm) {
			case 1:
				lAlgorithmName = "Round Robin [1]";
				break;
			case 2:
				lAlgorithmName = "Least CPU Usage [2]";
				break;
			case 3:
				lAlgorithmName = "Optimum Balance [3]";
				break;
		}

		Token lResponse = createResponse(aToken);
		lResponse.setString("currentAlgorithm", lAlgorithmName);

		sendToken(aConnector, lResponse);
	}

	/**
	 * Sends client requests to the appropriate service. If occurs any error or timeout, repeats the
	 * operation.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void sendToService(final WebSocketConnector aConnector, final Token aToken) {
		aToken.setString("sourceId", aConnector.getId());
		String lNS = aToken.getNS();

		final IClusterEndPoint lEndPoint = mClusterManager.getOptimumServiceEndPoint(lNS);

		if (null != lEndPoint) {
			final WebSocketConnector lConnector = getConnector(lEndPoint.getConnectorId());

			if (lConnector.supportsTransactions()) {
				sendTokenInTransaction(lConnector, aToken, new IPacketDeliveryListener() {

					@Override
					public long getTimeout() {
						return mMessageDeliveryTimeout;
					}

					@Override
					public void OnTimeout() {
						// deregister target connector services because a possible 
						// connection bottleneck or node shutdown.
						int lDeregistered = mClusterManager.removeConnectorEndPoints(aConnector.getId());

						if (mLog.isDebugEnabled()) {
							mLog.debug("Remote client not received a message on required '"
									+ mMessageDeliveryTimeout + "'time! " + lDeregistered
									+ " client services were deregistered!");
						}

						// call again
						sendToService(aConnector, aToken);
					}

					@Override
					public void OnSuccess() {
						lEndPoint.increaseRequests();
					}

					@Override
					public void OnFailure(Exception lEx) {
						Token lToken = createResponse(aToken);
						lToken.setCode(-1);
						lToken.setString("msg", lEx.getMessage());
					}
				});
			} else {
				sendToken(lConnector, aToken);
				lEndPoint.increaseRequests();
			}
		} else {
			sendErrorToken(aConnector, aToken, -1, "No service available on cluster to process the resquest!");
		}
	}

	/**
	 * Sends response to appropriate remote client.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void responseAction(WebSocketConnector aConnector, Token aToken) {
		String lSourceId = aToken.getString("sourceId");
		if (mSettings.isIncludeWorkerServiceId()) {
			aToken.setString("workerServiceId", aConnector.getId());
		}

		sendToken(getConnector(lSourceId), aToken);
	}

	/**
	 * Update CPU usage to a determined cluster endpoint.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void updateCpuUsageAction(WebSocketConnector aConnector, Token aToken) {
		mClusterManager.updateCpuUsage(aConnector.getId(), aToken.getDouble("usage"));
	}

	/**
	 * The method is required for the Load Balancer Filter.
	 *
	 * @param aNS
	 * @return
	 */
	public boolean isNamespaceSupported(String aNS) {
		return mClusterManager.isNamespaceSupported(aNS);
	}

	boolean isAliasSupported(String aAliasName) {
		return mClusterManager.getClusterByAlias(aAliasName) != null;
	}
}
