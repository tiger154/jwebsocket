/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketConstants
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
using System.Xml;
using log4net;
using WebSocket.org.jwebsocket.protocol.kit;



namespace WebSocket.org.jwebsocket.common
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Contains all constants of the client.
    /// </summary>
    public sealed class WebSocketConstants
    {
        /// <summary>
        /// Logger.
        /// </summary>
        private static readonly ILog mLog = LogManager.GetLogger(typeof(WebSocketConstants).Name);

        /// <summary>
        /// Use text format as default encoding for WebSocket Packets if not explicitly specified.
        /// </summary>
        public static readonly WebSocketEncoding WS_ENCODING_DEFAULT = WebSocketEncoding.TEXT;

        /// <summary>
        /// WebSocket default protocol version.
        /// </summary>
        public static readonly int WS_VERSION_DEFAULT = 13;

        /// <summary>
        /// jWebSocket sub protocol prefix.
        /// </summary>
        public static readonly string WS_SUBPROT_PREFIX = WebSocketMessage.ORG_JWEBSOCKET;

        /// <summary>
        /// jWebSocket JSON sub protocol.
        /// </summary>
        public static readonly string WS_SUBPROT_JSON = WS_SUBPROT_PREFIX + WebSocketMessage.POINT + WebSocketMessage.JSON;

        /// <summary>
        /// jWebSocket CSV sub protocol.
        /// </summary>
        public static readonly string WS_SUBPROT_CSV = WS_SUBPROT_PREFIX + WebSocketMessage.POINT + WebSocketMessage.CSV;

        /// <summary>
        /// jWebSocket XML sub protocol.
        /// </summary>
        public static readonly string WS_SUBPROT_XML = WS_SUBPROT_PREFIX + WebSocketMessage.POINT + WebSocketMessage.XML;

        /// <summary>
        /// jWebSocket custom specific text sub protocol.
        /// </summary>
        public static readonly string WS_SUBPROT_TEXT = WS_SUBPROT_PREFIX + WebSocketMessage.POINT + WebSocketMessage.TEXT;

        /// <summary>
        /// jWebSocket custom specific binary sub protocol.
        /// </summary>
        public static readonly string WS_SUBPROT_BINARY = WS_SUBPROT_PREFIX + WebSocketMessage.POINT + WebSocketMessage.BINARY;

        /// <summary>
        /// JSON sub protocol format.
        /// </summary>
        public static readonly string WS_FORMAT_JSON = WebSocketMessage.JSON;

        /// <summary>
        /// CSV sub protocol format.
        /// </summary>
        public static readonly string WS_FORMAT_CSV = WebSocketMessage.CSV;

        /// <summary>
        /// XML sub protocol format.
        /// </summary>
        public static readonly string WS_FORMAT_XML = WebSocketMessage.XML;

        /// <summary>
        /// Binary sub protocol format.
        /// </summary>
        public static readonly string WS_FORMAT_BINARY = WebSocketMessage.BINARY;

        /// <summary>
        /// Custom specific sub protocol format.
        /// </summary>
        public static readonly string WS_FORMAT_TEXT = WebSocketMessage.TEXT;

        /// <summary>
        /// Default protocol.
        /// </summary>
        public static readonly string WS_SUBPROT_DEFAULT = WS_SUBPROT_JSON;

        /// <summary>
        /// Host header from handshake.
        /// </summary>
        public static readonly string HOST = WebSocketMessage.HOST;

        /// <summary>
        /// Upgrade header from handshake.
        /// </summary>
        public static readonly string UPGRADE = WebSocketMessage.UPGRADE;

        /// <summary>
        /// Connection header from handshake.
        /// </summary>
        public static readonly string CONNECTION = WebSocketMessage.CONNECTION;

        /// <summary>
        /// Sec-WebSocket-Key header from handshake.
        /// </summary>
        public static readonly string SEC_WEBSOCKET_KEY = WebSocketMessage.SEC_WEBSOCKET_KEY;

        /// <summary>
        /// Sec-WebSocket-Origin header from handshake.
        /// </summary>
        public static readonly string ORIGIN = WebSocketMessage.ORIGIN;

        /// <summary>
        /// Sec-WebSocket-Protocol header from handshake.
        /// </summary>
        public static readonly string SEC_WEBSOCKET_PROTOCOL = WebSocketMessage.SEC_WEBSOCKET_PROTOCOL;

        /// <summary>
        /// Sec-WebSocket-Version header from handshake.
        /// </summary>
        public static readonly string SEC_WEBSOCKET_VERSION = WebSocketMessage.SEC_WEBSOCKET_VERSION;

        /// <summary>
        /// Sec-WebSocket-Accept header from handshake.
        /// </summary>
        public static readonly string SEC_WEBSOCKET_ACCEPT = WebSocketMessage.SEC_WEBSOCEKT_ACCEPT;

        /// <summary>
        /// Scheme ws for connection.
        /// </summary>
        public static readonly string SCHEME_WS = WebSocketMessage.WS;

        /// <summary>
        /// Scheme wss for connection.
        /// </summary>
        public static readonly string SCHEME_WSS = WebSocketMessage.WSS;

        /// <summary>
        /// XML configuration file.
        /// </summary>
        public static readonly string CONFIG = WebSocketMessage.CONFIG + WebSocketMessage.POINT + WebSocketMessage.XML;

        public static readonly string UTID = WebSocketMessage.UTID;

        /// <summary>
        /// The default maximum frame size if not configured.
        /// </summary>
        public static int DEFAULT_MAX_FRAME_SIZE;

        /// <summary>
        /// Default Session Timeout for client connections.
        /// </summary>
        public static int DEFAULT_OPEN_TIMEOUT;

        /// <summary>
        /// Default ping Timeout for client connections.
        /// </summary>
        public static int DEFAULT_PING_TIMEOUT;

        /// <summary>
        /// Default ping delay for client connections.
        /// </summary>
        public static int DEFAULT_PING_DELAY;

        /// <summary>
        /// WebSocket supported hixie versions.
        /// </summary>
        public static List<int> WS_SUPPORTED_HYBI_VERSIONS = new List<int>();

        /// <summary>
        /// Initializes the <see cref="WebSocketConstants"/> class.
        /// </summary>`
        static WebSocketConstants()
        {
            WS_SUPPORTED_HYBI_VERSIONS.Add(8);
            WS_SUPPORTED_HYBI_VERSIONS.Add(13);
            WS_SUPPORTED_HYBI_VERSIONS.Add(17);
            try
            {
                InitialiceXML();
                if (mLog.IsInfoEnabled)
                    mLog.Info(WebSocketMessage.CONFIGURATION_LOADED_CORRECTLY);
            }
            catch (Exception lEX)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.CAN_NOT_LOAD_CONFIGURATION 
                        + WebSocketMessage.SEPARATOR + lEX.Message);
            }
        }

        /// <summary>
        /// Initialices the XML config.
        /// </summary>
        private static void InitialiceXML()
        {
            XmlTextReader textReader = new XmlTextReader(CONFIG);
            while (textReader.Read())
            {
                XmlNodeType lType = textReader.NodeType;
                if (lType.Equals(XmlNodeType.Element))
                {
                    if (textReader.Name.Equals(WebSocketMessage.DEFAULT_MAX_FRAME_SIXE))
                        DEFAULT_MAX_FRAME_SIZE = Convert.ToInt32(textReader.GetAttribute(0));
                    if (textReader.Name.Equals(WebSocketMessage.DEFAULT_OPEN_TIMEOUT))
                        DEFAULT_OPEN_TIMEOUT = Convert.ToInt32(textReader.GetAttribute(0));
                    if (textReader.Name.Equals(WebSocketMessage.DEFAULT_PING_TIMEOUT))
                        DEFAULT_PING_TIMEOUT = Convert.ToInt32(textReader.GetAttribute(0));
                    if (textReader.Name.Equals(WebSocketMessage.DEFAULT_PING_DELAY))
                        DEFAULT_PING_DELAY = Convert.ToInt32(textReader.GetAttribute(0));
                }
            }
        }

    }
}

    

