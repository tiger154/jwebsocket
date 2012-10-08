//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Filesystem Plug-In
//	Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.plugins.filesystem;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.codec.binary.Base64;
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
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class FileSystemPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	public static final String NS_FILESYSTEM = JWebSocketServerConstants.NS_BASE + ".plugins.filesystem";
	public static final String PRIVATE_ALIAS_DIR_KEY = "privateDir";
	public static final String PUBLIC_ALIAS_DIR_KEY = "publicDir";
	public static final String ALIAS_WEB_ROOT_KEY = "webRoot";
	public static final String PRIVATE_ALIAS_DIR_DEF = "${" + JWebSocketServerConstants.JWEBSOCKET_HOME + "}/filesystem/private/{username}/";
	public static final String PUBLIC_ALIAS_DIR_DEF = "${" + JWebSocketServerConstants.JWEBSOCKET_HOME + "}/filesystem/public/";
	public static final String ALIAS_WEB_ROOT_DEF = "http://localhost/public/";
	private FileAlterationMonitor mFileSystemMonitor = null;
	protected ApplicationContext mBeanFactory;
	protected Settings mSettings;

	/**
	 *
	 * @param aConfiguration
	 */
	public FileSystemPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating FileSystem plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_FILESYSTEM);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for filesystem plug-in, some features may not be available.");
			} else {
				mBeanFactory = getConfigBeanFactory();
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.filesystem.settings");

				// setting core aliases (private, public and webRoot)
				String lPrivateAlias = getString("alias:" + PRIVATE_ALIAS_DIR_KEY, PRIVATE_ALIAS_DIR_DEF);
				String lPublicAlias = getString("alias:" + PUBLIC_ALIAS_DIR_KEY, PUBLIC_ALIAS_DIR_DEF);
				String lWebRootAlias = getString("alias:" + ALIAS_WEB_ROOT_KEY, ALIAS_WEB_ROOT_DEF);

				mSettings.getAliases().put(PRIVATE_ALIAS_DIR_KEY, lPrivateAlias);
				mSettings.getAliases().put(PUBLIC_ALIAS_DIR_KEY, lPublicAlias);
				mSettings.getAliases().put(ALIAS_WEB_ROOT_KEY, lWebRootAlias);

				if (mLog.isInfoEnabled()) {
					mLog.info("Filesystem plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating filesystem plug-in"));
			throw lEx;
		}
	}

	@Override
	public synchronized void engineStarted(WebSocketEngine aEngine) {
		if (getServer().getEngines().size() == 1) {
			startAliasesMonitor(1000);
		}
	}

	@Override
	public synchronized void engineStopped(WebSocketEngine aEngine) {
		if (getServer().getEngines().size() == 1) {
			stopAliasesMonitor();
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
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
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("getFilelist")) {
				return getFilelist(aConnector.getUsername(), aToken);
			} else if (lType.equals("getAliasPath")) {
				String lTargetAlias = aToken.getString("alias");
				Token lToken = TokenFactory.createToken();
				lToken.setString("aliasPath", getAliasPath(aConnector, lTargetAlias));

				return lToken;
			}
		}

		return null;
	}

	public String getAliasPath(WebSocketConnector aConnector, String aAlias) {
		String lBaseDir = mSettings.getAliasPath(aAlias);
		lBaseDir = JWebSocketConfig.expandEnvAndJWebSocketVars(lBaseDir);

		if (aAlias.equals(PRIVATE_ALIAS_DIR_KEY)) {
			lBaseDir = lBaseDir.replace("{username}", aConnector.getUsername());
		}

		return lBaseDir;
	}

	/**
	 * Indicates if a file exists in a given alias
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
		if (hasAuthority(aConnector, NS_FILESYSTEM + "exists")) {
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
		}

		// getting alias
		String lFolder = getAliasPath(aConnector, lAlias);
		File lFile = null;
		if (lFolder != null) {
			lFile = new File(lFolder + "/" + lFilename);
		} else {
			sendErrorToken(aConnector, aToken, -1, "The given alias '" + lAlias + "' does not exists!");
		}

		// file exists?
		boolean lExists = lFile.exists();

		Token lResponse = createResponse(aToken);
		lResponse.setBoolean("exists", lExists);

		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * saves a file
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void save(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'save'...");
		}

		// check if user is allowed to run 'save' command
		if (!hasAuthority(aConnector, NS_FILESYSTEM + ".save")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		// obtain required parameters for file load operation
		String lFilename = aToken.getString("filename");
		String lScope = aToken.getString("scope", JWebSocketCommonConstants.SCOPE_PRIVATE);

		// scope may be "private" or "public"
		String lBaseDir;
		if (JWebSocketCommonConstants.SCOPE_PRIVATE.equals(lScope)) {
			lBaseDir = getAliasPath(aConnector, PRIVATE_ALIAS_DIR_KEY);
		} else if (JWebSocketCommonConstants.SCOPE_PUBLIC.equals(lScope)) {
			lBaseDir = getAliasPath(aConnector, PUBLIC_ALIAS_DIR_KEY);
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
			sendErrorToken(aConnector, aToken, -1, "Argument 'data' contains null value!");
			return;
		}

		String lEncoding = aToken.getString("encoding", "base64");
		byte[] lBA = null;
		try {
			if ("base64".equals(lEncoding)) {
				int lIdx = lData.indexOf(',');
				if (lIdx >= 0) {
					lData = lData.substring(lIdx + 1);
				}
				lBA = Base64.decodeBase64(lData);
			} else {
				lBA = lData.getBytes("UTF-8");
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "decoding file"));
		}

		String lFullPath = lBaseDir + lFilename;
		File lFile = new File(lFullPath);
		try {
			checkForSave(aConnector, lFile);
			// prevent two threads at a time writing to the same file
			synchronized (this) {
				// force create folder if not yet exists
				File lDir = new File(FilenameUtils.getFullPath(lFullPath));
				FileUtils.forceMkdir(lDir);
				if (lBA != null) {
					FileUtils.writeByteArrayToFile(lFile, lBA);
				} else {
					FileUtils.writeStringToFile(lFile, lData, "UTF-8");
				}
			}
		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			lMsg = lEx.getClass().getSimpleName() + " on save: " + lEx.getMessage();
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
		lEvent.setString("name", "filesaved");
		lEvent.setString("filename", lFilename);
		lEvent.setString("sourceId", aConnector.getId());

		if (lNotify && lScope.equals(JWebSocketCommonConstants.SCOPE_PUBLIC)) {
			lServer.broadcastToken(lEvent);
		} else {
			// notify always the requester
			lServer.sendToken(aConnector, lEvent);
		}
	}

	/**
	 * Allows to perform actions and checks before save a file.
	 *
	 * @param lFile
	 */
	protected void checkForSave(WebSocketConnector aConnector, File lFile) throws Exception {
		// TODO: to be overwritten for enterprise filesystem plug-in
	}

	/**
	 * load a file
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
		String lData = "";

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lBaseDir = getAliasPath(aConnector, lAlias);
		if (null == lBaseDir) {
			sendErrorToken(aConnector, aToken, -1, "The given alias '" + lAlias
					+ "' does not exists!");
			return;
		}

		File lFile = new File(lBaseDir + lFilename);
		if (!lFile.exists()) {
			sendErrorToken(aConnector, aToken, -1, "The file '" + lFilename
					+ "' does not exists in the given alias!");
			return;
		}

		byte[] lBA;
		try {
			lBA = FileUtils.readFileToByteArray(lFile);
			if (lBA != null && lBA.length > 0) {
				lData = new String(Base64.encodeBase64(lBA), "UTF-8");
			}
			lResponse.setString("data", lData);
		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			lMsg = lEx.getClass().getSimpleName() + " on load: " + lEx.getMessage();
			lResponse.setString("msg", lMsg);
			mLog.error(lMsg);
		}

		// send response to requester
		lResponse.setBoolean("decode", aToken.getBoolean("decode", false));
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * send a file from one client to another client
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

	protected Token getFilelist(String aUsername, Token aToken) {

		String lAlias = aToken.getString("alias");
		boolean lRecursive = aToken.getBoolean("recursive", false);
		List lFilemasks = aToken.getList("filemasks");

		Object lObject;
		String lFolder;
		Token lToken = TokenFactory.createToken();

		lObject = mSettings.getAliasPath(lAlias);
		if (lObject != null) {
			lFolder = (String) lObject;
			lFolder = JWebSocketConfig.expandEnvAndJWebSocketVars(lFolder).
					replace("{username}", aUsername);
			File lDir = new File(lFolder);
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
			Collection<File> lFiles = FileUtils.listFiles(lDir, lFileFilter, lDirFilter);
			List lFileList = new FastList<Map>();
			for (File lFile : lFiles) {
				Map lFileData = new FastMap< String, Object>();
				String lName = lFile.getName();
				lFileData.put("filename", lName);
				lFileData.put("size", lFile.length());
				lFileData.put("modified", Tools.DateToISO8601(new Date(lFile.lastModified())));
				lFileData.put("hidden", lFile.isHidden());
				lFileData.put("canRead", lFile.canRead());
				lFileData.put("canWrite", lFile.canWrite());
				if (lAlias.equals(PRIVATE_ALIAS_DIR_KEY)) {
					lFileData.put("url", getString(ALIAS_WEB_ROOT_KEY, ALIAS_WEB_ROOT_DEF) + lName);
				}

				lFileList.add(lFileData);
			}
			lToken.setList("files", lFileList);
			lToken.setInteger("code", 0);
			lToken.setString("msg", "ok");
		} else {
			lToken.setInteger("code", -1);
			lToken.setString("msg", "No alias '" + lAlias + "' defined for filesystem plug-in");
		}

		return lToken;
	}

	/**
	 * Delete a file
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void delete(WebSocketConnector aConnector, Token aToken) {
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

		String lBaseDir = getAliasPath(aConnector, PRIVATE_ALIAS_DIR_KEY);
		String lFilePath = lBaseDir + lFilename;
		File lFile = new File(lFilePath);

		if (null == lFilename || !lFile.exists()) {
			sendErrorToken(aConnector, aToken, -1, "The given filename '" + lFilename + "' is invalid or not exists!");
			return;
		}

		try {
			if (lForce) {
				FileUtils.forceDelete(lFile);
			} else {
				if (!FileUtils.deleteQuietly(lFile)) {
					throw new Exception("File could not be deleted quietly!");
				}
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "File '" + lFilename + "' could not be deleted!");
			return;
		}

		getServer().sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Gets the file list for a given alias
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

		Token lResponse = getFilelist(aConnector.getUsername(), aToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	class ChangeListener implements FileAlterationListener {

		// Directory changed Event.
		@Override
		public void onDirectoryChange(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory '" + aDirectory.getName() + "' has been changed.");
			}
		}

		// Directory created Event.
		@Override
		public void onDirectoryCreate(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory '" + aDirectory.getName() + "' has been created.");
			}
		}

		//  Directory deleted Event.
		@Override
		public void onDirectoryDelete(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory '" + aDirectory.getName() + "' has been deleted.");
			}
		}

		// File changed Event.
		@Override
		public void onFileChange(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File '" + aFile.getPath() + "' has been changed.");
			}
		}

		// File created Event.
		@Override
		public void onFileCreate(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File '" + aFile.getPath() + "' has been created.");
			}
		}

		// File deleted Event.
		@Override
		public void onFileDelete(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File '" + aFile.getPath() + "' has been deleted.");
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
			Thread lThread = new Thread(aRunnable, "jWebSocket FileSystemPlugIn file-system monitor");
			return lThread;
		}
	}

	public void startAliasesMonitor(int aInterval) {
		if (null == mFileSystemMonitor) {
			mFileSystemMonitor = new FileAlterationMonitor(aInterval);
			mFileSystemMonitor.setThreadFactory(new MonitorThreadFactory());

			String lMask = "*";
			IOFileFilter lFileFilter = new WildcardFileFilter(lMask);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting file-system monitor...");
			}

			FileAlterationListener lFileSystemListener = getFileSystemListener();
			Set lAliases = mSettings.getAliases().keySet();
			for (Object lAlias : lAliases) {
				if (lAlias.equals(PRIVATE_ALIAS_DIR_KEY) || lAlias.equals(ALIAS_WEB_ROOT_KEY)) {
					continue;
				}
				// registering file-system listener
				FileAlterationObserver lObserver = new FileAlterationObserver(
						JWebSocketConfig.expandEnvAndJWebSocketVars(mSettings.getAliasPath(lAlias.toString())),
						lFileFilter);
				lObserver.addListener(lFileSystemListener);
				mFileSystemMonitor.addObserver(lObserver);
			}
		}
		try {
			// starting file-system monitor...
			mFileSystemMonitor.start();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "starting directory monitor..."));
		}
	}

	public void stopAliasesMonitor() {
		if (null != mFileSystemMonitor) {
			try {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Stopping public monitor...");
				}
				mFileSystemMonitor.stop();
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping directory monitor..."));
			}
		}
	}

	public FileAlterationListener getFileSystemListener() {
		return new ChangeListener();
	}
}
