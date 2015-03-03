//	---------------------------------------------------------------------------
//	jWebSocket - API for Reporting Plug-In (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
package org.jwebsocket.plugins.reporting.api;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.plugins.reporting.Settings;

/**
 *
 * @author Javier Alejandro Puentes
 */
public interface IJasperReportService extends IInitializable {

	/**
	 * Gets the Jasper reports templates names. Excluding the file extension.
	 *
	 * @return
	 */
	List<String> getReportNames();

	/**
	 * Gets a report template path
	 *
	 * @param aReportName
	 * @return
	 * @throws Exception
	 */
	String getReportTemplatePath(String aReportName) throws Exception;

	/**
	 * Generates a report using a Map of parameters or a List of Fields or a
	 * Connection.
	 *
	 * @param aUserHome
	 * @param aReportName
	 * @param aParams
	 * @param aFields
	 * @param aConnection
	 * @param aFormat
	 * @return
	 * @throws Exception
	 */
	String generateReport(String aUserHome, String aReportName,
			Map<String, Object> aParams, List<Map<String, Object>> aFields,
			Connection aConnection, String aFormat) throws Exception;

	/**
	 * Gets a Settings object
	 *
	 * @return
	 */
	Settings getSettings();

	/**
	 * Sets a Settings object
	 *
	 * @param aSettings
	 */
	void setSettings(Settings aSettings);

	/**
	 * Gets the JDBC connection alias
	 *
	 * @return
	 */
	String getConnectionAlias();
}
