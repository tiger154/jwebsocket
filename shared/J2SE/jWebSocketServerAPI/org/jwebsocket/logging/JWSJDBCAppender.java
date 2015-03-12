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
package org.jwebsocket.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import org.apache.log4j.Layout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Alexander Schulze
 * @author Victor Antonio Barzana Crespo
 */
public class JWSJDBCAppender extends JDBCAppender {
	
	@Override
	public void setLayout(Layout layout) {

	}

	/**
	 * TODO: We need to Override this to link it with our mConnection pooling
	 * system.
	 *
	 */
	@Override
	protected Connection getConnection() throws SQLException {
//		if (!DriverManager.getDrivers().hasMoreElements()) {
//			setDriver("sun.jdbc.odbc.JdbcOdbcDriver");
//		}
//
//		if (mConnection == null) {
//			mConnection = DriverManager.getConnection(mDBURL, mDBUser,
//					mDBPassword);
//		}
//
		return DriverManager.getConnection("localhost", "root",
				"root");
	}

	/**
	 * TODO: CLOSE THE CONNECTION USING OUR OWN METHODS
	 */
	@Override
	public void close() {
		flushBuffer();

//		try {
//			if (mConnection != null && !mConnection.isClosed()) {
//				mConnection.close();
//			}
//		} catch (SQLException e) {
//			mErrorHandler.error("Error closing connection", e, ErrorCode.GENERIC_FAILURE);
//		}
//		this.mIsClosed = true;
	}

	// TODO: we need to create our own way to get the SQL into the database, 
	// we can do this by overriding the PatternLayout
	@Override
	public void append(LoggingEvent aLE) {
		Object lMsg = aLE.getMessage();
		Map lInfo = null;
		//	Trying to get info from the message, this could have been 
		//	sent via a Token using the loggingPlugIn
		if (null != lMsg) {
			//	lInfo = getInfoMapFromMsg((String) lMsg);
		}
		if (null != lInfo) {
			lMsg = lInfo.get("message");
			lInfo.remove("message");
		}
		System.out.println("[JDBC Appender]: "
				+ aLE.getLevel().toString() + ": "
				+ lMsg
				+ (lInfo != null
						? ", info: " + lInfo
						: "")
		);
	}

}
