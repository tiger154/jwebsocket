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
using ClientLibrary.org.jwebsocket.client.token;
using ClientLibrary.org.jwebsocket.client.kit;
using ClientLibrary.org.jwebsocket.client.config;
using ClientLibrary.org.jwebsocket.client.api;
using ClientLibrary.org.jwebsocket.client.packetProcessor;



namespace ClientLibrary.org.jwebsocket.client.csharp
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketBaseTokenClient:WebSocketBaseClient
    {
        public static string NS_BASE = "org.jwebsocket";
        public static string NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";
        public static string NS_FILESYSTEM_PLUGIN = NS_BASE + ".plugins.filesystem";
        public static string NS_ADMIN_PLUGIN = NS_BASE + ".plugins.admin";

        private int CURRENT_TOKEN_ID = 0;
        private Dictionary<int, PendingResponseQueueItem> mPendingResponseQueue =
            new Dictionary<int, PendingResponseQueueItem>();

        public Dictionary<int, PendingResponseQueueItem> MPendingResponseQueue
        {
            get { return mPendingResponseQueue; }
            set { mPendingResponseQueue = value; }
        }

        public WebSocketBaseTokenClient() { }
         
        public WebSocketBaseTokenClient(WebSocketReliabilityOptions aReliabilityOptions)
            : base(aReliabilityOptions) { }


        private TokenProcessor TokenProtocolProcessor()
        {
            if (WebSocketConstants.WS_FORMAT_JSON.Equals(mNegotiatedSubProtocol.GetFormat()))
                return new JSONTokenProcessor();
            else if (WebSocketConstants.WS_FORMAT_CSV.Equals(mNegotiatedSubProtocol.GetFormat()))
                return new CSVTokenProcessor();
            else if (WebSocketConstants.WS_FORMAT_XML.Equals(mNegotiatedSubProtocol.GetFormat()))
                return new XMLTokenProcessor();
            else
                return null;
        }

        private void SendToken(Token aToken,int aFragmentSize)
        {
            TokenProcessor lProcessor = TokenProtocolProcessor();
            if (aToken.IsBinary())
            {
                byte [] lByteData=lProcessor.TokenToByte(aToken);
                if (!aFragmentSize.Equals(-1))
                    SendBinary(lByteData, aFragmentSize);
                else
                    SendBinary(lByteData);
            }
            else
            {
                string lUTF8String = lProcessor.TokenToText(aToken);
                if (!aFragmentSize.Equals(-1))
                    SendText(lUTF8String, aFragmentSize);
                else
                    SendText(lUTF8String);
            }
        }

        public void SendTokenText(Token aToken)
        {
            aToken.SetBinary(false);
            SendToken(aToken,-1);
        }

        public void SendTokenText(Token aToken, WebSocketResponseTokenListener aResponseListener)
        {
            CURRENT_TOKEN_ID++;
            aToken.SetInt("utid", CURRENT_TOKEN_ID);
            PendingResponseQueueItem lPRQI = new PendingResponseQueueItem(aToken, aResponseListener);
            mPendingResponseQueue.Add(CURRENT_TOKEN_ID, lPRQI);
            aToken.SetBinary(false);
            SendToken(aToken, -1);
        }

        public void SendTokenBinary(Token aToken)
        {
            aToken.SetBinary(true);
            SendToken(aToken,-1);
        }

        public void SendTokenFragmented(Token aToken, int aFragmentSize)
        {
            SendToken(aToken, aFragmentSize);
        }

        public void AddTokenClientListener(WebSocketClientTokenListener aTokenListener)
        {
            base.AddListener(aTokenListener);
        }

        public void RemoveTokenClientListener(WebSocketClientTokenListener aTokenListener)
        {
            base.RemoveListener(aTokenListener);
        }

        public Token PacketToToken(WebSocketPacket aPacket)
        {
            return TokenProtocolProcessor().PacketToToken(aPacket);
        }

    }
}
