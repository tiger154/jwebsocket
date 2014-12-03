//	---------------------------------------------------------------------------
//	jWebSocket - Report Service for Reporting Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.reporting.service;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.FileUtils;
import org.jwebsocket.plugins.reporting.Settings;
import org.jwebsocket.plugins.reporting.api.IJasperReportService;
import org.jwebsocket.plugins.reporting.api.ReportFormats;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 *
 * @author Javier Alejandro Puentes
 */
public class JasperReportService implements IJasperReportService {

	private Settings mSettings;
	private final Map<String, JasperReport> mCache = new FastMap<String, JasperReport>();

	/**
	 * Gets the Settings object
	 *
	 * @return
	 */
	@Override
	public Settings getSettings() {
		return mSettings;
	}

	/**
	 * Sets the Settings object
	 *
	 * @param aSettings
	 */
	@Override
	public void setSettings(Settings aSettings) {
		this.mSettings = aSettings;
	}

	/**
	 * Gets the names of the templates reports without the 'jrxml' extension
	 * from the output folder
	 *
	 * @return
	 */
	@Override
	public List<String> getReportNames() {
		List<String> lResponse = new ArrayList<String>();
		// getting the files in the directory
		File lDirectory = new File(mSettings.getReportFolder() + File.separator);
		Assert.isTrue(lDirectory.exists() && lDirectory.canRead(), "Invalid "
				+ "templates reports directory");
		String[] lDirectoryList = lDirectory.list();
		// if no files, return an empty response list
		if (lDirectoryList.length == 0) {
			return lResponse;
		}
		// storing the names of the report templates without extension .jrxml
		for (String lFiles : lDirectoryList) {
			if (lFiles.endsWith(".jrxml")) {
				String lFilename = lFiles;
				int lIdx = lFilename.lastIndexOf('.');
				String lReportName;
				if (lIdx > 0) {
					lReportName = lFilename.substring(0, lIdx);
					lResponse.add(lReportName);
				}
			}
		}

		return lResponse;
	}

	/**
	 * Gets a report template path by using its name
	 *
	 * @param aReportName
	 * @return
	 * @throws Exception
	 */
	@Override
	public String getReportTemplatePath(String aReportName) throws Exception {
		String lPath = mSettings.getReportFolder() + aReportName + ".jrxml";
		File lFile = new File(lPath);
		Assert.isTrue(lFile.exists() && lFile.canRead(), "The given report name does not exists!");

		return lFile.getAbsolutePath();
	}

	/**
	 * Save the report in the desire format. Generate an array of bytes to
	 * transform the report in a base64 String.
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
	@Override
	public String generateReport(String aUserHome, String aReportName,
			Map<String, Object> aParams, List<Map<String, Object>> aFields,
			Connection aConnection, String aFormat) throws Exception {

		// getting the report template path to compile
		String lTemplatePath = getReportTemplatePath(aReportName);
		// a JasperReport object
		JasperReport lJasperReport;
		// searching for JasperReport object in cache to improve performance
		if (mCache.containsKey(lTemplatePath)) {
			// initializing the JasperReport object from cache
			lJasperReport = mCache.get(lTemplatePath);
		} else {
			// compiling the JasperReport object using the report template path
			lJasperReport = JasperCompileManager.compileReport(lTemplatePath);
			mCache.put(lTemplatePath, lJasperReport);
		}
		// JasperPrint Object
		JasperPrint lJasperPrint;
		if (null != aConnection) {
			lJasperPrint = JasperFillManager.fillReport(lJasperReport, aParams, aConnection);
		} else {
			Assert.notNull(aFields, "The 'fields' arguments cannot be null!");
			JRBeanCollectionDataSource lReportDataSource = new JRBeanCollectionDataSource(aFields);
			lJasperPrint = JasperFillManager.fillReport(lJasperReport, aParams, lReportDataSource);
		}
		// getting the directory for the report 
		String lOutputDir = mSettings.getOutputFolder().replace("${USER_HOME}", aUserHome);
		FileUtils.forceMkdir(new File(lOutputDir));

		// the final zip file path
		String lFinalPath = "";

		String lDestFile = lOutputDir
				+ File.separator + aReportName;
		// generating the report
		if (ReportFormats.PDF.equals(aFormat)) {
			lDestFile = lDestFile + ".pdf";
			JasperExportManager.exportReportToPdfFile(lJasperPrint, lDestFile);
			lFinalPath = lDestFile;
		} else if (ReportFormats.HTML.equals(aFormat)) {
			lDestFile = lDestFile + ".html";
			JasperExportManager.exportReportToHtmlFile(lJasperPrint, lDestFile);

			// getting the 'report_name'.html page and the report_name_folder_files
			File lFilesDirectory = new File(lDestFile + "_files");
			File lFilePage = new File(lDestFile);

			// moving resulting files to a unique directory to get zip
			File lReportZipFolder = new File(lOutputDir + File.separator + aReportName);
			FileUtils.forceMkdir(lReportZipFolder);
			FileUtils.copyDirectoryToDirectory(lFilesDirectory, lReportZipFolder);
			FileUtils.copyFileToDirectory(lFilePage, lReportZipFolder, true);

			// deleting the resulting files of reporting
			FileUtils.forceDelete(lFilesDirectory);
			FileUtils.forceDelete(lFilePage);

			// using the zip method of FileUtils
			String[] lFiles = new String[]{lReportZipFolder.getPath()};
			Tools.zip(lFiles, lOutputDir + File.separator + aReportName + ".zip");
			FileUtils.deleteDirectory(lReportZipFolder);

			lFinalPath = lOutputDir + aReportName + ".zip";
		} else {
			throw new Exception("The given format is not supported!");
		}
		return lFinalPath.replace(aUserHome, "");
	}

	@Override
	public String getConnectionAlias() {
		return mSettings.getJdbcAlias();
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}
}
