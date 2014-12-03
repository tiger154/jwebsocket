/*--------------------------------------------------------------------------------
 * jWebSocket - IWebSocketCookiesManager
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
using System.Web;

namespace WebSocket.org.jwebsocket.protocol.api
{
    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// API for Cookie Manage
    /// </summary>
    public interface IWebSocketCookiesManager
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="aCookies"></param>
        /// <param name="aUri"></param>
        void AddCookies(List<string> aCookies, Uri aUri);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="aUri"></param>
        /// <returns></returns>
        HttpCookieCollection GetCookies(Uri aUri);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="aCookies"></param>
        /// <returns></returns>
        string ProcessCookies(HttpCookieCollection aCookies);
    }
}
