/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketStateOfStatus
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
    /// Status of the connection.
    /// </summary>
    public class WebSocketStateOfStatus
    {
        public static bool isWritable(WebSocketStatus aStatus)
        {
            return (aStatus.Equals(WebSocketStatus.OPENED));
        }

        public static bool isConnected(WebSocketStatus aStatus)
        {
            return (aStatus.Equals(WebSocketStatus.OPENED));
        }

        public static bool isClosable(WebSocketStatus aStatus)
        {
            return (aStatus.Equals(WebSocketStatus.CLOSED));
        }

        public static bool isClosed(WebSocketStatus aStatus)
        {
            return (aStatus.Equals(WebSocketStatus.CLOSED));
        }

    }
}
