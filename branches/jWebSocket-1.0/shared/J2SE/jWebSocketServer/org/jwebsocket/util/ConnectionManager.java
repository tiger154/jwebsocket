//	---------------------------------------------------------------------------
//	jWebSocket ConnectionManager (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.util;

import com.mongodb.Mongo;
import java.security.AccessControlException;
import java.security.AccessController;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import javolution.util.FastMap;
import org.jwebsocket.api.IInitializable;
import static org.jwebsocket.util.Tools.stringToPermission;
import org.springframework.util.Assert;

/**
 * jWebSocket server connections manager
 *
 * @author kyberneees
 */
public class ConnectionManager implements IInitializable {

	private final Map<String, Object> mConnections = new FastMap<String, Object>();

	/**
	 *
	 * @param aConnections
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ConnectionManager(Map<String, Object> aConnections) {
		for (String lName : aConnections.keySet()) {
			putConnection(lName, aConnections.get(lName));
		}
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getConnectionNames() {
		return mConnections.keySet();
	}

	/**
	 *
	 * @param aConnectionName
	 * @return
	 */
	public boolean containsConnection(String aConnectionName) {
		return mConnections.containsKey(aConnectionName);
	}

	private void checkPermission(String aConnectionName) {
		AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
				+ "org.jwebsocket.connection." + aConnectionName + "\", \"write\""));
	}

	/**
	 *
	 * @param aConnectionName
	 * @param aConnection
	 * @throws AccessControlException
	 */
	public void putConnection(String aConnectionName, Object aConnection) throws AccessControlException {
		checkPermission(aConnectionName);

		Assert.isTrue(supportConnection(aConnection), "Unsupported connection!");
		mConnections.put(aConnectionName, aConnection);
	}

	/**
	 *
	 * @param aConnectionName
	 * @return
	 */
	public Object removeConnection(String aConnectionName) {
		checkPermission(aConnectionName);

		return mConnections.remove(aConnectionName);
	}

	/**
	 *
	 * @param aConnectionName
	 * @return
	 * @throws AccessControlException
	 */
	public Object getConnection(String aConnectionName) throws AccessControlException {
		checkPermission(aConnectionName);

		Assert.isTrue(mConnections.containsKey(aConnectionName), "The given connection '"
				+ aConnectionName + "' does not exists!");
		return mConnections.get(aConnectionName);
	}

	/**
	 *
	 * @param aConnection
	 * @return
	 */
	public boolean supportConnection(Object aConnection) {
		return aConnection instanceof Mongo
				|| aConnection instanceof Connection
				|| aConnection instanceof DataSource
				|| aConnection instanceof javax.jms.Connection;
	}

	/**
	 *
	 * @param aConnectionName
	 * @return
	 */
	@SuppressWarnings("UseSpecificCatch")
	public boolean isValid(String aConnectionName) {
		try {
			Object lConnection = getConnection(aConnectionName);

			if (lConnection instanceof Mongo) {
				return isValid((Mongo) lConnection);
			} else if (lConnection instanceof Connection) {
				return isValid((Connection) lConnection);
			} else if (lConnection instanceof DataSource) {
				return isValid((DataSource) lConnection);
			} else {
				return isValid((javax.jms.Connection) lConnection);
			}
		} catch (Exception lEx) {
			return false;
		}
	}

	/**
	 *
	 * @param aConnection
	 * @return
	 */
	public static boolean isValid(Mongo aConnection) {
		try {
			aConnection.getConnector().getDBPortPool(aConnection.getAddress()).get().ensureOpen();
		} catch (Exception lEx) {
			return false;
		}

		return true;
	}

	/**
	 *
	 * @param aConnection
	 * @return
	 * @throws SQLException
	 */
	public static boolean isValid(Connection aConnection) throws SQLException {
		return aConnection.isValid(3000);
	}

	/**
	 *
	 * @param aConnection
	 * @return
	 */
	public static boolean isValid(javax.jms.Connection aConnection) {
		return true;
	}

	/**
	 *
	 * @param aDS
	 * @return
	 * @throws SQLException
	 */
	public static boolean isValid(DataSource aDS) throws SQLException {
		return isValid(aDS.getConnection());
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}
}
