//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Reporting Plug-in  (Community Edition, CE)
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
package org.jwebsocket.plugins.reporting;

import org.apache.commons.io.FilenameUtils;
import org.jwebsocket.config.JWebSocketConfig;

/**
 *
 * @author Alexander Schulze
 */
public class Settings {

	private String mReportFolder;
	private String mOutputFolder;
	private String mJdbcAlias;

	/**
	 * @return the mReportFolder
	 */
	public String getReportFolder() {
		return mReportFolder;
	}

	/**
	 * Sets the report folder
	 *
	 * @param aReportFolder
	 */
	public void setReportFolder(String aReportFolder) {
		mReportFolder = aReportFolder;
		mReportFolder = FilenameUtils.separatorsToUnix(JWebSocketConfig.expandEnvVarsAndProps(mReportFolder));
		if (!mReportFolder.endsWith("/")) {
			mReportFolder += "/";
		}
	}

	/**
	 * @return the mOutputFolder
	 */
	public String getOutputFolder() {
		return mOutputFolder;
	}

	/**
	 * Sets the output folder
	 *
	 * @param aOutputFolder
	 */
	public void setOutputFolder(String aOutputFolder) {
		mOutputFolder = aOutputFolder;
	}

	/**
	 * Gets the mJdbcAlias
	 *
	 * @return the mJdbcAlias
	 */
	public String getJdbcAlias() {
		return mJdbcAlias;
	}

	/**
	 *
	 * @param aJdbcAlias
	 */
	public void setJdbcAlias(String aJdbcAlias) {
		mJdbcAlias = aJdbcAlias;
	}

}
