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
//using System.Web.Script.Serialization;
using ClientLibrary.org.jwebsocket.client.api;
using ClientLibrary.org.jwebsocket.client.kit;
using ClientLibrary.org.jwebsocket.client.token;

namespace ClientLibrary.org.jwebsocket.client.packetProcessor
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class JSONTokenProcessor : TokenProcessor
    {
        public string TokenToText(Token aToken)
        {
            //JavaScriptSerializer lJavaScriptSerializer = new JavaScriptSerializer();
            //return lJavaScriptSerializer.Serialize(aToken.GetDictionary());
            throw new Exception("Not implemented yet");
        }

        public byte[] TokenToByte(Token aToken)
        {
            //JavaScriptSerializer lJavaScriptSerializer = new JavaScriptSerializer();
            //return WebSocketConvert.StringToBytes(lJavaScriptSerializer.Serialize(aToken.GetDictionary()),
            //    WebSocketTypeEncoding.UTF8);
            throw new Exception("Not implemented yet");
        }

        public Token PacketToToken(WebSocketPacket aPacket)
        {
            //DictionaryToken lDicToken = new DictionaryToken();
            //JavaScriptSerializer lJavaScriptSerializer = new JavaScriptSerializer();
            //Dictionary<string, object> lDic = lJavaScriptSerializer.Deserialize<Dictionary<string, object>>(
            //    WebSocketConvert.BytesToString(aPacket.GetByteArray(), WebSocketTypeEncoding.ASCII));
            //lDicToken.SetDictionary(lDic);
            //return lDicToken;
            throw new Exception("Not implemented yet");
        }
    }
}
