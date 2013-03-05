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
 * @author aschulze
 */
public class Settings {

	private String mReportFolder = null;
	private String mOutputFolder = null;
	private String mOutputURL = null;
	private String mReportNamePattern = null;

	/**
	 * @return the mReportFolder
	 */
	public String getReportFolder() {
		return mReportFolder;
	}

	/**
	 *
	 * @param aReportFolder
	 */
	public void setReportFolder(String aReportFolder) {
		mReportFolder = aReportFolder;
		mReportFolder = FilenameUtils.separatorsToUnix(JWebSocketConfig.expandEnvAndJWebSocketVars(mReportFolder));
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
	 *
	 * @param aOutputFolder
	 */
	public void setOutputFolder(String aOutputFolder) {
		mOutputFolder = aOutputFolder;
		mOutputFolder = FilenameUtils.separatorsToUnix(JWebSocketConfig.expandEnvAndJWebSocketVars(mOutputFolder));
		if (!mOutputFolder.endsWith("/")) {
			mOutputFolder += "/";
		}
	}

	/**
	 * @return the mOutputURL
	 */
	public String getOutputURL() {
		return mOutputURL;
	}

	/**
	 * @param mOutputURL the mOutputURL to set
	 */
	public void setOutputURL(String mOutputURL) {
		this.mOutputURL = mOutputURL;
	}

	/**
	 * @return the mReportNamePattern
	 */
	public String getReportNamePattern() {
		return mReportNamePattern;
	}

	/**
	 * @param mReportNamePattern the mReportNamePattern to set
	 */
	public void setReportNamePattern(String mReportNamePattern) {
		this.mReportNamePattern = mReportNamePattern;
	}
}
