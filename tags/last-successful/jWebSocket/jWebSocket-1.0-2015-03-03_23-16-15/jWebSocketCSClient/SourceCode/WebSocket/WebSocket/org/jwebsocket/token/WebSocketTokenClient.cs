/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketTokenClient
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
using JSON;
using log4net;
using WebSocket.org.jwebsocket.token.kit;
using WebSocket.org.jwebsocket.protocol;
using WebSocket.org.jwebsocket.protocol.kit;
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.token.api;
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.token.processor;


namespace WebSocket.org.jwebsocket.token
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Token based implementation of <c>WebSocketBaseClient</c>.
    /// </summary>

    #region Delegates

    public delegate void ReciveTokenEventHandler(WebSocketTokenClient sender, IToken e);
    public delegate void OpenEventHandler(WebSocketTokenClient sender, WebSocketHeaders e);
    public delegate void ReciveTextPacketEventHandler(WebSocketProtocol sender, IWebSocketPacket e);
    public delegate void PendingResponseEventHandler(WebSocketTokenClient sender,TokenResponse e);
    public delegate void CloseEventHandler(WebSocketTokenClient sender, WebSocketCloseReason e);
    public delegate void ErrorEventHandler(WebSocketTokenClient sender, WebSocketError e);

    #endregion

    public class WebSocketTokenClient : WebSocketProtocol
    {
        private int CURRENT_TOKEN_ID = 0;
        private Dictionary<int, PendingResponseQueue> mPendingResponseQueue =
            new Dictionary<int, PendingResponseQueue>();

        #region Events

        public event ReciveTokenEventHandler reciveToken;
        public event OpenEventHandler open;
        public event ReciveTextPacketEventHandler recivePacket;
        public event CloseEventHandler close;
        public event ErrorEventHandler error;

        #endregion

        public WebSocketTokenClient() { }

        public WebSocketTokenClient(WebSocketReliabilityOptions aReliabilityOptions)
            : base(aReliabilityOptions) { }


        public IWebSocketPacket TokenToPacket(IToken aToken)
        {
            if (mNegotiatedSubProtocol.SubProtocol.Equals(WebSocketConstants.WS_SUBPROT_JSON))
                return JSONTokenProcessor.TokenToPacket(aToken);
            else if (mNegotiatedSubProtocol.Equals(WebSocketConstants.WS_SUBPROT_CSV))
                throw new NotImplementedException(WebSocketMessage.NOT_IMPLEMENTED_YET);
            else if (mNegotiatedSubProtocol.Equals(WebSocketConstants.WS_FORMAT_XML))
                throw new NotImplementedException(WebSocketMessage.NOT_IMPLEMENTED_YET);
            return null;
        }

        public void SendToken(IToken aToken)
        {
            CURRENT_TOKEN_ID++;
            aToken.SetInt(WebSocketConstants.UTID, CURRENT_TOKEN_ID);
            aToken.IsBinary = false;
            SendTokenOption(aToken, -1);
        }

        public void SendToken(IToken aToken, PendingResponseEventHandler aPendingResponse)
        {
            CURRENT_TOKEN_ID++;
            aToken.SetInt(WebSocketConstants.UTID, CURRENT_TOKEN_ID);
            PendingResponseQueue lPRQI = new PendingResponseQueue(aToken, aPendingResponse);
            mPendingResponseQueue.Add(CURRENT_TOKEN_ID, lPRQI);
            aToken.IsBinary = false;
            SendTokenOption(aToken, -1);
        }

        private void SendTokenOption(IToken aToken, int aFragmentSize)
        {
            if (aToken.IsBinary)
            {
                if (!aFragmentSize.Equals(-1))
                    SendBinary(TokenToPacket(aToken).ByteArray, aFragmentSize);
                else
                    SendBinary(TokenToPacket(aToken).ByteArray);
            }
            else
            {
                if (!aFragmentSize.Equals(-1))
                    SendText(TokenToPacket(aToken).GetString(WebSocketTypeEncoding.UTF8), aFragmentSize);
                else
                    base.SendText(TokenToPacket(aToken).GetString(WebSocketTypeEncoding.UTF8));
            }
        }

        private IToken PacketToToken(IWebSocketPacket aDataPacket)
        {
            return JSONTokenProcessor.PacketToToken(aDataPacket);
        }

        public Dictionary<int, PendingResponseQueue> PendingResponseQueue
        {
            get { return mPendingResponseQueue; }
            set { mPendingResponseQueue = value; }
        }

        #region OnEvents

        protected override void OnCloseConnection(WebSocketCloseReason aReason)
        {
            if (close != null)
                close(this, aReason);
        }

        protected override void OnError(WebSocketError aError)
        {
            if (error != null)
                error(this, aError);
        }

        protected override void OnOpenConnection(WebSocketHeaders aHeaders)
        {
            if (open != null)
                open(this, aHeaders);
        }

        protected void OnReciveTokenText(IToken e)
        {
            if (reciveToken != null)
                reciveToken(this, e);
        }

        protected override void OnReciveTextPacket(WebSocketProtocol aSender, IWebSocketPacket e)
        {
            if (recivePacket != null)
                recivePacket(aSender, e);
            try
            {
                JsonObject lTextjson = new JsonObject(e.GetString());

                IToken lToken = PacketToToken(e);
                string lType = lToken.GetType();

                lock (PendingResponseQueue)
                {
                    if (!lType.Equals(WebSocketMessage.WELCOME) && !lType.Equals(WebSocketMessage.GOODBYTE))
                    {
                        try
                        {
                            int lUTID = lToken.GetInt(WebSocketMessage.UTID);
                            int lCode = lToken.GetInt(WebSocketMessage.CODE);

                            PendingResponseQueue lPRQI = PendingResponseQueue[lUTID];
                            if (lPRQI != null)
                            {
                                bool lSuccess = false;
                                if (lCode == 0)
                                    lSuccess = true;

                                TokenResponse lResponse = new TokenResponse(mPendingResponseQueue[lUTID].Token, lToken, lSuccess);
                                lPRQI.PendingResponse.Invoke(this, lResponse);
                                PendingResponseQueue.Remove(lUTID);
                            }
                        }
                        catch (Exception) { }
                    }
                }
                OnReciveTokenText(lToken);
            }
            catch (Exception) { }
        }
        #endregion

    }
}
