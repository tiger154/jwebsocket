//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Cluster (Community Edition, CE)
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;

/**
 * Manages the list of end points per cluster.
 *
 * @author Alexander Schulze
 * @author Rolando Betancourt Toucet
 * @author Rolando Santamaria Maso
 */
public class Cluster {

        /**
         * List of endpoints.
         */
        private List<ClusterEndPoint> mEndPoints = new FastList<ClusterEndPoint>();
        /**
         * Cluster name space.
         */
        private String mNamespace;
        /**
         * Password required for add a new cluster endpoint.
         */
        private String mPassword;
        /**
         * Cluster endpoint position used in load balancer algorithms.
         */
        private int mEndPointPosition = -1;

        /**
         * @return a list of endpoints.
         */
        public List<ClusterEndPoint> getEndPoints() {
                return mEndPoints;
        }

        /**
         * @param aEndPoints the list of endpoints to set.
         */
        public void setEndPoints(List<ClusterEndPoint> aEndPoints) {
                mEndPoints = aEndPoints;
        }

        /**
         * @return the cluster name space.
         */
        public String getNamespace() {
                return mNamespace;
        }

        /**
         * @param mNamespace the name space to set.
         */
        public void setNamespace(String aNamespace) {
                this.mNamespace = aNamespace;
        }

        /**
         * @return cluster password.
         */
        public String getPassword() {
                return mPassword;
        }

        /**
         * @param aPassword the password to set.
         */
        public void setPassword(String aPassword) {
                mPassword = aPassword;
        }

        /**
         * Registers a new cluster endpoint.
         *
         * @param aConnector cluster endpoint connector.
         * @return the cluster endpoint registered.
         */
        public ClusterEndPoint registerEndPoint(WebSocketConnector aConnector) {

                // creates a new cluster endpoint with the target connector. 
                ClusterEndPoint lEndPoint = new ClusterEndPoint(aConnector);
                return (mEndPoints.add(lEndPoint) ? lEndPoint : null);
        }

        /**
         * Removes the specific cluster endpoint from endpoints list.
         *
         * @param aClusterEndPoint endpoint to be removed.
         * @return the cluster endpoint removed.
         */
        public ClusterEndPoint removeEndPoint(ClusterEndPoint aClusterEndPoint) {
                int lEndPointPosition = mEndPoints.indexOf(aClusterEndPoint);

                // remove the cluster endpoint from the list.
                return mEndPoints.remove(lEndPointPosition);
        }

        /**
         * Removes a cluster endpoint by a specific connector.
         *
         * @param aConnector connector to find the correct cluster endpoint.
         * @return the count of cluster endpoints removed.
         */
        public int removeEndPointsByConnector(WebSocketConnector aConnector) {
                int lCount = 0;
                Iterator<ClusterEndPoint> lIt = mEndPoints.iterator();
                while (lIt.hasNext()) {
                        ClusterEndPoint lEndPoint = lIt.next();

                        // if the argument is equal to cluster endpoint connector, remove it.
                        if (aConnector.equals(lEndPoint.getConnector())) {
                                removeEndPoint(lEndPoint);
                                lCount++;
                        }
                }
                return lCount;
        }

        /**
         * Gets information about this cluster.
         *
         * @param aAlias alias from this cluster.
         * @return a map with information about this cluster.
         */
        public Map<String, Object> getInfo(String aAlias) {
                Map<String, Object> lInfoCluster = new HashMap<String, Object>();
                lInfoCluster.put("clusterAlias", aAlias);
                lInfoCluster.put("clusterNS", getNamespace());
                lInfoCluster.put("endPointsCount", getEndPoints().size());
                lInfoCluster.put("endPoints", getEndPoints());
                lInfoCluster.put("requests", getTotalEndPointsRequests());

                return lInfoCluster;
        }

        /**
         * Gets all sticky routes in this cluster. A sticky routes is a cluster
         * endpoint with status online.
         *
         * @param aAlias alias from this cluster.
         * @param aStickyRoutes sticky routes list.
         */
        public void getStickyRoutes(String aAlias, List<Map<String, String>> aStickyRoutes) {
                Map<String, String> lInfoCluster;
                List<String> lIDs = getStickyId();
                for (int lPos = 0; lPos < lIDs.size(); lPos++) {
                        lInfoCluster = new FastMap<String, String>();
                        lInfoCluster.put("clusterAlias", aAlias);
                        lInfoCluster.put("endPointId", lIDs.get(lPos));
                        aStickyRoutes.add(lInfoCluster);
                }
        }

