/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketTimeout
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
using System.Threading;
using System.IO;
using System.Net.Sockets;
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.common;

namespace WebSocket.org.jwebsocket.protocol.kit
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public class WebSocketTimeout
    {
        private object mWriteLock = new object();

        /// <summary>
        /// Calls the with timeout.
        /// </summary>
        /// <param name="aAction">Action.</param>
        /// <param name="aTimeoutMilliseconds">Timeout milliseconds.</param>
        /// <param name="aIn">Network Stream.</param>
        public static void CallWithTimeout(Action<NetworkStream> aAction, int aTimeoutMilliseconds, NetworkStream aIn)
        {
            Thread lThreadToKill = null;
            Action lWrappedAction = () =>
            {
                lock (aIn)
                {
                    lThreadToKill = Thread.CurrentThread;
                    aAction(aIn);
                } 
            };

            IAsyncResult lResult = lWrappedAction.BeginInvoke(null, null);
            if (lResult.AsyncWaitHandle.WaitOne(aTimeoutMilliseconds))
            {
                lWrappedAction.EndInvoke(lResult);
            }
            else
            {
                lThreadToKill.Abort();
                throw new TimeoutException(WebSocketMessage.TIMEOUT);
            }
        }

        /// <summary>
        /// Calls the with timeout.
        /// </summary>
        /// <param name="aAction">Action.</param>
        /// <param name="aTimeoutMilliseconds">Timeout milliseconds.</param>
        /// <param name="lPacket">WebSocket Packet.</param>
        public static void CallWithTimeout(Action<IWebSocketPacket> aAction, int aTimeoutMilliseconds, IWebSocketPacket lPacket)
        {
            Thread lThreadToKill = null;
            Action lWrappedAction = () =>
            {
                lThreadToKill = Thread.CurrentThread;
                aAction(lPacket);
            };

            IAsyncResult lResult = lWrappedAction.BeginInvoke(null, null);
            if (lResult.AsyncWaitHandle.WaitOne(aTimeoutMilliseconds))
            {
                lWrappedAction.EndInvoke(lResult);
            }
            else
            {
                lThreadToKill.Abort();
                throw new TimeoutException(WebSocketMessage.TIMEOUT);
            }
        }

        /// <summary>
        /// Calls the with timeout.
        /// </summary>
        /// <param name="aAction">Action.</param>
        /// <param name="aTimeoutMilliseconds">Timeout milliseconds.</param>
        /// <param name="lHeaders"> WebSocket Headers.</param>
        public static void CallWithTimeout(Action<WebSocketHeaders> aAction, int aTimeoutMilliseconds, WebSocketHeaders lHeaders)
        {
            Thread lThreadToKill = null;
            Action lWrappedAction = () =>
            {
                lThreadToKill = Thread.CurrentThread;
                aAction(lHeaders);
            };

            IAsyncResult lResult = lWrappedAction.BeginInvoke(null, null);
            if (lResult.AsyncWaitHandle.WaitOne(aTimeoutMilliseconds))
            {
                lWrappedAction.EndInvoke(lResult);
            }
            else
            {
                lThreadToKill.Abort();
                throw new TimeoutException(WebSocketMessage.TIMEOUT);
            }
        }
    }
}
