//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class Settings {

    // applications directory 
    private File mAppsDirectory;

    /**
     * Gets the map representation <app name, app absolute path> of the apps
     * directory.
     *
     * @return
     */
    public Map<String, String> getApps() {
        Map<String, String> lApps = new HashMap<String, String>();
        File[] lFiles = mAppsDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        for (File lF : lFiles) {
            lApps.put(lF.getName(), lF.getAbsolutePath());
        }

        return lApps;
    }
    private String mAppsDirectoryPath;

    /**
     * Gets the applications directory path.
     *
     * @return
     */
    public String getAppsDirectory() {
        return mAppsDirectoryPath;
    }

    /**
     * Sets the applications directory path.
     *
     * @param aAppsDirectoryPath
     */
    public void setAppsDirectory(String aAppsDirectoryPath) {
        this.mAppsDirectoryPath = Tools.expandEnvVarsAndProps(aAppsDirectoryPath);
    }

    /**
     * Initialize apps directory checkings.
     *
     * @throws Exception
     */
    public void initialize() throws Exception {
        File lDirectory = new File(mAppsDirectoryPath);
        Assert.isTrue(lDirectory.isDirectory(), "The applications directory path does not exists!"
                + " Please check directory path or access permissions.");
        Assert.isTrue(lDirectory.canWrite(), "The Scripting plug-in requires WRITE permissions in the applications directory!");

        mAppsDirectory = lDirectory;
    }
}
