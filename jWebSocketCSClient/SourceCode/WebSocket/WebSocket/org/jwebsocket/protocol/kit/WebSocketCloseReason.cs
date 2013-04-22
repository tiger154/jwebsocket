/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketCloseReason
 * Copyright (c) 2013 Rolando Betancourt Toucet
 * -------------------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
 * -------------------------------------------------------------------------------
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WebSocket.org.jwebsocket.protocol.kit
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 1000  indicates a normal closure, meaning that the purpose for
    /// which the connection was established has been fulfilled.
    ///
    /// 1001  indicates that an endpoint is "going away", such as a server
    /// going down or a browser having navigated away from a page.
    ///
    /// 1002  indicates that an endpoint is terminating the connection due
    /// to a protocol error.
    ///
    /// 1004  Reserved.  The specific meaning might be defined in the future.
    ///
    /// 1005  is a reserved value and MUST NOT be set as a status code in a
    /// Close control frame by an endpoint.  It is designated for use in
    /// applications expecting a status code to indicate that no status
    /// code was actually present.
    ///
    /// 1006  is a reserved value and MUST NOT be set as a status code in a
    /// Close control frame by an endpoint.  It is designated for use in
    /// applications expecting a status code to indicate that the
    /// connection was closed abnormally, e.g., without sending or
    /// receiving a Close control frame.
    /// </summary>
    public enum WebSocketCloseReason
    {
        BROKEN = 1001,

        TIMEOUT = 1002,

        SERVER = 1004,

        CLIENT = 1005,

        SHUTDOWN = 1006,
    }
}
