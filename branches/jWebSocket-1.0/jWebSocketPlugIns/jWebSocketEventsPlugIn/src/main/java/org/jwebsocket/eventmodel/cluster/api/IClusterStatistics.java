//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.cluster.api;

/**
 *
 * @author kyberneees
 */
public interface IClusterStatistics {

	/**
	 * 
	 * @return The max concurrent connections number supported by the cluster
	 */
	Integer getMaxConnectionsSupported();

	/**
	 * 
	 * @return The cluster load per cent
	 */
	Integer getLoadPerCent();

	/**
	 * 
	 * @return The cluster current concurrent connections
	 */
	Integer getCurrentConnections();
	
	
	/**
	 * 
	 * @return The cluster CPU usage
	 */
	Integer getCpuUsagePerCent();
}
