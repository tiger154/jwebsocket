//	---------------------------------------------------------------------------
//	jWebSocket Memory Cluster Manager for LBP (Community Edition, CE)
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
package org.jwebsocket.plugins.loadbalancer.memory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;
import org.jwebsocket.plugins.loadbalancer.api.IClusterManager;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MemoryClusterManager implements IClusterManager {

	private List<ICluster> mClusters = new FastList<ICluster>();
	private int mLbAlgorithm = 3;

	@Override
	public Iterator<ICluster> getClusters() {
		return mClusters.iterator();
	}

	public void setClustersList(List<ICluster> aClusters) {
		mClusters = aClusters;
	}

	public List<ICluster> getClustersList() {
		return mClusters;
	}

	@Override
	public List<Map<String, Object>> getClustersInfo() {
		List<Map<String, Object>> lList = new FastList<Map<String, Object>>();
		for (ICluster lC : mClusters) {
			lList.add(lC.getInfo());
		}

		return lList;
	}

	@Override
	public List<Map<String, String>> getStickyRoutes() {
		List<Map<String, String>> lStickyRoutes = new FastList<Map<String, String>>();
		for (ICluster lC : mClusters) {
			lC.getStickyRoutes(lStickyRoutes);
		}

		return lStickyRoutes;
	}

	@Override
	public ICluster getClusterByAlias(String aAlias) {
		for (ICluster lC : mClusters) {
			if (lC.getAlias().equals(aAlias)) {
				return lC;
			}
		}

		return null;
	}

	@Override
	public void setBalancerAlgorithm(Integer aAlgorithm) {
		mLbAlgorithm = aAlgorithm;
	}

	@Override
	public Integer getBalancerAlgorithm() {
		return mLbAlgorithm;
	}

	@Override
	public ICluster getClusterByNamespace(String aNS) {
		for (ICluster lC : mClusters) {
			if (Tools.wildCardMatch(aNS, lC.getNamespace().split(","))) {
				return lC;
			}
		}

		return null;
	}

	@Override
	public void updateCpuUsage(String aConnectorId, double aCpuUsage) {
		for (ICluster lC : mClusters) {
			lC.updateCpuUsage(aConnectorId, aCpuUsage);
		}
	}

	@Override
	public boolean isNamespaceSupported(String aNS) {
		for (ICluster lC : mClusters) {
			if (Tools.wildCardMatch(aNS, lC.getNamespace().split(","))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public IClusterEndPoint getOptimumServiceEndPoint(String aNS) {
		ICluster lCluster = getClusterByNamespace(aNS);

		return getOptimumServiceEndPoint(lCluster);
	}

	@Override
	public IClusterEndPoint getOptimumServiceEndPoint(ICluster aCluster) {
		if (aCluster.isEndPointAvailable()) {
			if (getBalancerAlgorithm() == 1) {
				return aCluster.getRoundRobinEndPoint();
			} else if (getBalancerAlgorithm() == 2) {
				return aCluster.getOptimumEndPoint();
			} else {
				return aCluster.getOptimumRREndPoint();
			}
		} else {
			return null;
		}
	}

	@Override
	public int removeConnectorEndPoints(String aConnectorId) {
		int lTotal = 0;
		for (ICluster lC : mClusters) {
			lTotal += lC.removeConnectorEndPoints(aConnectorId);
		}

		return lTotal;
	}

}
