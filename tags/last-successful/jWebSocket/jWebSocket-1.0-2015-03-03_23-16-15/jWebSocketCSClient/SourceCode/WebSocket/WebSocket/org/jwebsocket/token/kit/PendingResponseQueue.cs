/*--------------------------------------------------------------------------------
 * jWebSocket - PendingResponseQueue
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
using WebSocket.org.jwebsocket.token.api;
using WebSocket.org.jwebsocket.token;

namespace WebSocket.org.jwebsocket.token.kit
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public class PendingResponseQueue
    {
        private IToken mToken = null;
        private PendingResponseEventHandler mPendingResponse;

        public PendingResponseQueue(IToken aToken,PendingResponseEventHandler aPendingResponse)
        {
            mToken = aToken;
            mPendingResponse = aPendingResponse;
        }

        public IToken Token
        {
            get { return mToken; }
            set { mToken = value; }
        }

        public PendingResponseEventHandler PendingResponse
        {
            get { return mPendingResponse; }
            set { mPendingResponse = value; }
        }

    }
}
