//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Native SQL Access for JDBC Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jdbc;

import javax.sql.DataSource;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author aschulze
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
