/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketConvert
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

namespace WebSocket.org.jwebsocket.common
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Convert strings to bytes and viceversa.
    /// </summary>
    public class WebSocketConvert
    {
        /// <summary>
        /// Strings to bytes.
        /// </summary>
        /// <param name="aString">String.</param>
        /// <param name="aEncoding">Encoding.</param>
        /// <returns>Array bytes</returns>
        public static byte[] StringToBytes(string aString, WebSocketTypeEncoding aEncoding)
        {
            try
            {
                byte[] lData;
                switch (aEncoding)
                {
                    case WebSocketTypeEncoding.ASCII:
                        lData = ASCIIEncoding.ASCII.GetBytes(aString);
                        break;
                    case WebSocketTypeEncoding.Unicode:
                        lData = ASCIIEncoding.Unicode.GetBytes(aString);
                        break;
                    case WebSocketTypeEncoding.UTF32:
                        lData = ASCIIEncoding.UTF32.GetBytes(aString);
                        break;
                    case WebSocketTypeEncoding.UTF7:
                        lData = ASCIIEncoding.UTF7.GetBytes(aString);
                        break;
                    case WebSocketTypeEncoding.UTF8:
                        lData = ASCIIEncoding.UTF8.GetBytes(aString);
                        break;
                    default:
                        lData = ASCIIEncoding.Default.GetBytes(aString);
                        break;
                }
                return lData;
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.INVALID_CONVERTING + lEx.Message);
            }
        }

        /// <summary>
        /// Bytes to strings.
        /// </summary>
        /// <param name="aBytes">Bytes.</param>
        /// <param name="aEncoding">Encoding.</param>
        /// <returns>String</returns>
        public static string BytesToString(byte [] aBytes, WebSocketTypeEncoding aEncoding)
        {
            try
            {
                string lString;
                switch (aEncoding)
                {
                    case WebSocketTypeEncoding.ASCII:
                        lString = ASCIIEncoding.ASCII.GetString(aBytes);
                        break;
                    case WebSocketTypeEncoding.Unicode:
                        lString = ASCIIEncoding.Unicode.GetString(aBytes);
                        break;
                    case WebSocketTypeEncoding.UTF32:
                        lString = ASCIIEncoding.UTF32.GetString(aBytes);
                        break;
                    case WebSocketTypeEncoding.UTF7:
                        lString = ASCIIEncoding.UTF7.GetString(aBytes);
                        break;
                    case WebSocketTypeEncoding.UTF8:
                        lString = ASCIIEncoding.UTF8.GetString(aBytes);
                        break;
                    default:
                        lString = ASCIIEncoding.Default.GetString(aBytes);
                        break;
                }
                return lString;
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.INVALID_CONVERTING + lEx.Message);
            }
        }

    }
}
