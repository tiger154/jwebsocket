using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using ClientLibrary.org.jwebsocket.client.packetProcessor;
using ClientLibrary.org.jwebsocket.client.token;
using ClientLibrary.org.jwebsocket.client.api;
using Newtonsoft.Json;
using System.Collections.Generic;
using ClientLibrary.org.jwebsocket.client.kit;

namespace WebSocketWPClient
{
    public class JSONTokenProcessor : TokenProcessor
    {

        public string TokenToText(Token aToken)
        {
            return JsonConvert.SerializeObject(aToken.GetDictionary());
        }

        public byte[] TokenToByte(Token aToken)
        {
            return WebSocketConvert.StringToBytes(TokenToText(aToken), WebSocketTypeEncoding.UTF8);
        }

        public Token PacketToToken(WebSocketPacket aPacket)
        {
            var byteArray = aPacket.GetByteArray();
            var packetString = WebSocketConvert.BytesToString(byteArray, WebSocketTypeEncoding.UTF8);
            var dictionary = JsonConvert.DeserializeObject<Dictionary<string, object>>(packetString);
            var dictionaryToken = new DictionaryToken();
            dictionaryToken.SetDictionary(dictionary);
            return dictionaryToken;
        }
    }
}
