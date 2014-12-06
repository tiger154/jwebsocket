//	---------------------------------------------------------------------------
//	jWebSocket Filesystem Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.activation.MimetypesFileTypeMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.http.HTTPConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.MapAppender;
import org.jwebsocket.util.Tools;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * The FileSystemPlugIn class is a plug-in implementation for the jWebSocket
 * framework to provide support for the basic file system management operations
 * in WebSocket applications.
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class FileSystemPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 * Namespace for the FSP
	 */
	public static final String NS_FILESYSTEM
			= JWebSocketServerConstants.NS_BASE + ".plugins.filesystem";
	private final static String VERSION = "1.0.1";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket FileSystemPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket FileSystemPlugIn - Community Edition";
	/**
	 * Private alias directory settings map key.
	 */
	public static final String PRIVATE_ALIAS_DIR_KEY = "privateDir";
	/**
	 * Private Session alias directory settings map key.
	 */
	public static final String SESSION_ALIAS_DIR_KEY = "sessionDir";

	/**
	 *
	 */
	public static final String UUID_ALIAS_DIR_KEY = "uuidDir";
	/**
	 * Public alias directory settings map key.
	 */
	public static final String PUBLIC_ALIAS_DIR_KEY = "publicDir";
	/**
	 * Web root alias settings map key.
	 */
	public static final String ALIAS_WEB_ROOT_KEY = "webRoot";
	/**
	 * Private alias directory default value.
	 */
	public static final String PRIVATE_ALIAS_DIR_DEF = "${"
			+ JWebSocketServerConstants.JWEBSOCKET_HOME + "}/filesystem/private/{username}/";
	/**
	 * Public alias directory default value.
	 */
	public static final String PUBLIC_ALIAS_DIR_DEF = "${"
			+ JWebSocketServerConstants.JWEBSOCKET_HOME + "}/filesystem/public/";
	/**
	 * Web root alias default value.
	 */
	public static final String ALIAS_WEB_ROOT_DEF = "http://localhost/public/";
	private FileAlterationMonitor mFileSystemMonitor = null;
	/**
	 * FSP bean factory cached instance.
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 * FSP settings instance.
	 */
	protected Settings mSettings;

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public FileSystemPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating FileSystem plug-in...");
		}
		// specify default name space for file system plugin
		this.setNamespace(NS_FILESYSTEM);

		try {
			mBeanFactory = getConfigBeanFactory(NS_FILESYSTEM);
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for filesystem plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.filesystem.settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("Filesystem plug-in successfully instantiated.");
				}
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"instantiating filesystem plug-in"));
			throw lEx;
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public synchronized void engineStarted(WebSocketEngine aEngine) {
		if (mSettings.isMonitoringActive() 
				&& getServer().getEngines().size() >= 1) {
			startAliasesMonitor(mSettings.getMonitoringInterval());
		}
	}

	@Override
	public synchronized void engineStopped(WebSocketEngine aEngine) {
		if (mSettings.isMonitoringActive() 
				&& getServer().getEngines().size() >= 1) {
			stopAliasesMonitor();
		}
	}

	@Override
	public String getNamespace() {
		return NS_FILESYSTEM;
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();

		if (lType != null) {
			if (lType.equals("save")) {
				save(aConnector, aToken);
			} else if (lType.equals("load")) {
				load(aConnector, aToken);
			} else if (lType.equals("send")) {
				send(aConnector, aToken);
			} else if (lType.equals("getFilelist")) {
				getFilelist(aConnector, aToken);
			} else if (lType.equals("delete")) {
				delete(aConnector, aToken);
			} else if (lType.equals("exists")) {
				exists(aConnector, aToken);
			} else if (lType.equals("append")) {
				append(aConnector, aToken);
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("getFilelist")) {
				return mGetFilelist(aConnector, aToken);
			} else if (lType.equals("getAliasPath")) {
				String lTargetAlias = aToken.getString("alias");
				Token lToken = TokenFactory.createToken();
				lToken.setString("aliasPath",
						getAliasPath(aConnector, lTargetAlias));

				return lToken;
			}
		}

		return null;
	}

	private String replaceAliasVars(WebSocketConnector aConnector, String aBaseDir) {
		aBaseDir = JWebSocketConfig.expandEnvVarsAndProps(aBaseDir);
		aBaseDir = aBaseDir.replace("{username}", aConnector.getUsername());
		if (null != aConnector.getSession()) {
			aBaseDir = aBaseDir.replace("{uuid}", aConnector.getSession().getUUID());
			aBaseDir = aBaseDir.replace("{sessionId}", aConnector.getSession().getSessionId());
		}
		return aBaseDir;
	}

	/**
	 * Gets the directory path for a given alias.
	 *
	 * @param aConnector The requester connector.
	 * @param aAlias The alias value.
	 * @return
	 */
	public String getAliasPath(WebSocketConnector aConnector, String aAlias) {
		String lBaseDir = mSettings.getAliasPath(aAlias);

		if (null != lBaseDir && (PRIVATE_ALIAS_DIR_KEY.equals(aAlias)
				|| SESSION_ALIAS_DIR_KEY.equals(aAlias)
				|| UUID_ALIAS_DIR_KEY.equals(aAlias))) {
			lBaseDir = replaceAliasVars(aConnector, lBaseDir);
			/*
			 lBaseDir = JWebSocketConfig.expandEnvVarsAndProps(lBaseDir);
			 lBaseDir = lBaseDir.replace("{username}", aConnector.getUsername());
			 */
		}

		return lBaseDir;
	}

	/**
	 * Indicates if a file exists in a given alias.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void exists(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'exists'...");
		}

		TokenServer lServer = getServer();

		// check if user is allowed to run 'exists' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM + ".exists")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		String lAlias = aToken.getString("alias", PRIVATE_ALIAS_DIR_KEY);
		String lFilename = aToken.getString("filename", null);
		if (null == lFilename) {
			sendErrorToken(aConnector, aToken, -1, "Missing filename argument!");
			return;
		}

		// getting alias
		String lBaseDir = getAliasPath(aConnector, lAlias);
		File lFile = null;
		if (lBaseDir != null) {
			lFile = new File(lBaseDir + "/" + lFilename);
		} else {
			sendErrorToken(aConnector, aToken, -1,
					"The given alias '" + lAlias + "' does not exist!");
			return;
		}

		Token lResponse = createResponse(aToken);
		try {
			if (!isPathInFS(lFile, lBaseDir)) {
				sendErrorToken(aConnector, aToken, -1, "The file '" + lFilename
						+ "' is out of the file-system location!");
				return;
			}
		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			String lMsg = lEx.getClass().getSimpleName() + " on exists: "
					+ lEx.getMessage();
			lResponse.setString("msg", lMsg);
			mLog.error(lMsg);
		}

		// file exists?
		boolean lExists = lFile.exists();
		lResponse.setBoolean("exists", lExists);
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * Saves or appends a file, depends of the aToken.append (Boolean) arg.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void mWrite(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		boolean lAppend = aToken.getBoolean("append");
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing '" + (lAppend ? "append" : "save") + "'...");
		}

		// check if user is allowed to run 'save' or 'append' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM
				+ "." + (lAppend ? "append" : "save"))) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}
		String lAlias = aToken.getString("alias", PUBLIC_ALIAS_DIR_KEY);

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		// obtain required parameters for file load operation
		String lFilename = aToken.getString("filename");
		String lScope = aToken.getString("scope", JWebSocketCommonConstants.SCOPE_PRIVATE);

		// scope may be "private" or "public"
		String lBaseDir;
		if (JWebSocketCommonConstants.SCOPE_PRIVATE.equals(lScope)) {
			if (SESSION_ALIAS_DIR_KEY.equals(lAlias)) {
				lBaseDir = getAliasPath(aConnector, lAlias);
			} else if (UUID_ALIAS_DIR_KEY.equals(lAlias)) {
				lBaseDir = getAliasPath(aConnector, lAlias);
			} else {
				lBaseDir = getAliasPath(aConnector, PRIVATE_ALIAS_DIR_KEY);
			}
		} else if (JWebSocketCommonConstants.SCOPE_PUBLIC.equals(lScope)) {
			lBaseDir = getAliasPath(aConnector, lAlias);
		} else {
			lMsg = "invalid scope";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lServer.sendErrorToken(aConnector, aToken, -1, lMsg);
			return;
		}

		Boolean lNotify = aToken.getBoolean("notify", false);
		String lData = aToken.getString("data", null);
		if (null == lData) {
			sendErrorToken(aConnector, aToken, -1,
					"Argument 'data' contains null value!");
			return;
		}

		String lEncoding = aToken.getString("encoding", "base64");
		Boolean lEncode = aToken.getBoolean("encode", false);
		byte[] lBA;
		if (!lEncode && "base64".equals(lEncoding)) {
			// supporting HTML5 readAsDataURL method
			int lIdx = lData.indexOf(',');
			if (lIdx >= 0) {
				lData = lData.substring(lIdx + 1);
			}
			lBA = Tools.base64Decode(lData);
			lData = null;
		} else {
			lBA = lData.getBytes();
		}

		String lFullPath = lBaseDir + lFilename;
		File lFile = new File(lFullPath);
		try {
			if (!isPathInFS(lFile, lBaseDir)) {
				sendErrorToken(aConnector, aToken, -1, "The file '" + lFilename
						+ "' is out of the file-system location!");
				return;
			}

			checkForSave(aConnector, lFile, lBA);
			// prevent two threads at a time writing to the same file
			synchronized (this) {
				// force create folder if not yet exists
				File lDir = new File(FilenameUtils.getFullPath(lFullPath));
				FileUtils.forceMkdir(lDir);
				if (lData == null) {
					FileUtils.writeByteArrayToFile(lFile, lBA, lAppend);
				} else {
					FileUtils.writeStringToFile(lFile, lData, "UTF-8", lAppend);
				}
			}
		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			lMsg = lEx.getClass().getSimpleName() + " while saving file: "
					+ lFilename + ". " + lEx.getMessage();
			lResponse.setString("msg", lMsg);
			mLog.error(lMsg);
		}
		// send response to requester
		lServer.sendToken(aConnector, lResponse);

		// send notification event to other affected clients
		// to allow to update their content (if desired)
		Token lEvent;
		// create token of type "event"
		lEvent = TokenFactory.createToken(BaseToken.TT_EVENT);
		lEvent.setNS(NS_FILESYSTEM);
		lEvent.setString("name", lAppend ? "fileappended" : "filesaved");
		lEvent.setString("filename", lFilename);
		lEvent.setString("sourceId", aConnector.getId());

		if (lNotify) {
			if (lScope.equals(JWebSocketCommonConstants.SCOPE_PUBLIC)) {
				lServer.broadcastToken(lEvent);
			} else {
				// notify requester if desired
				lServer.sendToken(aConnector, lEvent);
			}
		}
	}

	/**
	 * Saves a file into a given scope.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void save(WebSocketConnector aConnector, Token aToken) {
		aToken.setBoolean("append", Boolean.FALSE);
		mWrite(aConnector, aToken);
	}

	/**
	 * Appends a file into a given scope.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void append(WebSocketConnector aConnector, Token aToken) {
		aToken.setBoolean("append", Boolean.TRUE);
		mWrite(aConnector, aToken);
	}

	/**
	 * Allows to perform actions and checks before save a file.
	 *
	 * @param aConnector
	 * @param lFile
	 * @param lBA
	 * @throws Exception
	 */
	protected void checkForSave(WebSocketConnector aConnector, File lFile, byte[] lBA) throws Exception {
		// TODO: to be overwritten for enterprise filesystem plug-in
	}

	/**
	 * Load a file from a given alias.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void load(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'load'...");
		}

		// check if user is allowed to run 'load' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM + ".load")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// obtain required parameters for file load operation
		String lFilename = aToken.getString("filename");
		String lAlias = aToken.getString("alias", PRIVATE_ALIAS_DIR_KEY);
		String lData;

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lBaseDir = getAliasPath(aConnector, lAlias);
		if (null == lBaseDir) {
			sendErrorToken(aConnector, aToken, -1, "The given alias '" + lAlias
					+ "' does not exist!");
			return;
		}

		File lFile = new File(lBaseDir + lFilename);
		String lEncoding = aToken.getString("encoding", "base64");

		byte[] lBA;
		try {
			if (!isPathInFS(lFile, lBaseDir)) {
				sendErrorToken(aConnector, aToken, -1, "The file '" + lFilename
						+ "' is out of the file-system location!");
				return;
			}

			if (!lFile.exists()) {
				sendErrorToken(aConnector, aToken, -1, "The file '" + lFilename
						+ "' does not exist in the given alias!");
				return;
			}

			if (aConnector instanceof HTTPConnector) {
				// supporting HTTP download
				ServletUtils.sendFile(((HTTPConnector) aConnector).getHttpResponse(), lFile);
				((HTTPConnector) aConnector).setHttpResponse(null);
				if (mLog.isDebugEnabled()){
					mLog.debug("File '"+ lFilename + "' sent in the HTTP response!");
				}
			} else {

				lBA = FileUtils.readFileToByteArray(lFile);

				// populating the response data field according to the file type
				String lFileType = new MimetypesFileTypeMap().getContentType(lFile);
				boolean lIsBinary;
				try {
					lIsBinary = Tools.isBinaryFile(lFile);
				} catch (IOException aEx) {
					lIsBinary = lFileType.contains("text") || lFileType.contains("json")
							|| lFileType.contains("javascript");
				}
				if (!lIsBinary) {
					lData = new String(lBA);
					lResponse.setString("data", lData);
				} else {
					lResponse.getMap().put("data", lBA);
					lResponse.setBoolean("isBinary", Boolean.TRUE);
				}
				// setting the file MIME type
				lResponse.setString("mime", lFileType);

				// send response to requester
				lResponse.setMap("enc", new MapAppender().append("data", lEncoding).getMap());
				lResponse.setString("filename", lFilename);
				lServer.sendToken(aConnector, lResponse);
			}
		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			lMsg = lEx.getClass().getSimpleName() + " on load: " + lEx.getMessage();
			lResponse.setString("msg", lMsg);
			lServer.sendToken(aConnector, lResponse);
		}
	}

	/**
	 * Sends a file from one client to another client.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void send(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'send'...");
		}

		// check if user is allowed to run 'save' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM + ".send")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		String lFilename = aToken.getString("filename");
		String lData = aToken.getString("data");
		String lNodeId = aToken.getString("unid");
		String lTargetId = aToken.getString("targetId");
		String lEncoding = aToken.getString("encoding");

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		WebSocketConnector lTarget = null;
		if (lNodeId != null) {
			lTarget = lServer.getNode(lNodeId);
		} else if (lTargetId != null) {
			lTarget = lServer.getConnector(lTargetId);
		}
		if (lTarget != null) {
			// send notification event to target client
			// to allow to update their content (if desired)
			// create token of type "event"
			Token lEvent = TokenFactory.createToken(BaseToken.TT_EVENT);
			// include name space of this plug-in
			lEvent.setNS(NS_FILESYSTEM);
			lEvent.setString("name", "filereceived");
			lEvent.setString("filename", lFilename);
			lEvent.setString("sourceId", aConnector.getId());
			lEvent.setString("data", lData);
			lEvent.setString("encoding", lEncoding);
			// send file to target client
			lServer.sendToken(lTarget, lEvent);
		} else {
			lMsg = "target '" + lTargetId + "' not found";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * Gets the file list from a given alias an optionally from a sub path.
	 *
	 * @param aUsername The requester client username.
	 * @param aToken
	 * @return
	 */
	private Token mGetFilelist(WebSocketConnector aConnector, Token aToken) {

		String lAlias = aToken.getString("alias");
		boolean lRecursive = aToken.getBoolean("recursive", false);
		boolean lIncludeDirs = aToken.getBoolean("includeDirs", false);
		List<Object> lFilemasks = aToken.getList("filemasks", new FastList<Object>());
		String lSubPath = aToken.getString("path", null);
		Object lObject;
		String lBaseDir;
		Token lToken = TokenFactory.createToken();

		lObject = mSettings.getAliasPath(lAlias);
		if (lObject != null) {
			lBaseDir = (String) lObject;
			lBaseDir = replaceAliasVars(aConnector, lBaseDir);
			/*
			 lBaseDir = JWebSocketConfig.expandEnvVarsAndProps(lBaseDir).
			 replace("{username}", aConnector.getUsername());
			 */
			File lDir;
			if (null != lSubPath) {
				lDir = new File(lBaseDir + File.separator + lSubPath);
			} else {
				lDir = new File(lBaseDir + File.separator);
			}

			if (!isPathInFS(lDir, lBaseDir)) {
				lToken.setInteger("code", -1);
				lToken.setString("msg", "The path '" + lSubPath
						+ "' is out of the file-system location!");

				return lToken;
			} else if (!(lDir.exists() && lDir.isDirectory())) {
				lToken.setInteger("code", -1);
				lToken.setString("msg", "The path '" + lSubPath
						+ "' is not directory on target '" + lAlias + "' alias!");

				return lToken;
			}
			// IOFileFilter lFileFilter = FileFilterUtils.nameFileFilter(lFilemask);
			String[] lFilemaskArray = new String[lFilemasks.size()];
			int lIdx = 0;
			for (Object lMask : lFilemasks) {
				lFilemaskArray[lIdx] = (String) lMask;
				lIdx++;
			}
			IOFileFilter lFileFilter = new WildcardFileFilter(lFilemaskArray);
			IOFileFilter lDirFilter = null;
			if (lRecursive) {
				lDirFilter = FileFilterUtils.directoryFileFilter();
			}
			Collection<File> lFiles = FileUtils.listFilesAndDirs(lDir, lFileFilter, lDirFilter);
			List<Map> lFileList = new FastList<Map>();
			File lBasePath = new File(lBaseDir);
			MimetypesFileTypeMap lMimesMap = new MimetypesFileTypeMap();
			String lRelativePath;
			for (File lFile : lFiles) {
				if (lFile == lDir
						// we don't want directories to be returned
						// except explicitely requested
						|| (!lIncludeDirs && lFile.isDirectory())) {
					continue;
				}
				Map<String, Object> lFileData = new FastMap<String, Object>();
				String lFilename = lFile.getAbsolutePath()
						.replace(lBasePath.getAbsolutePath() + File.separator, "");
				// we always return the path in unix/url/java format
				String lUnixPath = FilenameUtils.separatorsToUnix(lFilename);
				int lSeparator = lUnixPath.lastIndexOf("/");
				if (lSeparator != -1) {
					lFilename = lUnixPath.substring(lSeparator + 1);
					lRelativePath = lUnixPath.substring(0, lSeparator + 1);
				} else {
					lRelativePath = "";
				}

				lFileData.put("relativePath", lRelativePath);
				lFileData.put("filename", lFilename);
				lFileData.put("size", lFile.length());
				lFileData.put("modified", Tools.DateToISO8601(new Date(lFile.lastModified())));
				lFileData.put("hidden", lFile.isHidden());
				lFileData.put("canRead", lFile.canRead());
				lFileData.put("canWrite", lFile.canWrite());
				lFileData.put("directory", lFile.isDirectory());
				lFileData.put("mime", lMimesMap.getContentType(lFile));
				if (lAlias.equals(PRIVATE_ALIAS_DIR_KEY)) {
					lFileData.put("url", getString(ALIAS_WEB_ROOT_KEY, ALIAS_WEB_ROOT_DEF)
							// in URLs we only want forward slashes
							+ FilenameUtils.separatorsToUnix(lFilename));
				}
				lFileList.add(lFileData);
			}
			lToken.setList("files", lFileList);
			lToken.setInteger("code", 0);
			lToken.setString("msg", "ok");
		} else {
			lToken.setInteger("code", -1);
			lToken.setString("msg", "No alias '" + lAlias
					+ "' defined for filesystem plug-in");
		}

		return lToken;
	}

	/**
	 * Returns TRUE if a file path is inside of a given base path, FALSE
	 * otherwise
	 *
	 * @param aFile
	 * @param aBasePath
	 * @return
	 */
	protected boolean isPathInFS(File aFile, String aBasePath) {
		return Tools.isParentPath(aFile, aBasePath);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		super.connectorStopped(aConnector, aCloseReason);
	}

	/**
	 * Deletes a file on a target scope.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void delete(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'delete'...");
		}

		// check if user is allowed to run 'delete' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM + ".delete")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			getServer().sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		boolean lForce = aToken.getBoolean("force", true);
		String lFilename = aToken.getString("filename", null);
		String lScope = aToken.getString("scope", JWebSocketCommonConstants.SCOPE_PRIVATE);
		Boolean lNotify = aToken.getBoolean("notify", false);

		// scope may be "private" or "public"
		String lBaseDir;
		String lAlias = aToken.getString("alias", PUBLIC_ALIAS_DIR_KEY);
		if (JWebSocketCommonConstants.SCOPE_PRIVATE.equals(lScope)) {
			if (SESSION_ALIAS_DIR_KEY.equals(lAlias)) {
				lBaseDir = getAliasPath(aConnector, lAlias);
			} else if (UUID_ALIAS_DIR_KEY.equals(lAlias)) {
				lBaseDir = getAliasPath(aConnector, lAlias);
			} else {
				lBaseDir = getAliasPath(aConnector, PRIVATE_ALIAS_DIR_KEY);
			}
		} else if (JWebSocketCommonConstants.SCOPE_PUBLIC.equals(lScope)) {
			lBaseDir = getAliasPath(aConnector, lAlias);
		} else {
			lMsg = "invalid scope";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lServer.sendErrorToken(aConnector, aToken, -1, lMsg);
			return;
		}

		String lFilePath = lBaseDir + lFilename;
		File lFile = new File(lFilePath);

		try {
			if (!isPathInFS(lFile, lBaseDir)) {
				sendErrorToken(aConnector, aToken, -1, "The file '" + lFilename
						+ "' is out of the file-system location!");
				return;
			}

			if (null == lFilename || !lFile.exists()) {
				sendErrorToken(aConnector, aToken, -1, "The given filename '"
						+ lFilename + "' is invalid or does not exist!");
				return;
			}

			if (lForce) {
				FileUtils.forceDelete(lFile);
			} else {
				if (!FileUtils.deleteQuietly(lFile)) {
					throw new Exception("File could not be deleted quietly!");
				}
			}
		} catch (Exception aEx) {
			sendErrorToken(aConnector, aToken, -1, "File '" + lFilename
					+ "' could not be deleted. " + aEx.getMessage());
			return;
		}

		if (lNotify && lScope.equals(JWebSocketCommonConstants.SCOPE_PUBLIC)) {
			// send notification event to other affected clients
			// to allow to update their content (if desired)
			Token lEvent;
			// create token of type "event"
			lEvent = TokenFactory.createToken(BaseToken.TT_EVENT);
			lEvent.setNS(NS_FILESYSTEM);
			lEvent.setString("name", "filedeleted");
			lEvent.setString("filename", lFilename);
			lEvent.setString("sourceId", aConnector.getId());
			lServer.broadcastToken(lEvent);
		}

		lServer.sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Gets the file list for a given alias.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void getFilelist(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilelist'...");
		}

		// check if user is allowed to run 'save' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM + ".getFilelist")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResponse = mGetFilelist(aConnector, aToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * Internal file-system listener.
	 */
	class ChangeListener implements FileAlterationListener {

		// Directory changed Event.
		@Override
		public void onDirectoryChange(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory '" + aDirectory.getName()
						+ "' has been changed.");
			}
		}

		// Directory created Event.
		@Override
		public void onDirectoryCreate(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory '" + aDirectory.getName()
						+ "' has been created.");
			}
		}

		//  Directory deleted Event.
		@Override
		public void onDirectoryDelete(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory '" + aDirectory.getName()
						+ "' has been deleted.");
			}
		}

		// File changed Event.
		@Override
		public void onFileChange(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File '" + aFile.getPath()
						+ "' has been changed.");
			}
		}

		// File created Event.
		@Override
		public void onFileCreate(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File '" + aFile.getPath()
						+ "' has been created.");
			}
		}

		// File deleted Event.
		@Override
		public void onFileDelete(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File '" + aFile.getPath()
						+ "' has been deleted.");
			}
		}

		// File system observer started checking event.
		@Override
		public void onStart(FileAlterationObserver aObserver) {
		}

		// File system observer finished checking event.		
		@Override
		public void onStop(FileAlterationObserver aObserver) {
		}
	}

	// inner helper class to just to give the 
	// monitor thread a name for maintainability
	class MonitorThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable aRunnable) {
			Thread lThread = new Thread(aRunnable,
					"jWebSocket FileSystemPlugIn file-system monitor");
			return lThread;
		}
	}

	/**
	 * Starts the aliases file-system monitor.
	 *
	 * @param aInterval Changes checks interval value.
	 */
	public void startAliasesMonitor(long aInterval) {
		if (null == mFileSystemMonitor) {
			mFileSystemMonitor = new FileAlterationMonitor(aInterval);
			mFileSystemMonitor.setThreadFactory(new FileSystemPlugIn.MonitorThreadFactory());

			String lMask = "*";
			IOFileFilter lFileFilter = new WildcardFileFilter(lMask);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting file-system monitor...");
			}

			FileAlterationListener lFileSystemListener = getFileSystemListener();
			Set<String> lAliases = mSettings.getAliases().keySet();
			for (Object lAlias : lAliases) {
				if (lAlias.equals(PRIVATE_ALIAS_DIR_KEY)
						|| lAlias.equals(ALIAS_WEB_ROOT_KEY)) {
					continue;
				}
				// registering file-system listener
				FileAlterationObserver lObserver = new FileAlterationObserver(
						JWebSocketConfig.expandEnvVarsAndProps(
								mSettings.getAliasPath(lAlias.toString())),
						lFileFilter);
				lObserver.addListener(lFileSystemListener);
				mFileSystemMonitor.addObserver(lObserver);
			}
		}
		try {
			// starting file-system monitor...
			mFileSystemMonitor.start();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"starting directory monitor..."));
		}
	}

	/**
	 * Stops the aliases file-system monitor.
	 */
	public void stopAliasesMonitor() {
		if (null != mFileSystemMonitor) {
			try {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Stopping public monitor...");
				}
				mFileSystemMonitor.stop();
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx,
						"stopping directory monitor..."));
			}
		}
	}

	/**
	 * Gets the file-system listener instance.
	 *
	 * @return
	 */
	public FileAlterationListener getFileSystemListener() {
		return new FileSystemPlugIn.ChangeListener();
	}
}
