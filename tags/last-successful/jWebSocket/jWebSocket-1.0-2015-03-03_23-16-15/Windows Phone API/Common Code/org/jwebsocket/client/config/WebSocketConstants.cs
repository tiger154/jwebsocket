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
using ClientLibrary.org.jwebsocket.client.kit;

namespace ClientLibrary.org.jwebsocket.client.config
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketConstants
    {
        internal const int MAX_RECEIVE_BUFFER_SIZE = 2048;

        public static WebSocketEncoding WS_ENCODING_DEFAULT = WebSocketEncoding.TEXT;

        public static int WS_VERSION_DEFAULT = 13;

        public static string WS_SUBPROT_PREFIX = "org.jwebsocket";

        public static string WS_SUBPROT_JSON = WS_SUBPROT_PREFIX + ".json";

        public static string WS_SUBPROT_CSV = WS_SUBPROT_PREFIX + ".csv";

        public static string WS_SUBPROT_XML = WS_SUBPROT_PREFIX + ".xml";

        public static string WS_SUBPROT_TEXT = WS_SUBPROT_PREFIX + ".text";

        public static string WS_SUBPROT_BINARY = WS_SUBPROT_PREFIX + ".binary";

        public static string WS_FORMAT_JSON = "json";

        public static string WS_FORMAT_CSV = "csv";

        public static string WS_FORMAT_XML = "xml";

        public static string WS_FORMAT_BINARY = "binary";

        public static string WS_FORMAT_TEXT = "text";

        public static string WS_SUBPROT_DEFAULT = WS_SUBPROT_JSON;

        public static string HOST = "Host";

        public static string UPGRADE = "Upgrade";

        public static string CONNECTION = "Connection";

        public static string SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";

        public static string SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";

        public static string SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

        public static string SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";

        public static string SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

        public static int DEFAULT_MAX_FRAME_SIZE = 1048840;

        public static int DEFAULT_OPEN_TIMEOUT = 5000;

        public static int DEFAULT_PING_TIMEOUT = 5000;

        public static List<int> WS_SUPPORTED_HYBI_VERSIONS = new List<int>();

        static WebSocketConstants()
        {
            WS_SUPPORTED_HYBI_VERSIONS.Add(13);
        }
    }
}
