//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Settings for Reporting Plug-in
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
	 * @param mReportFolder the mReportFolder to set
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
	 * @param mOutputFolder the mOutputFolder to set
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
