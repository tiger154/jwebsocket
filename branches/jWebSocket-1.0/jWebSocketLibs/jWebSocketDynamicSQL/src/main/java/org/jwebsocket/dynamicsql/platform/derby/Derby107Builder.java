//	---------------------------------------------------------------------------
//	jWebSocket UserAdmin Plug-in (Enterprise Edition, EE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//	All rights reserved, Alexander Schulze, Germany (NRW)
//	---------------------------------------------------------------------------
package org.jwebsocket.dynamicsql.platform.derby;

import java.sql.Types;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.platform.derby.DerbyBuilder;
import org.apache.ddlutils.util.Jdbc3Utils;

/**
 *
 * @author markos
 */
public class Derby107Builder extends DerbyBuilder {

    /**
     * Creates a new builder instance.
     *
     * @param platform The plaftform this builder belongs to
     */
    public Derby107Builder(Platform platform) {
        super(platform);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getNativeDefaultValue(Column column) {
        if ((column.getTypeCode() == Types.BIT)) {
            return getDefaultValueHelper().convert(column.getDefaultValue(), column.getTypeCode(), Types.SMALLINT).toString();
        } else if ((Jdbc3Utils.supportsJava14JdbcTypes() && (column.getTypeCode() == Jdbc3Utils.determineBooleanTypeCode()))) {
            return column.getDefaultValue();
        } else {
            return super.getNativeDefaultValue(column);
        }
    }
}
