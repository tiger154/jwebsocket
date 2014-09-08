//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Disk Space (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.definitions;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import static org.jwebsocket.plugins.quota.definitions.BaseQuota.mLog;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaDiskSpaceSI;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaDiskSpace extends BaseQuota {

	/**
	 *
	 */
	public static final String JWS_HOME = JWebSocketServerConstants.JWEBSOCKET_HOME;
    /**
     * Private directory.
     */
    public static final String PRIVATE_DIR_DEF = JWS_HOME + "/filesystem/private/";
    /**
     * Public directory.
     */
    public static final String PUBLIC_DIR_DEF = JWS_HOME + "/filesystem/public/";
    /**
     * Value for the private names_space
     */
    public static final String PRIVATE_NAMESPACE = "private";
    /**
     * Value for the public names_space
     */
    public static final String PUBLIC_NAMESPACE = "public";

    @Override
    public long reduceQuota(String aInstance, String aNameSpace,
            String aInstanceType, String aActions, long aAmount) {

        long lQuota = getQuota(aInstance, aNameSpace, aInstanceType, aActions).getvalue();
        if (lQuota <= 0) {
            return -1;
        }

        String lFolder = "";
        if (aNameSpace.equals(PRIVATE_NAMESPACE)) {
            lFolder = PRIVATE_DIR_DEF + aInstance;
        } else if (aNameSpace.equals(PUBLIC_NAMESPACE)) {
            lFolder = PUBLIC_DIR_DEF;
        }

        long lUsedSpace = 0;
        try {
            lUsedSpace = getDirectorySpace(lFolder);
        } catch (Exception ex) {
            mLog.error(ex.getMessage());
        }
        long lAvailableSpace = evaluateQuotaDisk(lQuota, lUsedSpace, aAmount);
        return lAvailableSpace;
    }

    @Override
    public long reduceQuota(String aUuid, long aAmount) {

        long lQuota = getQuota(aUuid).getvalue();

        if (lQuota <= 0) {
            return -1;
        }

        IQuotaSingleInstance lQuotaSI = this.mQuotaStorage.getQuotaByUuid(aUuid);

        long lAvailableSpace = reduceQuota(lQuotaSI.getInstance(),
                lQuotaSI.getNamespace(), lQuotaSI.getInstanceType(),
                lQuotaSI.getActions(), aAmount);

        return lAvailableSpace;
    }

    private long evaluateQuotaDisk(long aQuota, long aUsedSpace, long aAmount) {

        long lUsedSpace = inMeasureMB(aUsedSpace);
        long lFreeSpace = aQuota - lUsedSpace;
        if (lFreeSpace < aAmount) {
            return -1;
        } else {
            return lFreeSpace - aAmount;
        }
    }

    private long getDirectorySpace(String aDirectory) throws Exception {

        File lFiles = new File(aDirectory);
        long lUsedSpace = 0;
        if (lFiles.exists() && lFiles.isDirectory()) {
            lUsedSpace = FileUtils.sizeOfDirectory(lFiles);
        } else {
            throw new Exception("Directory: " + aDirectory + " not found");
        }
        return lUsedSpace;
    }

    private long inMeasureMB(long value) {
        return (value / 1000 / 1000); //"in MB"
    }

    @Override
    public void create(String aInstance, String aNameSpace, String aUuid, long aAmount,
            String aInstanceType, String aQuotaType, String aQuotaIdentifier, String aActions) throws Exception {

        if (!aNameSpace.equals(PRIVATE_NAMESPACE) && !aNameSpace.equals(PUBLIC_NAMESPACE)) {
            throw new Exception("The quota diskspace just accept as namespace:("
                    + PRIVATE_NAMESPACE + " and " + PUBLIC_NAMESPACE + "), " + aNameSpace + " is not a valid"
                    + "namespace for a quotaDiskSpace");
        }

        if (mQuotaStorage.quotaExist(aNameSpace, aQuotaIdentifier, aInstance, aActions)) {

            throw new ExceptionQuotaAlreadyExist(mQuotaStorage.
                    getUuid(aQuotaIdentifier, aNameSpace, aInstance,
                            aInstanceType, aActions));
        }

        IQuotaSingleInstance lSingleQuota;
        lSingleQuota = new QuotaDiskSpaceSI(aAmount, aInstance, aUuid, aNameSpace, aQuotaType,
                aQuotaIdentifier, aInstanceType, aActions);
        mQuotaStorage.save(lSingleQuota);

    }
}
