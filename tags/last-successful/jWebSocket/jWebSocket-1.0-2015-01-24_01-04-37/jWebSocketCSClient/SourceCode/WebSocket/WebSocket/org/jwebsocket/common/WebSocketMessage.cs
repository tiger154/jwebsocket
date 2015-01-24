/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketMessage
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
    /// All messages of the client.
    /// </summary>
    public sealed class WebSocketMessage
    {
        public static readonly string ESTABLiSHING_CONNECTION = "Establishing connection to the server ";

        public static readonly string NOT_ESTABLISH_CONNECTION = "Could not establish the connection to the server";

        public static readonly string URL = "URL ";

        public static readonly string BYE = "BYE";

        public static readonly string HELLO = "Hello";

        public static readonly string UNKNOWN_HOST = "Unknown host";

        public static readonly string SUBPROTOCOL = "Subprotocol ";

        public static readonly string TIMEOUT = "Timeout ";

        public static readonly string VERSION = "Version ";

        public static readonly string SEPARATOR = " - ";

        public static readonly string SENDING_HANDSHAKE = "Sending handshake to the server : ";

        public static readonly string RECEIVING_HANDSHAKE = "Receiving handshake from server : ";

        public static readonly string CONNECTION_HAS_BEEN_ESTABLISHED = "The connection has been established whit the server";

        public static readonly string DETAILS = "Details: ";

        public static readonly string EXCEEDED_FOR_CONNECTION = TIMEOUT + "exceeded for connection";

        public static readonly string SENDING_TEXT_PACKET = "Sending text packet";

        public static readonly string SENDING_FRAGMENT_TEXT_PACKET = "Sending fragment text packet";

        public static readonly string FRAGMENT_SIXE_EXCEED_MAX_SIZE = "The Fragment size exceed default max frame size";

        public static readonly string SENDING_BINARY_PACKET = "Sending binary packet";

        public static readonly string SENDING_FRAGMENT_BINARY_PACKET = "Sending fragment binary packet";

        public static readonly string ERROR_WHILE_SENDING_SOCKET = "Error while sending socket data";

        public static readonly string ERROR_WHILE_SENDING_BINARY = "Error while sending binary data: not connected";

        public static readonly string WEBSOCKETBASECLIENT = "WebSocketBaseClient";

        public static readonly string ERROR_WHILE_CLOSING_SOCKET = "Error while closing Socket";

        public static readonly string NULL_FRAME_TYPE = "Null frame type was received";

        public static readonly string INVALID_HYBI_FRAME = "Invalid hybi frame was received";

        public static readonly string CLOSE_FRAME_TYPE = "Close frame type was received";

        public static readonly string PING_PACKET_SENDING = "Ping packet was sending";

        public static readonly string BINARY_PACKET_RECEIVED = "Binary packet was received";

        public static readonly string TEXT_PACKET_RECEIVED = "Text packet was received";

        public static readonly string FRAGMENT_PACKET_RECEIVED = "Fragment packet was received";

        public static readonly string ERROR_HYBI_PROCESSOR = "Error in hybi processor";

        public static readonly string CLOSE_CONNECTION = "CLose connection";

        public static readonly string ERROR_WHILE_CLOSE_CONNECTION = "Error while close connection";

        public static readonly string ERROR_WHILE_SENDING_CLOSE_HANDSHAKE = "Error while sending close handshake";

        public static readonly string ERROR_WHILE_SENDING_PING = "Error while sending ping";

        public static readonly string UNSUPPORTED_PROTOCOL = "Unsupported protocol";

        public static readonly string CHECKING_RECONNECT = "Checking reconnect";

        public static readonly string INITIALIZING = "Initializing ";

        public static readonly string CONFIGURATION_LOADED_CORRECTLY = "The Config.xml configuration was loaded correctly";

        public static readonly string CAN_NOT_LOAD_CONFIGURATION = "Can not load Config.xml configuration correctly";

        public static readonly string NOT_IMPLEMENTED_YET = "Not implemented yet";

        public static readonly string ORG_JWEBSOCKET="org.jwebsocket";

        public static readonly string JSON = "json";

        public static readonly string CSV = "csv";

        public static readonly string XML = "xml";

        public static readonly string TEXT = "text";

        public static readonly string BINARY = "binary";

        public static readonly string POINT = ".";

        public static readonly string HOST = "Host";

        public static readonly string UPGRADE = "Upgrade";

        public static readonly string LOWUPGRADE = "upgrade";

        public static readonly string CONNECTION = "Connection";

        public static readonly string SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";

        public static readonly string ORIGIN = "Origin";

        public static readonly string SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

        public static readonly string SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";

        public static readonly string SEC_WEBSOCEKT_ACCEPT = "Sec-WebSocket-Accept";

        public static readonly string WS = "ws";

        public static readonly string WSS = "wss";

        public static readonly string CONFIG = "Config";

        public static readonly string UTID = "utid";

        public static readonly string DEFAULT_MAX_FRAME_SIXE = "default_max_frame_sixe";

        public static readonly string DEFAULT_OPEN_TIMEOUT = "default_open_timeout";

        public static readonly string DEFAULT_PING_TIMEOUT = "default_ping_timeout";

        public static readonly string DEFAULT_PING_DELAY = "default_ping_delay";

        public static readonly string INVALID_CONVERTING = "invalid converting : ";

        public static readonly string WEBSOCKET = "WebSocket ";

        public static readonly string HANDSHAKE = "Handshake";

        public static readonly string ILLEGAL_WEBSOCKET_PROTOCOL_VERSION = " Illegal WebSocket protocol version '";

        public static readonly string DETECTED = "' detected.";

        public static readonly string GENERATING_C2S_REQUEST = "Generating C2S request";

        public static readonly string HTTP = "http://";

        public static readonly string SLASH = "/";

        public static readonly string SLASH1 = " / ";

        public static readonly string GET = "GET ";

        public static readonly string HTTP11 = " HTTP/1.1\r\n";

        public static readonly string TWO_POINT = ": ";

        public static readonly string NL_RETURN = "\r\n";

        public static readonly string UPGRADE_WEBSOCEKT = "Upgrade: WebSocket";

        public static readonly string CONNECTION_UPGRADE = "Connection: Upgrade";

        public static readonly string KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        public static readonly string ERROR_CONVERTING_BASE64 = "Error converting to Base64";

        public static readonly string VERIFYING_S2C_RESPONSE = "verifying S2C response";

        public static readonly string HTTP_SWITCHING_PROTOCOL = "HTTP/1.1 101 Switching Protocols";

        public static readonly string CONNECTION_FAILED_HTTP = "connection failed: missing header field in server handshake: HTTP/1.1";

        public static readonly string WEBSOCKET1 = "websocket";

        public static readonly string CONNECTION_FAILED_UPGRADE = "connection failed: missing header field in server handshake: Upgrade";

        public static readonly string CONNECTION_FAILED_CONNECTION = "connection failed: missing header field in server handshake: Connection";

        public static readonly string CONNECTION_FAILED_SEC_WEBSOCKET_KEY = "connection failed: missing header field in server handshake: Sec-WebSocket-Key";

        public static readonly string CONNECTION_FAILED_SEC_WEBSOCKET_PROTOCOL = "connection failed: missing header field in server handshake: Sec-WebSocket-Protocol";

        public static readonly string HEADERS = "headers ";

        public static readonly string READING_RESPONSE_FROM_STREAM = "Reading response from stream";

        public static readonly char TWO_POINT2 = ':';

        public static readonly string READING_REQUEST_FROM_BUFFER = "Reading request from buffer";

        public static readonly string ERROR_READING_STREAM = "Error on reading stream";

        public static readonly string CANNOT_CONSTRUCT_PACKET = "Cannot construct a packet with unknown packet type";

        public static readonly string INVALID_FRAME_TYPE = "Invalid frame type";

        public static readonly string EOF = "EOF";

        public static readonly string L_PARENT = "[";

        public static readonly string R_PARENT = "]";

        public static readonly string WELCOME = "welcome";

        public static readonly string GOODBYTE = "gooByte";

        public static readonly string CODE = "code";

        public static readonly string NO_FIND_KEY = "No find key : ";

        public static readonly string NO_INSERT_KEY_VALUE = "No insert key/value : ";

        public static readonly string NO_PARSED_VALUE = "No parsed value : ";

        public static readonly string TYPE = "type";

        public static readonly string NS = "ns";

        public static readonly string NS_BASE = "org.jwebsocket";

        public static readonly string NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";

        public static readonly string ECHO = "echo";

        public static readonly string TRYING_TO_CONNECT_TO_THE_SERVER = "Trying to connect to the server";

        public static readonly string COOKIE = "Cookie";

        public static readonly char SEMICOLON = ';';

        public static readonly string SEMICOLON2 = "; ";

        public static readonly string EXPIRES = "Expires";

        public static readonly string DOMAIN = "Domain";

        public static readonly string PATH = "Path";

        public static readonly char EQUAL = '=';

        public static readonly string SECURE = "Secure";

        public static readonly string HTTPONLY = "HttpOnly";

        public static readonly char SLASHCHAR = '/';

        public static readonly string SET_COOKIE = "Set-Cookie";

        public static readonly string MAXFRAMESIZE = "maxframesize1048840";

    }
}
