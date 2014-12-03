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
using ClientLibrary.org.jwebsocket.client.kit;
using ClientLibrary.org.jwebsocket.client.config;
using ClientLibrary.org.jwebsocket.client.token;

namespace WebSocketWPClient
{
    /// <summary>
    /// The web socket client that sends and receive data as <see cref="DictionaryToken"/>
    /// </summary>
    public class WebSocketTokenClient : WebSocketBaseClient
    {

        internal static string NS_BASE = "org.jwebsocket";
        public static string NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";
        internal static string NS_FILESYSTEM_PLUGIN = NS_BASE + ".plugins.filesystem";
        internal static string NS_ADMIN_PLUGIN = NS_BASE + ".plugins.admin";

        private JSONTokenProcessor _tokenProcessor;
        private int _currentTokenId;

        /// <summary>
        /// Initializes a new instance of the <see cref="WebSocketTokenClient"/> class.
        /// </summary>
        public WebSocketTokenClient()
        {
            SubProtocol = new WebSocketSubProtocol(WebSocketConstants.WS_SUBPROT_JSON, WebSocketEncoding.TEXT);
            _tokenProcessor = new JSONTokenProcessor();
        }


        private void SendToken(Token aToken, int aFragmentSize)
        {

            var tokenText = _tokenProcessor.TokenToText(aToken);
            SendText(tokenText);
        }


        public void SendTokenText(Token token)
        {
            _currentTokenId++;
            token.SetInt("utid", _currentTokenId);
            SendToken(token, -1);
        }

        public Token PacketToToken(WebSocketRawPacket packet)
        {
            return _tokenProcessor.PacketToToken(packet);
        }

    }
}
