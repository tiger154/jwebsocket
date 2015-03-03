/*--------------------------------------------------------------------------------
 * jWebSocket - TokenFactory
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
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.token.processor;

namespace WebSocket.org.jwebsocket.token
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public sealed class TokenFactory
    {
        public static IToken CreateToken()
        {
            return new Token();
        }

        public static IToken CreateToken(string aType)
        {
            return new Token(aType);
        }

        public static IToken CreateToken(string aNS, string aType)
        {
            return new Token(aNS, aType);
        }

        public static IToken PacketToToken(string aFormat, IWebSocketPacket aDataPacket)
        {
            if (aFormat.Equals(WebSocketConstants.WS_FORMAT_JSON))
                return JSONTokenProcessor.PacketToToken(aDataPacket);
            else if (aFormat.Equals(WebSocketConstants.WS_SUBPROT_CSV))
                throw new NotImplementedException(WebSocketMessage.NOT_IMPLEMENTED_YET);
            else if (aFormat.Equals(WebSocketConstants.WS_FORMAT_XML))
                throw new NotImplementedException(WebSocketMessage.NOT_IMPLEMENTED_YET);
            return null;
        }

        public static IWebSocketPacket TokenToPacket(string aFormat, IToken aToken)
        {
            if (aFormat.Equals(WebSocketConstants.WS_FORMAT_JSON))
                return JSONTokenProcessor.TokenToPacket(aToken);
            else if (aFormat.Equals(WebSocketConstants.WS_SUBPROT_CSV))
                throw new NotImplementedException(WebSocketMessage.NOT_IMPLEMENTED_YET);
            else if (aFormat.Equals(WebSocketConstants.WS_FORMAT_XML))
                throw new NotImplementedException(WebSocketMessage.NOT_IMPLEMENTED_YET);
            return null;
        }

    }
}
