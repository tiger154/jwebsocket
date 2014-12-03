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
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Provides all functionalities to load balancer for create, remove and manage
 * services endpoints.
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
	protected Map<String, Cluster> mClusters;
	/**
	 * Clusters name space/alias.
	 */
	private Map<String, String> mNamespaceToService;
	/**
	 * Load balancer message delivery timeout.
	 */
	protected long mMessageDeliveryTimeout;
	/**
	 * Load balancer algorithm.
	 */
	private int mBalancerAlgorithm;
	/**
	 * Delay connectors queue for to be stopped.
	 */
	private List<WebSocketConnector> mDelayConnectorsQueue;
	/**
	 * Delay time for stop the connector when the client can't stop it.
	 */
	private long mConnectorStopDelay;

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
					mClusters = mSettings.getClusters();
					mMessageDeliveryTimeout = mSettings.getMessageTimeout();
					mConnectorStopDelay = mSettings.getConnectorStopDelay();
					mBalancerAlgorithm = mSettings.getBalancerAlgorithm();
					buildMap();
					mDelayConnectorsQueue = new FastList<WebSocketConnector>();
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
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mDelayConnectorsQueue.contains(aConnector)) {
			mDelayConnectorsQueue.remove(aConnector);
		}

		int lRemoved = removeConnector(aConnector);

		if (mLog.isDebugEnabled()) {
			mLog.debug(lRemoved + " services where removed due to client '" + aConnector.getId()
					+ "' disconnection. Reason: " + aCloseReason);
		}
	}

	/**
	 * Sends a list (of maps) with the in-formation about all clusters (e.g. per
	 * cluster: cluster-alias, number of end-points, list of endpoints in this
	 * cluster, status per endpoint etc.)
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".clustersInfo")
	public void clustersInfoAction(WebSocketConnector aConnector, Token aToken) {
		List<Map<String, Object>> lInfo = new FastList<Map<String, Object>>();
		for (Map.Entry<String, Cluster> lEntry : mClusters.entrySet()) {
			lInfo.add(lEntry.getValue().getInfo(lEntry.getKey()));
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("data", lInfo);
		sendToken(aConnector, lResponse);
	}

	/**
	 * Sends a list of all sticky routes man-aged by the load balancer,
	 * consisting of cluster-alias, client endpoint-id, service endpoint-id.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".clustersInfo")
	public void stickyRoutesAction(WebSocketConnector aConnector, Token aToken) {
		List<Map<String, String>> lStickyRoutes = new FastList<Map<String, String>>();
		for (Map.Entry<String, Cluster> lEntry : mClusters.entrySet()) {
			lEntry.getValue().getStickyRoutes(lEntry.getKey(), lStickyRoutes);
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("data", lStickyRoutes);

		sendToken(aConnector, lResponse);
	}

	/**
	 * Registers a new service endpoint which is not yet specified in the load
	 * balancer configuration file.In case an endpoint id is passed which
	 * already exists in the load balancer configuration the request is
	 * rejected. In case a valid new endpoint is to be registered the internal
	 * table gets appended accordingly.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void registerServiceEndPointAction(WebSocketConnector aConnector, Token aToken) {
		String lClusterAlias = aToken.getString("clusterAlias");
		String lPassword = aToken.getString("password");

		Assert.isTrue(mClusters.containsKey(lClusterAlias), "The target cluster does not exists!");

		Cluster lCluster = getClusterByAlias(lClusterAlias);
		// checking password.
		if (null != lCluster.getPassword()) {
			Assert.isTrue(lCluster.getPassword().equals(lPassword), "Password is invalid!");
		}

		Token lResponse = createResponse(aToken);
		ClusterEndPoint lEndPoint = lCluster.registerEndPoint(aConnector);
		Assert.notNull(lEndPoint, "The new endpoint can't be created");
		lResponse.setString("endPointId", lEndPoint.getServiceId());

		sendToken(aConnector, lResponse);
	}

	/**
	 * De-registers an connected service endpoint. In case the endpoint is part
	 * of the load balancer configuration the internal entry in the table of
	 * end-points gets tagged as "de-registered". In case the endpoint was a
	 * dynamically added one the item in the internal table shall be
	 * removed.This method can be also used by an administration too to restart
	 * a certain endpoint after an update.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void deregisterServiceEndPointAction(WebSocketConnector aConnector, Token aToken) {
		String lEndPointId = aToken.getString("endPointId");
		String lClusterAlias = aToken.getString("clusterAlias");
		String lPassword = aToken.getString("password");

		// checking arguments.
		Assert.notNull(lClusterAlias, "The argument 'clusterAlias' cannot be null!");
		Assert.notNull(lEndPointId, "The argument 'endPointId' cannot be null!");
		Assert.isTrue(mClusters.containsKey(lClusterAlias), "The target cluster does not exists!");

		final Cluster lCluster = getClusterByAlias(lClusterAlias);
		// checking password.
		if (null != lCluster.getPassword()) {
			Assert.isTrue(lCluster.getPassword().equals(lPassword), "Password is invalid!");
		}

		ClusterEndPoint lClusterEndPoint = lCluster.containsEndPoint(lEndPointId);

		Assert.notNull(lClusterEndPoint, "The target endpoint does not exists!");
		Assert.isTrue(lClusterEndPoint.getStatus().equals(EndPointStatus.ONLINE),
				"The target endpoint is not ONLINE and can't be deregistered!");

		Token lEvent = TokenFactory.createToken(NS_LOADBALANCER, "event");
		lEvent.setString("user", aConnector.getUsername());
		lEvent.setString("name", "serviceEndPointDeregistered");
		lEvent.setString("endPointId", lEndPointId);

		// sending event to endpoint connector.
		sendToken(lClusterEndPoint.getConnector(), lEvent);

		// removing endpoint.
		lCluster.removeEndPoint(lClusterEndPoint);

		// acknowledge for the requester.
		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Should send a message to the referenced endpoint to gracefully shutdown.
	 * A graceful shutdown should include a clean de-registering from the load
	 * balancer plug-in. Since it can be not guaranteed that the target endpoint
	 * processes the shutdown request properly, this method shall come with a
	 * timeout, such as when this is exceeded the endpoint is automatically
	 * registered to be shut down manually by the administrator.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".shutdownEndPoint")
	public void shutdownServiceEndPointAction(final WebSocketConnector aConnector, final Token aToken) {
		String lEndPointId = aToken.getString("endPointId");
		String lClusterAlias = aToken.getString("clusterAlias");
		String lPassword = aToken.getString("password");

		// checking arguments.
		Assert.notNull(lClusterAlias, "The argument 'clusterAlias' cannot be null!");
		Assert.notNull(lEndPointId, "The argument 'endPointId' cannot be null!");
		Assert.isTrue(mClusters.containsKey(lClusterAlias), "The target cluster does not exists!");

		final Cluster lCluster = getClusterByAlias(lClusterAlias);
		// checking password
		if (null != lCluster.getPassword()) {
			Assert.isTrue(lCluster.getPassword().equals(lPassword), "Password is invalid!");
		}

		final ClusterEndPoint lClusterEndPoint = lCluster.containsEndPoint(lEndPointId);

		Assert.notNull(lClusterEndPoint, "The target endpoint does not exists!");
		Assert.isTrue(lClusterEndPoint.getStatus().equals(EndPointStatus.ONLINE),
				"The target endpoint is not ONLINE and can't be shutdown!");

		Token lResponse = createResponse(aToken);
		lResponse.setString("endPointId", lEndPointId);

		// acknowledge for the requester.
		sendToken(aConnector, lResponse);

		// change endpoint status
		lClusterEndPoint.setStatus(EndPointStatus.SHUTTING_DOWN);

		Token lShutdown = TokenFactory.createToken(NS_LOADBALANCER, "shutdown");
		lShutdown.setString("connectorId", lClusterEndPoint.getConnector().getId());

		Assert.isTrue(mDelayConnectorsQueue.add(lClusterEndPoint.getConnector()), "The connector can't be added to connectors queue!");

		// sending shutdown to endpoint connector.
		sendToken(lClusterEndPoint.getConnector(), lShutdown);

		// if the endpoint does not was stopped, then the connector Stopped is execute it with 10 seconds of delay.
		Tools.getTimer().schedule(new JWSTimerTask() {

			@Override
			public void runTask() {
				try {
					if (!mDelayConnectorsQueue.isEmpty()) {
						lClusterEndPoint.getConnector().stopConnector(CloseReason.TIMEOUT);
					}
				} catch (Exception lEx) {
					Logger.getLogger(LoadBalancerPlugIn.class.getName()).log(Priority.FATAL, lEx + " while the load balancer stopped the endpoint!");
				}
			}
		}, mConnectorStopDelay);
	}

	/**
	 * Changes the type of algorithm used by the load balancer.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	@Role(name = NS_LOADBALANCER + ".registerServiceEndPoint")
	public void changeAlgorithmAction(WebSocketConnector aConnector, Token aToken) {
		Integer lAlgorithm = aToken.getInteger("algorithm");

		// checking arguments.
		Assert.notNull(lAlgorithm, "The argument 'algorithm' cannot be null!");
		Assert.isTrue((lAlgorithm > 0 && lAlgorithm < 4), "The argument 'algorithm' only must be (1, 2 or 3)!");

		// set the current algorithm.
		setBalancerAlgorithm(lAlgorithm);

		String lAlgorithmName = "Round Robin [1]";

		switch (getBalancerAlgorithm()) {
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
	 * Sends client requests to the appropriate service. If occurs any error or
	 * timeout, repeats the operation.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void sendToService(final WebSocketConnector aConnector, final Token aToken) {
		aToken.setString("sourceId", aConnector.getId());
		String lNS = aToken.getNS();

		final ClusterEndPoint lEndPoint = getOptimumServiceEndPoint(lNS);

		if (null != lEndPoint) {
			final WebSocketConnector lConnector = lEndPoint.getConnector();

			sendTokenInTransaction(lConnector, aToken, new IPacketDeliveryListener() {

				@Override
				public long getTimeout() {
					return mMessageDeliveryTimeout;
				}

				@Override
				public void OnTimeout() {

                                        // deregister target connector services because a possible 
					// connection bottleneck or node shutdown.
					int lDeregistered = removeConnector(lConnector);

					if (mLog.isDebugEnabled()) {
						mLog.debug("Remote client not received a message on required '"
								+ mMessageDeliveryTimeout + "'time! " + lDeregistered
								+ " client services were deregistered!");
					}

					// call again.
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

		sendToken(getSourceConnector(lSourceId), aToken);
	}

	/**
	 * Update CPU usage to a determined cluster endpoint.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void updateCpuUsageAction(WebSocketConnector aConnector, Token aToken) {
		Iterator<Cluster> lCluster = mClusters.values().iterator();
		while (lCluster.hasNext()) {
			lCluster.next().refreshCpuUsage(aConnector.getId(), aToken.getDouble("usage"));
		}
	}

	/**
	 * @param aNamespace Incoming token's name space
	 * @return <code>true</code> if any cluster supports the incoming name space
	 * ; <code>false</code> otherwise
	 */
	public boolean supportsNamespace(String aNamespace) {
		Iterator<Cluster> lCluster = mClusters.values().iterator();
		while (lCluster.hasNext()) {
			if (lCluster.next().getNamespace().equals(aNamespace)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param mBalancerAlgorithm the balancer algorithm to set.
	 */
	public void setBalancerAlgorithm(int aBalancerAlgorithm) {
		this.mBalancerAlgorithm = aBalancerAlgorithm;
	}

	/**
	 * @return the balancer algorithm.
	 */
	public int getBalancerAlgorithm() {
		return mBalancerAlgorithm;
	}

	/**
	 * Gets a optimum cluster endpoint.
	 *
	 * @param aNamespace cluster name space.
	 * @return the optimum service.
	 */
	private ClusterEndPoint getOptimumServiceEndPoint(String aNamespace) {
		Cluster lCluster = getClusterByNamespace(aNamespace);
		if (lCluster.availableEndPoint()) {
			if (getAlgorithm() == 1) {
				return lCluster.getRoundRobinEndPoint();
			} else if (getAlgorithm() == 2) {
				return lCluster.getOptimumEndPoint();
			} else {
				return lCluster.getOptimumRREndPoint();
			}
		} else {
			return null;
		}
	}

	/**
	 * @return the current algorithm (1,2 or 3).
	 */
	private int getAlgorithm() {
		Assert.isTrue(getBalancerAlgorithm() > 0 && getBalancerAlgorithm() < 4,
				"'" + getBalancerAlgorithm() + "' : is not a valid algorithm type!");

		return getBalancerAlgorithm();
	}

	/**
	 * @param aAlias cluster alias.
	 * @return an specified cluster
	 */
	private Cluster getClusterByAlias(String aAlias) {
		return mClusters.get(aAlias);
	}

	/**
	 * @param aSourceId Source client connector
	 * @return an specified connector.
	 */
	private WebSocketConnector getSourceConnector(String aSourceId) {
		return getServer().getConnector(aSourceId);
	}

	/**
	 * @param aNamespace cluster name space.
	 * @return an specified cluster.
	 */
	private Cluster getClusterByNamespace(String aNamespace) {
		return mClusters.get(mNamespaceToService.get(aNamespace));
	}

	/**
	 * Build a map with the clusters name space and your alias.
	 */
	private void buildMap() {
		mNamespaceToService = new FastMap<String, String>();
		Iterator<String> lKeys = mClusters.keySet().iterator();
		while (lKeys.hasNext()) {
			String lKey = lKeys.next();
			mNamespaceToService.put(mClusters.get(lKey).getNamespace(), lKey);
		}
	}

	/**
	 * @param aConnector cluster endpoint connector.
	 * @return the amount of cluster endpoint removed.
	 */
	private int removeConnector(WebSocketConnector aConnector) {
		int lRemoved = 0;
		Iterator<Cluster> lCluster = mClusters.values().iterator();
		while (lCluster.hasNext()) {
			lRemoved += lCluster.next().removeEndPointsByConnector(aConnector);
		}

		return lRemoved;
	}
}
