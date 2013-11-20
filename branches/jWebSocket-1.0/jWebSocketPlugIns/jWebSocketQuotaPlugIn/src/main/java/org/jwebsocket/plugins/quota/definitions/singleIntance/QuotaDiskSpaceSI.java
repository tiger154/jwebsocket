/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.jwebsocket.config.JWebSocketServerConstants;

/**
 *
 * @author osvaldo
 */
public class QuotaDiskSpaceSI extends QuotaBaseInstance {

	private File[] mRoots;
	/**
	 * Private alias directory default value.
	 */
	public static final String PRIVATE_DIR_DEF = "${"
			+ JWebSocketServerConstants.JWEBSOCKET_HOME + "}/filesystem/private/";
	/**
	 * Public alias directory default value.
	 */
	public static final String PUBLIC_ALIAS_DIR_DEF = "${"
			+ JWebSocketServerConstants.JWEBSOCKET_HOME + "}/filesystem/public/";

	public QuotaDiskSpaceSI(long aValue, String aInstance, String aUuid,
			String aNamesPace, String aQuotaType, String aQuotaIdentifier,
			String aInstanceType, String aActions) {
		super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
	}

	public void testing() {

		File lFiles = new File("/home/svn/ijwssvn/rte/jWebSocket-1.0/filesystem/");
		mRoots = lFiles.listFiles();

		long lTotalHddSpace = 0;
		long lSpace = 0;

		for (File lRoot : mRoots) {

			if (lRoot.isDirectory()) {
				lSpace = FileUtils.sizeOfDirectory(lRoot);
				lTotalHddSpace += lSpace;
			}

		}

	}

	//for convert the hdd space
	private String inMeasure(long value) {
		if (value / 1000 < 1) {
			return String.valueOf(value) + " KB";
		} else if (value / 1000 / 1000 < 1) {
			return String.valueOf(value / 1000) + " MB";
		} else {
			return String.valueOf(value / 1000 / 1000 / 1000) + " GB";
		}
	}
}
