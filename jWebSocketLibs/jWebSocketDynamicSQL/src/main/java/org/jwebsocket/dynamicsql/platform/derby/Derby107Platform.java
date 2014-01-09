//	---------------------------------------------------------------------------
//	jWebSocket UserAdmin Plug-in (Enterprise Edition, EE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//	All rights reserved, Alexander Schulze, Germany (NRW)
//	---------------------------------------------------------------------------
package org.jwebsocket.dynamicsql.platform.derby;

import java.sql.Types;
import org.apache.ddlutils.platform.derby.DerbyModelReader;
import org.apache.ddlutils.platform.derby.DerbyPlatform;

/**
 *
 * @author markos
 */
public class Derby107Platform extends DerbyPlatform {

    /**
     * Creates a new Derby platform instance. For Derby 10.7 or higher.
     */
    public Derby107Platform() {
        super();
        getPlatformInfo().addNativeTypeMapping(Types.BOOLEAN,
                "BOOLEAN", Types.BOOLEAN);
        setSqlBuilder(new Derby107Builder(this));
        setModelReader(new DerbyModelReader(this));
    }
}
