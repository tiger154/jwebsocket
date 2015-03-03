//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutOutputStreamNIOWriter
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ClientLibrary.org.jwebsocket.client.csharp
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
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

        public bool IsAutoReconnect()
        {
            return mAutoReconnect;
        }

        public void SetAutoReconnect(bool aAutoReconnect)
        {
            this.mAutoReconnect = aAutoReconnect;
        }

        public int GetReconnectDelay()
        {
            return mReconnectDelay;
        }

        public void SetReconnectDelay(int aReconnectDelay)
        {
            this.mReconnectDelay = aReconnectDelay;
        }

        public int GetReconnectTimeout()
        {
            return mReconnectTimeout;
        }

        public void SetReconnectTimeout(int aReconnectTimeout)
        {
            this.mReconnectTimeout = aReconnectTimeout;
        }
    }
}
