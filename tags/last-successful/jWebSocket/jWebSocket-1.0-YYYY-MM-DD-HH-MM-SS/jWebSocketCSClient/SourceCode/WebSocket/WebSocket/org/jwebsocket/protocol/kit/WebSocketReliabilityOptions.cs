/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketReliabilityOptions
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
    /// Reliability Options for connection.
    /// </summary>
    public class WebSocketReliabilityOptions
    {
        private bool mAutoReconnect = false;
        private int mReconnectDelay = -1;
        private int mReconnectTimeout = -1;

        public WebSocketReliabilityOptions(bool aAutoReconnect, int aReconnectDelay,
                int aReconnectTimeout)
        {
            mAutoReconnect = aAutoReconnect;
            mReconnectDelay = aReconnectDelay;
            mReconnectTimeout = aReconnectTimeout;
        }

        public bool IsAutoReconnect
        {
            get { return mAutoReconnect; }
            set { mAutoReconnect = value; }
        }

        public int ReconnectDelay
        {
            get { return mReconnectDelay; }
            set { mReconnectDelay = value; }
        }

        public int ReconnectTimeout
        {
            get { return mReconnectTimeout; }
            set { mReconnectTimeout = value; }
        }
 
    }
}
