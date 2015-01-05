//	---------------------------------------------------------------------------
//	jWebSocket ConnectionManager (Community Edition, CE)
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
package org.jwebsocket.util;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
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
 * jWebSocket server connections manager. The component is designed to store a
 * reference of all existing server side 'connection' objects to easily allow
 * connection status checks in action plug-ins.
 *
 * <tt>Usage:</tt>
 * <code>@RequiredConnection(name = "aConnectionName")
 * public void someAction(WebSocketConnector aConnector, Token aToken)
 * throws Exception {}</code>
 *
 * @author Rolando Santamaria Maso
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
	 * Get a collection with existing collection names
	 *
	 * @return
	 */
	public Set<String> getConnectionNames() {
		return mConnections.keySet();
	}

	/**
	 * Return TRUE if the given connection name matches an existing connection
	 * name, FALSE otherwise
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
	 * Store a connection reference in the connection manager
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
	 * Remove a connection given it name
	 *
	 * @param aConnectionName
	 * @return
	 */
	public Object removeConnection(String aConnectionName) {
		checkPermission(aConnectionName);

		return mConnections.remove(aConnectionName);
	}

	/**
	 * Get a connection by it name
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
	 * Return TRUE if the given connection object is supported by the manager,
	 * FALSE otherwise
	 *
	 * @param aConnection
	 * @return
	 */
	public boolean supportConnection(Object aConnection) {
		return aConnection instanceof Mongo
				|| aConnection instanceof Connection
				|| aConnection instanceof DataSource
				|| aConnection instanceof JdbcConnectionSource
				|| aConnection instanceof javax.jms.Connection;
	}

	/**
	 * Return TRUE if the connection that matches the given connection name is
	 * valid, FALSE otherwise
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
			} else if (lConnection instanceof JdbcConnectionSource) {
				return isValid((JdbcConnectionSource) lConnection);
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
	 * Return TRUE of the given mongodb connection object is valid, FALSE
	 * otherwise
	 *
	 * @param aConnection
	 * @return
	 */
	public static boolean isValid(Mongo aConnection) {
		try {
			aConnection.getDatabaseNames();
		} catch (Exception lEx) {
			return false;
		}

		return true;
	}

	/**
	 * Return TRUE if the given SQL connection object is valid, FALSE otherwise
	 *
	 * @param aConnection
	 * @return
	 * @throws SQLException
	 */
	public static boolean isValid(Connection aConnection) throws SQLException {
		return aConnection.isValid(3000);
	}

	/**
	 * Return TRUE if the given JMS connection object is valid, FALSE otherwise
	 *
	 * @param aConnection
	 * @return
	 */
	public static boolean isValid(javax.jms.Connection aConnection) {
		return true;
	}

	/**
	 * Return TRUE if the given JdbcConnectionSource object is valid, FALSE
	 * otherwise
	 *
	 * @param aConnection
	 * @return
	 */
	public static boolean isValid(JdbcConnectionSource aConnection) {
		try {
			return !aConnection.getReadWriteConnection().isClosed();
		} catch (Exception lEx) {
			return false;
		}
	}

	/**
	 * Return TRUE if the given DataSource object is valid, FALSE otherwise
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
