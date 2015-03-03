//	---------------------------------------------------------------------------
//	jWebSocket - Native SQL Access for JDBC Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.jdbc;

import javax.sql.DataSource;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author Alexander Schulze
 */
public class NativeAccess {

	private JdbcTemplate mJDBCTemplate;
	private String mSelectSequenceSQL = null;
	private String mExecFunctionSQL = null;
	private String mExecStoredProcSQL = null;

	/**
	 * 
	 * @param aDataSource
	 */
	public void setDataSource(DataSource aDataSource) {
		mJDBCTemplate = new JdbcTemplate(aDataSource);
		// TODO: make query time out configurable with spring
		mJDBCTemplate.setQueryTimeout(10); // seconds
	}

	/**
	 * 
	 * @param aSQL
	 */
	public void setSelectSequenceSQL(String aSQL) {
		mSelectSequenceSQL = aSQL;
	}

	/**
	 * 
	 * @return
	 */
	public String getSelectSequenceSQL() {
		return mSelectSequenceSQL;
	}

	/**
	 * @return the mExecFunctionSQL
	 */
	public String getExecFunctionSQL() {
		return mExecFunctionSQL;
	}

	/**
	 * @param mExecFunctionSQL the mExecFunctionSQL to set
	 */
	public void setExecFunctionSQL(String mExecFunctionSQL) {
		this.mExecFunctionSQL = mExecFunctionSQL;
	}

	/**
	 * @return the mExecStoredProcSQL
	 */
	public String getExecStoredProcSQL() {
		return mExecStoredProcSQL;
	}

	/**
	 * @param mExecStoredProcSQL the mExecStoredProcSQL to set
	 */
	public void setExecStoredProcSQL(String mExecStoredProcSQL) {
		this.mExecStoredProcSQL = mExecStoredProcSQL;
	}

	/**
	 * 
	 * @return
	 */
	public DataSource getDataSource() {
		return mJDBCTemplate.getDataSource();
	}

	/**
	 * 
	 * @param aSQL
	 * @param aArgs
	 * @return
	 */
	public Token query(String aSQL, Object[] aArgs) {
		Token lResToken;
		SqlRowSet lRowSet;
		try {
			if (aArgs != null) {
				lRowSet = mJDBCTemplate.queryForRowSet(aSQL, aArgs);
			} else {
				lRowSet = mJDBCTemplate.queryForRowSet(aSQL);
			}
			lResToken = JDBCTools.resultSetToToken(lRowSet);
			lResToken.setInteger("code", 0);
		} catch (Exception lEx) {
			lResToken = TokenFactory.createToken();
			lResToken.setInteger("code", -1);
			lResToken.setString("msg",
					lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		return lResToken;
	}

	/**
	 * 
	 * @param aSQL
	 * @return
	 */
	public Token query(String aSQL) {
		return query(aSQL, null);
	}

	/**
	 * 
	 * @param aSQL
	 * @param aArgs
	 * @return
	 */
	public Token update(String aSQL, Object[] aArgs) {
		Token lResToken = TokenFactory.createToken();
		int lAffectedRows = 0;
		try {
			if (aArgs != null) {
				lAffectedRows = mJDBCTemplate.update(aSQL, aArgs);
			} else {
				lAffectedRows = mJDBCTemplate.update(aSQL);
			}
			lResToken.setInteger("code", 0);
		} catch (Exception lEx) {
			lResToken.setInteger("code", -1);
			lResToken.setString("msg",
					lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		lResToken.setInteger("rowsAffected", lAffectedRows);

		return lResToken;
	}

	/**
	 * 
	 * @param aSQL
	 * @return
	 */
	public Token update(String aSQL) {
		return update(aSQL, null);
	}

	/**
	 * 
	 * @param aSQL
	 * @return
	 */
	public Token exec(String aSQL) {
		Token lResToken = TokenFactory.createToken();
		try {
			mJDBCTemplate.execute(aSQL);
			lResToken.setInteger("code", 0);
		} catch (Exception lEx) {
			lResToken.setInteger("code", -1);
			lResToken.setString("msg",
					lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		return lResToken;
	}
}
