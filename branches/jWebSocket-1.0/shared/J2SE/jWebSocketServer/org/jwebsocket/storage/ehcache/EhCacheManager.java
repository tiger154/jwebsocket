//	---------------------------------------------------------------------------
//	jWebSocket - EhCacheManager (Community Edition, CE)
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
package org.jwebsocket.storage.ehcache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class EhCacheManager {

    private static CacheManager mInstance = null;
    private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 */
	public static void shutdown() {
        if (null != mInstance) {
            getInstance().shutdown();
        }
    }

    /**
     * Default constructor, cannot be called from outside this class.
     */
    private EhCacheManager() {
    }

    /**
     * Static method, returns the one and only instance
     *
     * @return
     */
    public static CacheManager getInstance() {
        if (mInstance == null) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("Instantiating EhCache Manager...");
            }
            ClassLoader lClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                String lContent = FileUtils.readFileToString(
                        new File(JWebSocketConfig.getConfigFolder("ehcache.xml", lClassLoader)), "UTF-8");
                lContent = JWebSocketConfig.expandEnvVarsAndProps(lContent);
                mInstance = new CacheManager(new ByteArrayInputStream(lContent.getBytes("UTF-8")));
                if (mLog.isInfoEnabled()) {
                    mLog.info("EhCache Manager successfully instantiated, "
                            + mInstance.getCacheNames().length
                            + " caches configured at '"
                            + mInstance.getDiskStorePath() + "'.");
                }
            } catch (IOException lIOEx) {
                mLog.error(Logging.getSimpleExceptionMessage(lIOEx, "Instantiating EhCache Manager"));
            }
        }
        return mInstance;
    }

    /**
     * Static method, returns the one and only instance
     *
     * @param aName
     * @return
     */
    public static Cache getCache(String aName) {
        return getInstance().getCache(aName);
    }
}
