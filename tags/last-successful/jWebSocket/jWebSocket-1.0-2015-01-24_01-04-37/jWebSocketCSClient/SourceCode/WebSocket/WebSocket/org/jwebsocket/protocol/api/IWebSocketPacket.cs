/*--------------------------------------------------------------------------------
 * jWebSocket - IWebSocketPacket
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
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.protocol.kit;

namespace WebSocket.org.jwebsocket.protocol.api
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Specifies the API for low level data packets which are interchanged between
    /// client and server. Data packets do not have a special format at this
    /// communication level.
    /// </summary>
    public interface IWebSocketPacket
    {
        /// <summary>
        /// Inits the fragmented.
        /// </summary>
        /// <param name="aTotal">Total size.</param>
        void InitFragmented(int aTotal);

        /// <summary>
        /// Sets the fragment.
        /// </summary>
        /// <param name="aString">String fragment.</param>
        /// <param name="aIdx">Idx.</param>
        void SetFragment(string aString, int aIdx);

        /// <summary>
        /// Packs the fragments.
        /// </summary>
        void PackFragments();

        /// <summary>
        /// Sets the value of the data packet to the given string by using
        /// default encoding.
        /// </summary>
        /// <param name="aString">String value.</param>
        void SetString(string aString);

        /// <summary>
        /// Sets the value of the data packet to the given string by using
        /// the passed encoding.
        /// </summary>
        /// <param name="aString">String value.</param>
        /// <param name="aEncoding">Encoding type.</param>
        void SetString(string aString, WebSocketTypeEncoding aEncoding);

        /// <summary>
        /// Sets the value of the data packet to the given string by using
        /// UTF-8 encoding.
        /// </summary>
        /// <param name="aString">String value.</param>
        void SetUTF8(string aString);

        /// <summary>
        /// Sets the value of the data packet to the given string by using
        /// 7 bit US-ASCII encoding.
        /// </summary>
        /// <param name="aString">String value.</param>
        void SetASCII(string aString);

        /// <summary>
        /// Returns the content of the data packet as a string using default
        /// encoding.
        /// </summary>
        /// <returns>Raw Data packet as string with default encoding</returns>
        string GetString();

        /// <summary>
        /// Returns the content of the data packet as a string using the passed
        /// encoding.
        /// </summary>
        /// <param name="aEncoding">Encoding type.</param>
        /// <returns>Raw Data packet as string using passed encoding</returns>
        string GetString(WebSocketTypeEncoding aEncoding);

        /// <summary>
        /// Interprets the data packet as a UTF8 string and returns the string
        /// in UTF-8 encoding.If an exception occurs "null" is returned.
        /// </summary>
        /// <returns>Data packet as UTF-8 string or <c>null</c> if not convertible.</returns>
        string GetUTF8();

        /// <summary>
        /// Interprets the data packet as a US-ASCII string and returns the string
        /// in US-ASCII encoding. If an exception occurs "null" is returned.
        /// </summary>
        /// <returns>Data packet as US-ASCII string or <c>null</c> if not convertible.</returns>
        string GetASCII();

        /// <summary>
        /// Gets or sets data packet.
        /// </summary>
        /// <value>The type of the frame.</value>
        WebSocketFrameType FrameType { get; set; }

        /// <summary>
        /// Gets or sets the byte array.
        /// </summary>
        /// <value>The byte array.</value>
        byte[] ByteArray { get; set; }

        /// <summary>
        /// Gets or sets the fragments.
        /// </summary>
        /// <value>The fragments.</value>
        string[] Fragments { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether this instance is fragmented.
        /// </summary>
        /// <value><c>true</c> if this instance is fragmented; otherwise, <c>false</c>.</value>
        bool IsFragmented { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether this instance is complete.
        /// </summary>
        /// <value><c>true</c> if this instance is complete; otherwise, <c>false</c>.</value>
        bool IsComplete { get; }

        /// <summary>
        /// Gets or sets the creation date.
        /// </summary>
        /// <value>The creation date.</value>
        DateTime CreationDate { get; set; }

        /// <summary>
        /// Gets or sets the timeout.
        /// </summary>
        /// <value>The timeout.</value>
        long Timeout { get; set; }
       
    }
}
