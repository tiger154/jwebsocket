//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Filesystem Plug-In
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
//	THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 */
public class FileSystemPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_FILESYSTEM = JWebSocketServerConstants.NS_BASE + ".plugins.filesystem";
	// TODO: make these settings configurable
	private static String PRIVATE_DIR_KEY = "alias:privateDir";
	private static String PUBLIC_DIR_KEY = "alias:publicDir";
	private static String WEB_ROOT_KEY = "alias:webRoot";
	private static String PRIVATE_DIR_DEF = "${" + JWebSocketServerConstants.JWEBSOCKET_HOME + "}/private/{username}/";
	private static String PUBLIC_DIR_DEF = "${" + JWebSocketServerConstants.JWEBSOCKET_HOME + "}/public/";
	private static String WEB_ROOT_DEF = "http://jwebsocket.org/";
	private static FileAlterationMonitor mPublicMonitor = null;
	
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;

	/**
	 * 
	 * @param aConfiguration
	 */
	public FileSystemPlugIn(PluginConfiguration aConfiguration) {
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
				mSettings = (Settings) mBeanFactory.getBean("settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("Filesystem plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating filesystem plug-in"));
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		startPublicMonitor(1000);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		stopPublicMonitor();
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// select from database
			if (lType.equals("save")) {
				save(aConnector, aToken);
			} else if (lType.equals("load")) {
				load(aConnector, aToken);
			} else if (lType.equals("send")) {
				send(aConnector, aToken);
			} else if (lType.equals("getFilelist")) {
				getFilelist(aConnector, aToken);
			} else if (lType.equals("watch")) {
				watch(aConnector, aToken);
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("getFilelist")) {
				return getFilelist(aToken);
			}
		}
		return null;
	}

	/**
	 * save a file
	 * @param aConnector
	 * @param aToken
	 */
	private void save(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'save'...");
		}

		// check if user is allowed to run 'save' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_FILESYSTEM + ".save")) {
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
			String lUsername = getUsername(aConnector);
			lBaseDir = getString(PRIVATE_DIR_KEY, PRIVATE_DIR_DEF);
			if (lUsername != null) {
				lBaseDir = JWebSocketConfig.expandEnvAndJWebSocketVars(lBaseDir).replace("{username}", lUsername);
			} else {
				lMsg = "not authenticated to save private file";
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMsg);
				}
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lMsg);
				// send error response to requester
				lServer.sendToken(aConnector, lResponse);
				return;
			}
		} else if (JWebSocketCommonConstants.SCOPE_PUBLIC.equals(lScope)) {
			lBaseDir = JWebSocketConfig.expandEnvAndJWebSocketVars(getString(PUBLIC_DIR_KEY, PUBLIC_DIR_DEF));
		} else {
			lMsg = "invalid scope";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			// send error response to requester
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		Boolean lNotify = aToken.getBoolean("notify", false);
		String lData = aToken.getString("data");
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
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "saving file"));
		}

		// complete the response token
		String lFullPath = lBaseDir + lFilename;
		File lFile = new File(lFullPath);
		try {
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
		if (lNotify) {
			// create token of type "event"
			Token lEvent = TokenFactory.createToken(BaseToken.TT_EVENT);
			// include name space of this plug-in
			lEvent.setNS(NS_FILESYSTEM);
			lEvent.setString("name", "filesaved");
			lEvent.setString("filename", lFilename);
			lEvent.setString("sourceId", aConnector.getId());
			lEvent.setString("url", getString(WEB_ROOT_KEY, WEB_ROOT_DEF) + lFilename);
			// TODO: Limit notification to desired scope
			lServer.broadcastToken(lEvent);
		}
	}

	/**
	 * load a file
	 * @param aConnector
	 * @param aToken
	 */
	private void load(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'load'...");
		}

		// check if user is allowed to run 'load' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_FILESYSTEM + ".load")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// obtain required parameters for file load operation
		String lFilename = aToken.getString("filename");
		String lScope = aToken.getString("scope", JWebSocketCommonConstants.SCOPE_PRIVATE);
		String lData = "";

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lBaseDir;
		if (JWebSocketCommonConstants.SCOPE_PRIVATE.equals(lScope)) {
			String lUsername = getUsername(aConnector);
			lBaseDir = getString(PRIVATE_DIR_KEY, PRIVATE_DIR_DEF);
			if (lUsername != null) {
				lBaseDir = JWebSocketConfig.expandEnvAndJWebSocketVars(lBaseDir).replace("{username}", lUsername);
			} else {
				lMsg = "not authenticated to load private file";
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMsg);
				}
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lMsg);
				// send error response to requester
				lServer.sendToken(aConnector, lResponse);
				return;
			}
		} else if (JWebSocketCommonConstants.SCOPE_PUBLIC.equals(lScope)) {
			lBaseDir = JWebSocketConfig.expandEnvAndJWebSocketVars(getString(PUBLIC_DIR_KEY, PUBLIC_DIR_DEF));
		} else {
			lMsg = "invalid scope";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			// send error response to requester
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		// complete the response token
		File lFile = new File(lBaseDir + lFilename);
		byte[] lBA = null;
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
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * send a file from one client to another client
	 * @param aConnector
	 * @param aToken
	 */
	private void send(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'send'...");
		}

		// check if user is allowed to run 'save' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_FILESYSTEM + ".send")) {
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
			lEvent.setString("name", "filesent");
			lEvent.setString("filename", lFilename);
			lEvent.setString("sourceId", aConnector.getId());
			lEvent.setString("data", lData);
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

	private Token getFilelist(Token aToken) {

		String lAlias = aToken.getString("alias");
		boolean lRecursive = aToken.getBoolean("recursive", false);
		List lFilemasks = aToken.getList("filemasks");

		Object lObject = null;
		String lFolder = null;
		Token lToken = TokenFactory.createToken();

		if (mSettings != null) {
			lObject = mSettings.getAlias(lAlias);
			if (lObject != null) {
				lFolder = (String) lObject;
				File lDir = new File(JWebSocketConfig.expandEnvAndJWebSocketVars(lFolder));
				lFolder = lDir.getPath();
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
					String lPath = lFile.getPath();
					if (lPath != null && lPath.indexOf(lFolder) == 0) {
						lPath = lPath.substring(lFolder.length() + 1, lPath.length() - lName.length());
					}
					lFileData.put("path", lPath);
					lFileData.put("size", lFile.length());
					lFileData.put("modified", Tools.DateToISO8601(new Date(lFile.lastModified())));
					lFileData.put("hidden", lFile.isHidden());
					lFileData.put("canRead", lFile.canRead());
					lFileData.put("canWrite", lFile.canWrite());
					lFileList.add(lFileData);
				}
				lToken.setList("files", lFileList);
				lToken.setInteger("code", 0);
				lToken.setString("msg", "ok");
			} else {
				lToken.setInteger("code", -1);
				lToken.setString("msg", "no alias '" + lAlias + "' defined for filesystem plug-in");
			}
		} else {
			lToken.setInteger("code", -1);
			lToken.setString("msg", "no settings defined for filesystem plug-in");
		}
		return lToken;
	}

	private void getFilelist(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilelist'...");
		}

		// check if user is allowed to run 'save' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_FILESYSTEM + ".getFilelist")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResponse = getFilelist(aToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	class ChangeListener implements FileAlterationListener {

		// Directory changed Event.
		@Override
		public void onDirectoryChange(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory " + aDirectory.getName() + " has been changed.");
			}
		}

		// Directory created Event.
		@Override
		public void onDirectoryCreate(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory " + aDirectory.getName() + " has been created.");
			}
		}

		//  Directory deleted Event.
		@Override
		public void onDirectoryDelete(File aDirectory) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory " + aDirectory.getName() + " has been deleted.");
			}
		}

		// File changed Event.
		@Override
		public void onFileChange(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File " + aFile.getName() + " has been changed.");
			}
		}

		// File created Event.
		@Override
		public void onFileCreate(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File " + aFile.getName() + " has been created.");
			}
		}

		// File deleted Event.
		@Override
		public void onFileDelete(File aFile) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("File " + aFile.getName() + " has been deleted.");
			}
		}

		// File system observer started checking event.
		@Override
		public void onStart(FileAlterationObserver aObserver) {
			/*
			if (mLog.isDebugEnabled()) {
			mLog.debug("Monitor has been started.");
			}
			 */
		}

		// File system observer finished checking event.		
		@Override
		public void onStop(FileAlterationObserver aObserver) {
			/*
			if (mLog.isDebugEnabled()) {
			mLog.debug("Monitor has been stopped.");
			}
			 */
		}
	}

	// inner helper class to just to give the 
	// monitor thread a name for maintainability
	class MonitorThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable aRunnable) {
			Thread lThread = new Thread(aRunnable, "jWebSocket FileSystemPlugIn Monitor");
			return lThread;
		}
	}

	/**
	 * 
	 * @param aInterval
	 */
	public void startPublicMonitor(int aInterval) {
		if (null == mPublicMonitor) {
			mPublicMonitor = new FileAlterationMonitor(aInterval);
			mPublicMonitor.setThreadFactory(new MonitorThreadFactory());
			String lBaseDir = JWebSocketConfig.expandEnvAndJWebSocketVars(getString(PUBLIC_DIR_KEY, PUBLIC_DIR_DEF));
			String lMask = "*";
			IOFileFilter lFileFilter = new WildcardFileFilter(lMask);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting public monitor for " + lBaseDir + ", files:" + lMask + "...");
			}
			FileAlterationObserver lObserver = new FileAlterationObserver(lBaseDir, lFileFilter);
			FileAlterationListener lListener = new ChangeListener();
			lObserver.addListener(lListener);
			mPublicMonitor.addObserver(lObserver);
		}
		try {
			mPublicMonitor.start();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "starting monitor"));
		}
	}

	/**
	 * 
	 */
	public void stopPublicMonitor() {
		if (null != mPublicMonitor) {
			try {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Stopping public monitor...");
				}
				mPublicMonitor.stop();
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping monitor"));
			}
		}
	}

	private void watch(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'watch'...");
		}

		// check if user is allowed to run 'save' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_FILESYSTEM + ".watch")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		String lPath = aToken.getString("path");
		String lFilename = aToken.getString("filename");

		Token lResponse = createResponse(aToken);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