        /**
         * Gets a balanced cluster endpoint using the round robin algorithm.
         *
         * @return optimum cluster endpoint or
         * <code>null</code> if endpoints list is empty.
         */
        public ClusterEndPoint getRoundRobinEndPoint() {

                // determine the cluster endpoint position to be returned. 
                mEndPointPosition = (mEndPointPosition + 1 < mEndPoints.size()
                        ? mEndPointPosition + 1 : 0);

                // if the cluster endpoint position is valid then return it,
                // but repeat this method.
                return (availableEndPoint(mEndPointPosition)
                        ? mEndPoints.get(mEndPointPosition) : getRoundRobinEndPoint());
        }

        /**
         * Gets a balanced cluster endpoint using the least CPU usage algorithm.
         *
         * @return optimum cluster endpoint.
         */
        public ClusterEndPoint getOptimumEndPoint() {
                double lLeastCpuUsage = Double.MAX_VALUE;
                int lEndPointPos = -1;
                for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
                        ClusterEndPoint lClusterEndPoint = mEndPoints.get(lPos);
                        if (lClusterEndPoint.getStatus().equals(EndPointStatus.ONLINE)) {
                                double lTempCpuUsage = lClusterEndPoint.getCpuUsage();
                                Object lJwsType = lClusterEndPoint.getConnector().getVar("jwsType");

                                // discard all java script clients because they can't get your cup usage.
                                if (!lJwsType.toString().equals("javascript") && lTempCpuUsage < lLeastCpuUsage) {
                                        lLeastCpuUsage = lTempCpuUsage;
                                        lEndPointPos = lPos;
                                }
                        }
                }
                return (lEndPointPos == -1 ? null : mEndPoints.get(lEndPointPos));
        }

        /**
         * Gets a balanced cluster endpoint using both algorithms (round robin &
         * least CPU usage).
         *
         * @return optimum cluster endpoint.
         */
        public ClusterEndPoint getOptimumRREndPoint() {
                ClusterEndPoint lTempClusterEndPoint = getRoundRobinEndPoint();
                Object lJwsType = lTempClusterEndPoint.getConnector().getVar("jwsType");

                // if 'ClusterEndPoint' is java script client executes round robin algorithm,
                // but executes least CPU usage algorithm (with CPU usage).
                if (lJwsType.toString().equals("javascript")) {
                        return lTempClusterEndPoint;
                } else {
                        return getOptimumEndPoint();
                }
        }

        /**
         * Refresh the CPU usage to a specific cluster endpoint by the connector
         * id.
         *
         * @param aConnectorId cluster endpoint connector.
         * @param aCpuUsage CPU usage.
         */
        public void refreshCpuUsage(String aConnectorId, double aCpuUsage) {
                for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
                        ClusterEndPoint lTempEndPoint = mEndPoints.get(lPos);
                        if (lTempEndPoint.getStatus().equals(EndPointStatus.ONLINE)) {
                                if (lTempEndPoint.getConnector().getId().equals(aConnectorId)) {
                                        lTempEndPoint.setCpuUsage(aCpuUsage);
                                }
                        }
                }
        }

        /**
         * Verify if endpoints list contains a cluster endpoint with a specific
         * id.
         *
         * @param aEndPointId endpoint id.
         * @return if the endpoints list contains the specified cluster endpoint
         * returns it, but returns
         * <code>null</code>
         */
        public ClusterEndPoint containsEndPoint(String aEndPointId) {
                for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
                        ClusterEndPoint lClusterEndPoint = mEndPoints.get(lPos);
                        if (lClusterEndPoint.getServiceId().equals(aEndPointId)) {
                                return lClusterEndPoint;
                        }
                }
                return null;
        }

        /**
         * @return
         * <code>true</code> if any cluster endpoint have status online;
         * <code>false</code> otherwise.
         */
        public boolean availableEndPoint() {
                for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
                        if (availableEndPoint(lPos)) {
                                return true;
                        }
                }
                return false;
        }

        /**
         * @param lPos position in cluster endpoint list.
         * @return
         * <code>true</code> if any cluster endpoint have status online;
         * <code>false</code> otherwise.
         */
        private boolean availableEndPoint(int lPos) {
                return mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE);
        }

        /**
         * @return a list with all sticky id.
         */
        private List<String> getStickyId() {
                List<String> lIDs = new FastList<String>();
                for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
                        if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
                                lIDs.add(mEndPoints.get(lPos).getServiceId());
                        }
                }

                return lIDs;
        }

        /**
         * @return total of endpoints requests.
         */
        private long getTotalEndPointsRequests() {
                long lRequests = 0;
                Iterator<ClusterEndPoint> lIt = mEndPoints.iterator();
                while (lIt.hasNext()) {
                        lRequests += lIt.next().getRequests();
                }

                return lRequests;
        }
}
