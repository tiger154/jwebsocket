//	---------------------------------------------------------------------------
//	jWebSocket - JDBC Appender (Community Edition, CE)
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
package org.jwebsocket.plugins.logging;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.ILog4JAppender;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.util.ConnectionManager;

/**
 *
 * @author Alexander Schulze
 * @author Victor Antonio Barzana Crespo
 */
public final class JWSJDBCAppender extends JDBCAppender implements ILog4JAppender {

	// The table name, note, this will be created if not exists
	private static String mTableName = "logs_table";
	// The datasource id from ConnectionManager 
	private static String mDataSourceId = "";
	private Level mLevel = null;

	public JWSJDBCAppender(String aDataSourceId, String aTableName, String aCreateTableQuery) throws Exception {
		super();
		mTableName = aTableName;
		if (null == mDataSourceId) {
			throw new Exception("JWSJDBCAppender could not be initialized, "
					+ "the dataSourceId was not provided, please check.");
		}
		mDataSourceId = aDataSourceId;
		try {
			ensureTableExists(aCreateTableQuery);

		} catch (Exception lEx) {
			throw new Exception("JWSJDBCAppender could not be initialized: " + lEx.getLocalizedMessage());
		}
	}

	public void ensureTableExists(String aQuery) throws Exception {
		Statement lStatement = null;
		ResultSet lResultSet = null;
		Connection lConnection;
		String lError = "";

		lConnection = getConnection();
		if (null != lConnection) {
			lStatement = lConnection.createStatement();
			DatabaseMetaData lMetadata = lConnection.getMetaData();
			lResultSet = lMetadata.getTables(null, null, mTableName, null);
			if (!lResultSet.next()) {
				try {
					lResultSet.close();
					lStatement.close();
				} catch (SQLException lEx) {
				}
				lStatement = lConnection.createStatement();
				// Formats any given query to the proper sql statement
				JWSJDBCPatternLayout lLayout = new JWSJDBCPatternLayout(aQuery);
				String lQuery = lLayout.format();
				lStatement.execute(lQuery);
				lResultSet = lStatement.getResultSet();
			}

		}

		if (lResultSet != null) {
			try {
				lResultSet.close();
			} catch (SQLException lSQLEx) {
			}
		}
		if (lStatement != null) {
			try {
				lStatement.close();
			} catch (SQLException lSQLEx) {
			}
		}
		getErrorHandler().error(lError);
	}

	public static String getDataSourceId() {
		return mDataSourceId;
	}

	public static void setDataSourceId(String mDataSource) {
		mDataSourceId = mDataSource;
	}

	/**
	 * Handling the JDBC connection with our connection pooling system
	 *
	 * @return Connection lConnection
	 * @throws java.sql.SQLException
	 */
	@Override
	protected Connection getConnection() throws SQLException {
		Connection lConnection = connection;
		if (null == lConnection) {
			ConnectionManager lConnManager = (ConnectionManager) JWebSocketBeanFactory.getInstance()
					.getBean(JWebSocketServerConstants.CONNECTION_MANAGER_BEAN_ID);
			BasicDataSource lDataSource = (BasicDataSource) lConnManager.getConnection(mDataSourceId);

			if (null != lDataSource) {
				lConnection = connection = lDataSource.getConnection();
			}
		}
		return lConnection;
	}

	@Override
	public void append(LoggingEvent aLE) {
		System.out.println("[JWSJDBCAppender] " + getLayout().format(aLE));
		buffer.add(aLE);
		if (buffer.size() >= bufferSize) {
			flushBuffer();
		}
//		super.append(aLE);
//		Object lMsg = aLE.getMessage();
//		Map lInfo = null;
//		//	Trying to get info from the message, this could have been 
//		//	sent via a Token using the loggingPlugIn
//		if (null != lMsg) {
//			//	lInfo = getInfoMapFromMsg((String) lMsg);
//		}
//		if (null != lInfo) {
//			lMsg = lInfo.get("message");
//			lInfo.remove("message");
//		}
//		System.out.println("[JDBC Appender]: "
//				+ aLE.getLevel().toString() + ": "
//				+ lMsg
//				+ (lInfo != null
//						? ", info: " + lInfo
//						: "")
//		);
	}

	@Override
	protected void execute(String sql) throws SQLException {
		Connection lConnection = null;
		Statement lStatement = null;
		try {
			lConnection = getConnection();
			lStatement = lConnection.createStatement();
			lStatement.executeUpdate(sql);
		} catch (SQLException lEx) {
			if (lStatement != null) {
				lStatement.close();
			}
			throw lEx;
		}
		lStatement.close();
//		closeConnection(con);
	}

	@Override
	public void setLevel(Level aLevel) {
		mLevel = aLevel;
	}

	@Override
	public Level getLevel() {
		return mLevel;
	}
}
