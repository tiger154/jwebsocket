//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Disk Space Single Instance (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.jwebsocket.config.JWebSocketServerConstants;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
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

    /**
     *
     * @param aValue
     * @param aInstance
     * @param aUuid
     * @param aNamesPace
     * @param aQuotaType
     * @param aQuotaIdentifier
     * @param aInstanceType
     * @param aActions
     */
    public QuotaDiskSpaceSI(long aValue, String aInstance, String aUuid,
            String aNamesPace, String aQuotaType, String aQuotaIdentifier,
            String aInstanceType, String aActions) {
        super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
    }

    /**
     *
     */
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
