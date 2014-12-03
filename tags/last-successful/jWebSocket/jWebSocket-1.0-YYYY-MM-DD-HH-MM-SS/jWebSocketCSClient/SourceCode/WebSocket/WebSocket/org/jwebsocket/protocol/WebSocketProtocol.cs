/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketProtocol
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
using System.Net.Sockets;
using System.Net.Security;
using System.Security.Authentication;
using System.Security.Cryptography.X509Certificates;
using System.IO;
using System.Threading;
using System.Diagnostics;
using log4net;
using log4net.Config;
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.protocol.kit;
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.protocol;


namespace WebSocket.org.jwebsocket.protocol
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Base WebSocket implementation.
    /// This uses thread model for handling WebSocket connection which is defined
    /// by the WebSocket protocol specification. 
    /// http://www.whatwg.org/specs/web-socket-protocol/
    /// http://www.w3.org/TR/websockets/
    /// </summary>

    #region Delegates

    public delegate void ReciveTextPacketEventHandler(WebSocketProtocol sender, IWebSocketPacket e);
    public delegate void RecivePongEventHandler(WebSocketProtocol sender);
    public delegate void SendPingEventHandler(WebSocketProtocol sender);
    public delegate void ErrorEventHandler(WebSocketProtocol sender, WebSocketError e);
    public delegate void OpenEventHandler(WebSocketProtocol sender, WebSocketHeaders e);
    public delegate void CloseEventHandler(WebSocketProtocol sender, WebSocketCloseReason e);

    #endregion

    public class WebSocketProtocol : IWebSocketProtocol
    {
        #region Atributes

        private Uri mURI;
        private TcpClient mSocket;
        private NetworkStream mNetStream;
        private int mVersion =WebSocketConstants.WS_VERSION_DEFAULT;

        private WebSocketCookieManager mCookieManage = new WebSocketCookieManager();
        private List<WebSocketSubProtocol> mSubProtocols;
        protected WebSocketSubProtocol mNegotiatedSubProtocol;

        private WebSocketStatus mStatus = WebSocketStatus.CLOSED;
        private WebSocketEncoding mEncoding = WebSocketEncoding.TEXT;
        private WebSocketReliabilityOptions mReliabilityOptions = null;
        private WebSocketHeaders mHeaders = new WebSocketHeaders();
        private WebSocketCloseReason mCLose = WebSocketCloseReason.CLIENT;

        private object mWriteLock = new object();
        private bool mIsRunning = false;
        private static readonly ILog mLog = LogManager.GetLogger(typeof(WebSocketProtocol).Name);
        private Thread mPing = null;
        private Thread mReconnect = null;

        #endregion

        #region Events

        public event ReciveTextPacketEventHandler recivePacket;
        public event RecivePongEventHandler recivePong;
        public event SendPingEventHandler sendPing;
        public event ErrorEventHandler error;
        public event OpenEventHandler open;
        public event CloseEventHandler close;

        #endregion

        public WebSocketProtocol()
        {
            XmlConfigurator.Configure(new FileInfo(WebSocketConstants.CONFIG));
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.INITIALIZING + WebSocketMessage.WEBSOCKETBASECLIENT);
        }

        public WebSocketProtocol(WebSocketReliabilityOptions aReliabilityOptions)
        {
            mReliabilityOptions = aReliabilityOptions;
            XmlConfigurator.Configure(new FileInfo(WebSocketConstants.CONFIG));
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.INITIALIZING + WebSocketMessage.WEBSOCKETBASECLIENT);
        }

        public void Open(string aURI)
        {
            string lSubProtocols = GenerateSubProtocolsHeaderValue();
            Open(aURI, lSubProtocols);
        }
     
        public void Open(string aURI, string aSubProtocol)
        {
            Open(aURI, aSubProtocol, WebSocketConstants.DEFAULT_OPEN_TIMEOUT);
        }

        public void Open(string aURI, string aSubProtocol, int aTimeout)
        {
            Open(aURI, aSubProtocol, aTimeout, mVersion);
        }

        public void Open(string aURI, string aSubProtocol, int aTimeout, int aVersion)
        {
            try
            {
                if (mLog.IsDebugEnabled)
                    mLog.Debug(WebSocketMessage.ESTABLiSHING_CONNECTION + WebSocketMessage.SEPARATOR
                        + WebSocketMessage.URL + aURI + WebSocketMessage.SEPARATOR + WebSocketMessage.SUBPROTOCOL
                        + aSubProtocol + WebSocketMessage.SEPARATOR + WebSocketMessage.TIMEOUT
                        + aTimeout + WebSocketMessage.SEPARATOR + WebSocketMessage.VERSION + aVersion);

                mStatus = WebSocketStatus.OPENING;

                mVersion = aVersion;
                mURI = new Uri(aURI);

                WebSocketHandshake lHandshake = new WebSocketHandshake(mURI, aSubProtocol, mVersion,mCookieManage);

                if (mSocket != null && mSocket.Connected)
                    mSocket.Close();
                 CreateSocket();

                byte[] sendBuffer = lHandshake.GenerateC2SRequest();
                mNetStream.Write(sendBuffer, 0, sendBuffer.Length);
                mNetStream.Flush();

                mStatus = WebSocketStatus.CONNECTING;

                mHeaders.ReadRequestFromBuffer(sendBuffer);
                if (mLog.IsDebugEnabled)
                    mLog.Debug(WebSocketMessage.SENDING_HANDSHAKE + mHeaders.ToStringRequest());
            
                WebSocketTimeout.CallWithTimeout(mHeaders.ReadResponseFromStream, aTimeout, mNetStream);

                mCookieManage.AddCookies(mHeaders.GetCookies, mURI);

                if (mLog.IsDebugEnabled)
                    mLog.Debug(WebSocketMessage.RECEIVING_HANDSHAKE + mHeaders.ToStringResponse());

                lHandshake.VerifyS2CResponse(mHeaders);

                string lProtocol = GetResponseHeaderField(WebSocketConstants.SEC_WEBSOCKET_PROTOCOL);

                if (lProtocol != null)
                    mNegotiatedSubProtocol = new WebSocketSubProtocol(lProtocol, mEncoding);
                else
                    mNegotiatedSubProtocol = new WebSocketSubProtocol(WebSocketConstants.WS_SUBPROT_DEFAULT,
                        WebSocketConstants.WS_ENCODING_DEFAULT);

                Thread lReciver = new Thread(new ThreadStart(Receiver));
                lReciver.Start();

                if (mLog.IsInfoEnabled)
                    mLog.Info(WebSocketMessage.CONNECTION_HAS_BEEN_ESTABLISHED);

                mStatus = WebSocketStatus.OPENED;
                mIsRunning = true;
                OnOpenConnection(mHeaders);
            }
            catch (TimeoutException lTe)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.EXCEEDED_FOR_CONNECTION + WebSocketMessage.SEPARATOR 
                        + WebSocketMessage.DETAILS + lTe.Message);
                mCLose = WebSocketCloseReason.TIMEOUT;
                OnCloseConnection(mCLose);
                OnError(new WebSocketError(WebSocketMessage.TIMEOUT + WebSocketMessage.EXCEEDED_FOR_CONNECTION, mCLose));
                CheckReconnect();
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.NOT_ESTABLISH_CONNECTION + WebSocketMessage.SEPARATOR 
                        + WebSocketMessage.DETAILS + lEx.Message);
                mCLose = WebSocketCloseReason.BROKEN;
                OnCloseConnection(mCLose);
                OnError(new WebSocketError(WebSocketMessage.NOT_ESTABLISH_CONNECTION, mCLose));
                CheckReconnect();
            }
        }
      
        public void SendText(string aUTF8String)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.SENDING_TEXT_PACKET);
            lock (mWriteLock)
                SendPacketText(aUTF8String, -1);
        }

        public void SendText(string aUTF8String, int aFragmentSize)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.SENDING_FRAGMENT_TEXT_PACKET);
            lock (mWriteLock)
                SendPacketText(aUTF8String, aFragmentSize);
        }

        internal void SendPacketText(string aUTF8String, int aFragmentSize)
        {
            if (!aFragmentSize.Equals(-1))
            {
                if (aFragmentSize <= WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendFragmentsText(aUTF8String, aFragmentSize);
                else
                {
                    if (mLog.IsErrorEnabled)
                        mLog.Error(WebSocketMessage.FRAGMENT_SIXE_EXCEED_MAX_SIZE);
                    OnError(new WebSocketError(WebSocketMessage.FRAGMENT_SIXE_EXCEED_MAX_SIZE));
                    throw new WebSocketException(WebSocketMessage.FRAGMENT_SIXE_EXCEED_MAX_SIZE
                        + WebSocketMessage.SEPARATOR + WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
                }
            }
            else
            {
                if (aUTF8String.Length > WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendFragmentsText(aUTF8String, WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
                else
                {
                    IWebSocketPacket lPacket = new WebSocketPacket(WebSocketFrameType.TEXT, aUTF8String);
                    lPacket.IsFragmented = true;
                    SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));
                }
            }
        }

        internal void SendFragmentsText(string aUTF8String, int aFragmentSize)
        {
            StringBuilder lBuild = new StringBuilder(aUTF8String);
            int lBuildSize = lBuild.Length;
            int lFragmentCount = CalcFragmentCount(lBuildSize, aFragmentSize);
            int lCurrentFragment = 1;
            int lPos = 0;

            while (lPos < lBuildSize)
            {
                if (lPos + aFragmentSize > lBuildSize)
                    aFragmentSize = lBuildSize - lPos;

                IWebSocketPacket lPacket;
                if (lCurrentFragment.Equals(1))
                    lPacket = new WebSocketPacket(WebSocketFrameType.TEXT,
                            lBuild.ToString(lPos, aFragmentSize));
                else if (lCurrentFragment.Equals(lFragmentCount))
                {
                    lPacket = new WebSocketPacket(WebSocketFrameType.FRAGMENT,
                           lBuild.ToString(lPos, aFragmentSize));
                    lPacket.IsFragmented = true;
                }
                else
                    lPacket = new WebSocketPacket(WebSocketFrameType.FRAGMENT,
                            lBuild.ToString(lPos, aFragmentSize));

                SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));

                if (lPos + aFragmentSize > lBuildSize)
                    lPos += lBuildSize - lPos;
                else
                    lPos += aFragmentSize;
                lCurrentFragment++;
                Thread.Sleep((aFragmentSize * 1) / 100);
            }
        }

        public void SendBinary(byte[] aBinaryData)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.SENDING_BINARY_PACKET);
            lock (mWriteLock)
                SendPacketBinary(aBinaryData, -1);
        }

        public void SendBinary(byte[] aBinaryData, int aFragmentSize)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.SENDING_FRAGMENT_BINARY_PACKET);
            lock (mWriteLock)
                SendPacketBinary(aBinaryData, aFragmentSize);
        }

        internal void SendPacketBinary(byte[] aBinaryData, int aFragmentSize)
        {
            if (!aFragmentSize.Equals(-1))
            {
                if (aFragmentSize <= WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendfragmentsBinary(aBinaryData, aFragmentSize);
                else
                {
                    if (mLog.IsErrorEnabled)
                        mLog.Error(WebSocketMessage.FRAGMENT_SIXE_EXCEED_MAX_SIZE);
                    OnError(new WebSocketError(WebSocketMessage.FRAGMENT_SIXE_EXCEED_MAX_SIZE));
                    throw new WebSocketException(WebSocketMessage.FRAGMENT_SIXE_EXCEED_MAX_SIZE
                        + WebSocketMessage.SEPARATOR + WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
                }
            }
            else
            {
                if (aBinaryData.Length > WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendfragmentsBinary(aBinaryData, WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
                else
                {
                    IWebSocketPacket lPacket = new WebSocketPacket(WebSocketFrameType.BINARY, aBinaryData);
                    lPacket.IsFragmented = true;
                    SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));
                }
            }
        }

        internal void SendfragmentsBinary(byte[] aBinaryData, int aFragmentSize)
        {
            byte[] lNewBinaryData = new byte[aFragmentSize];
            int lBinaryDataSize = aBinaryData.Length;
            int lFragmentCount = CalcFragmentCount(lBinaryDataSize, aFragmentSize);
            int lCurrentFragment = 1;
            int lPos = 0;

            while (lPos < lBinaryDataSize)
            {
                if (lPos + aFragmentSize > lBinaryDataSize)
                {
                    aFragmentSize = lBinaryDataSize - lPos;
                    lNewBinaryData = new byte[aFragmentSize];
                }
                Array.Copy(aBinaryData, lPos, lNewBinaryData, 0, aFragmentSize);
                IWebSocketPacket lPacket;
                if (lCurrentFragment.Equals(1))
                    lPacket = new WebSocketPacket(WebSocketFrameType.BINARY, lNewBinaryData);
                else if (lCurrentFragment.Equals(lFragmentCount))
                {
                    lPacket = new WebSocketPacket(WebSocketFrameType.FRAGMENT, lNewBinaryData);
                    lPacket.IsFragmented = true;
                }
                else
                    lPacket = new WebSocketPacket(WebSocketFrameType.FRAGMENT, lNewBinaryData);

                SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));

                if (lPos + aFragmentSize > lBinaryDataSize)
                    lPos += lBinaryDataSize - lPos;
                else
                    lPos += aFragmentSize;
                lCurrentFragment++;
                Thread.Sleep((aFragmentSize * 1) / 100);
            }
        }

        internal void SendPacket(IWebSocketPacket aPacket)
        {
            try
            {
                lock (mWriteLock)
                    SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(aPacket));
            }
            catch (Exception)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET);
                OnError(new WebSocketError(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET));
                StopIt();
            }
        }

        internal void SendInternal(byte[] aData)
        {
            try
            {
                if (WebSocketStateOfStatus.isWritable(mStatus))
                {
                    lock (mWriteLock)
                    {
                        mNetStream.Write(aData, 0, aData.Length);
                        mNetStream.Flush();
                    }
                }
            }
            catch (ObjectDisposedException lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET
                        + WebSocketMessage.SEPARATOR + lEx.Message);
                OnError(new WebSocketError(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET));
                StopIt();
                throw new WebSocketException(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET
                        + WebSocketMessage.SEPARATOR + lEx.Message);
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET
                        + WebSocketMessage.SEPARATOR + lEx.Message);
                OnError(new WebSocketError((WebSocketMessage.ERROR_WHILE_SENDING_SOCKET)));
                StopIt();
                throw new WebSocketException(WebSocketMessage.ERROR_WHILE_SENDING_SOCKET
                        + WebSocketMessage.SEPARATOR + lEx.Message);
            }
        }

        private void Receiver()
        {
            Thread.CurrentThread.Name = WebSocketMessage.WEBSOCKETBASECLIENT
                + WebSocketMessage.SEPARATOR + Thread.GetDomainID();
            mIsRunning = true;
            Ping();
            ProcessHybi();
            mStatus = WebSocketStatus.CLOSING;

            try
            {
                mNetStream.Close();
                if (mSocket.Connected)
                    mSocket.Close();
            }
            catch (IOException lIOEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_CLOSING_SOCKET);
                OnError(new WebSocketError(WebSocketMessage.ERROR_WHILE_CLOSING_SOCKET));
                throw new WebSocketException(WebSocketMessage.ERROR_WHILE_CLOSING_SOCKET
                    + WebSocketMessage.SEPARATOR + lIOEx.Message);
            }

            mStatus = WebSocketStatus.CLOSED;
            OnCloseConnection(mCLose);

            if (!mCLose.Equals(WebSocketCloseReason.CLIENT))
            {
                mHeaders.GetRequestFields.Clear();
                mHeaders.GetResponseFields.Clear();
                CheckReconnect();
            }
        }

        private void ProcessHybi()
        {
            WebSocketFrameType lFrameType;
            IWebSocketPacket lPacket;

            while (mIsRunning)
            {
                try
                {
                    lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(mNetStream);
                    Console.WriteLine(lPacket.GetString());
                    lFrameType = (lPacket != null ? lPacket.FrameType : WebSocketFrameType.INVALID);

                    if (lFrameType.Equals(null))
                    {
                        if (mLog.IsErrorEnabled)
                            mLog.Error(WebSocketMessage.NULL_FRAME_TYPE);
                        mIsRunning = false;
                        mCLose = WebSocketCloseReason.BROKEN;
                        OnError(new WebSocketError(WebSocketMessage.NULL_FRAME_TYPE, mCLose));
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.INVALID))
                    {
                        if (mLog.IsErrorEnabled)
                            mLog.Error(WebSocketMessage.INVALID_HYBI_FRAME);
                        mIsRunning = false;
                        mCLose = WebSocketCloseReason.BROKEN;
                        OnError(new WebSocketError(WebSocketMessage.INVALID_HYBI_FRAME, mCLose));
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.CLOSE))
                    {
                        if (mLog.IsInfoEnabled)
                            mLog.Info(WebSocketMessage.CLOSE_FRAME_TYPE);
                        mIsRunning = false;
                        mCLose = WebSocketCloseReason.CLIENT;

                        StopIt();
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.PONG))
                    {
                        OnRecivePong();
                        if (WebSocketStateOfStatus.isWritable(mStatus))
                        {
                            if (mLog.IsInfoEnabled)
                                mLog.Info(WebSocketMessage.PING_PACKET_SENDING);
                            Ping();
                            OnSendPing();
                        }
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.BINARY))
                    {
                        if (mLog.IsInfoEnabled)
                            mLog.Info(WebSocketMessage.BINARY_PACKET_RECEIVED);
                        //****TODO****
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.TEXT))
                    {
                        if (mLog.IsInfoEnabled)
                            mLog.Info(WebSocketMessage.TEXT_PACKET_RECEIVED);
                        OnReciveTextPacket(this, lPacket);
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.FRAGMENT))
                    {
                        if (mLog.IsInfoEnabled)
                            mLog.Info(WebSocketMessage.FRAGMENT_PACKET_RECEIVED);
                        //****TODO****
                    }
                }
                catch (Exception lEx)
                {
                    mIsRunning = false;
                    if (mStatus.Equals(WebSocketStatus.CLOSED))
                        mCLose = WebSocketCloseReason.CLIENT;
                    else
                    {
                        mCLose = WebSocketCloseReason.BROKEN;
                        OnError(new WebSocketError(WebSocketMessage.ERROR_HYBI_PROCESSOR, mCLose));
                        if (mLog.IsErrorEnabled)
                            mLog.Error(WebSocketMessage.ERROR_HYBI_PROCESSOR
                                + WebSocketMessage.SEPARATOR + lEx.Message);
                    }
                }
            }
        }

        #region OnEvents

        protected virtual void OnReciveTextPacket(WebSocketProtocol aSender, IWebSocketPacket e)
        {
            if (recivePacket != null)
            {
                recivePacket(aSender, e);
            }
        }

        protected virtual void OnRecivePong()
        {
            if (recivePong != null)
                recivePong(this);
        }

        protected virtual void OnSendPing()
        {
            if (sendPing != null)
                sendPing(this);
        }

        protected virtual void OnError(WebSocketError aError)
        {
            if (error != null)
                error(this, aError);
        }

        protected virtual void OnOpenConnection(WebSocketHeaders aHeaders)
        {
            if (open != null)
                open(this, aHeaders);
        }

 
        protected virtual void OnCloseConnection(WebSocketCloseReason aReason)
        {
            if (close != null)
                close(this, aReason);
        }

        #endregion

        public void Close()
        {
            if (!WebSocketStateOfStatus.isWritable(mStatus))
                return;
            try
            {
                if (mLog.IsInfoEnabled)
                    mLog.Info(WebSocketMessage.CLOSE_CONNECTION);
                lock (mWriteLock)
                    SendCloseHandshake();
            }
            catch (Exception)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_CLOSE_CONNECTION);
                OnError(new WebSocketError(WebSocketMessage.ERROR_WHILE_CLOSE_CONNECTION));
            }
            StopIt();
        }

        private void SendCloseHandshake()
        {
            try
            {
                IWebSocketPacket lPacket = new WebSocketPacket(WebSocketFrameType.CLOSE, WebSocketMessage.BYE);
                lPacket.IsFragmented = true;
                lock (mWriteLock)
                    SendPacket(lPacket);
            }
            catch (IOException lIOEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_SENDING_CLOSE_HANDSHAKE
                        + WebSocketMessage.SEPARATOR + lIOEx.Message);
                OnError(new WebSocketError(WebSocketMessage.ERROR_WHILE_SENDING_CLOSE_HANDSHAKE));
                throw new WebSocketException(WebSocketMessage.ERROR_WHILE_SENDING_CLOSE_HANDSHAKE
                    + WebSocketMessage.SEPARATOR + lIOEx.Message);
            }
        }

        private void StopIt()
        {
            mIsRunning = false;
            mStatus = WebSocketStatus.CLOSED;
            mCLose = WebSocketCloseReason.CLIENT;

            try
            {
                mNetStream.Close();
                mHeaders.GetRequestFields.Clear();
                mHeaders.GetResponseFields.Clear();
            }
            catch (IOException lIOEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lIOEx.Message);
                OnError(new WebSocketError(lIOEx.Message));
                throw new WebSocketException(lIOEx.Message);
            }
        }

        private void Ping()
        {
            mPing = new Thread(new ThreadStart(SendPings));
            mPing.Start();
        }

        private void SendPings()
        {
            IWebSocketPacket lPacketPing = new WebSocketPacket(WebSocketFrameType.PING, WebSocketMessage.HELLO);
            lPacketPing.IsFragmented = true;

            try
            {
                Thread.Sleep(WebSocketConstants.DEFAULT_PING_DELAY);
                if (IsRunning)
                    WebSocketTimeout.CallWithTimeout(SendPacket, WebSocketConstants.DEFAULT_PING_TIMEOUT, lPacketPing);
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(WebSocketMessage.ERROR_WHILE_SENDING_PING);
                OnError(new WebSocketError(WebSocketMessage.ERROR_WHILE_SENDING_PING));
                throw new WebSocketException(WebSocketMessage.ERROR_WHILE_SENDING_PING
                    + WebSocketMessage.SEPARATOR + lEx);
            }
        }

        private void CreateSocket()
        {
            string lScheme = mURI.Scheme;
            string lHost = mURI.DnsSafeHost;
            int lPort = mURI.Port;

            if (lScheme.Equals(WebSocketConstants.SCHEME_WS))
            {
                if (lPort <= 0)
                    lPort = 80;
                try
                {
                    mSocket = new TcpClient(lHost, lPort);
                    mSocket.NoDelay = true;
                    mNetStream = mSocket.GetStream();
                }
                catch (SocketException)
                {
                    if (mLog.IsErrorEnabled)
                        mLog.Error(WebSocketMessage.UNKNOWN_HOST + WebSocketMessage.SEPARATOR + lHost);
                    OnError(new WebSocketError(WebSocketMessage.UNKNOWN_HOST + WebSocketMessage.SEPARATOR + lHost));
                    throw new WebSocketException(WebSocketMessage.UNKNOWN_HOST + WebSocketMessage.SEPARATOR + lHost,
                          WebSocketExceptionType.UNKNOWN_HOST);
                }
            }
            else
            {
                if (lScheme.Equals(WebSocketConstants.SCHEME_WSS))
                {
                    if (lPort <= 0)
                        lPort = 443;
                    OnError(new WebSocketError(WebSocketMessage.UNSUPPORTED_PROTOCOL));
                    throw new WebSocketException(WebSocketMessage.UNSUPPORTED_PROTOCOL + WebSocketMessage.SEPARATOR
                        + lScheme, WebSocketExceptionType.PROTOCOL_NOT_SUPPORTED);
                }
                else
                {
                    OnError(new WebSocketError(WebSocketMessage.UNSUPPORTED_PROTOCOL));
                    throw new WebSocketException(WebSocketMessage.UNSUPPORTED_PROTOCOL + WebSocketMessage.SEPARATOR
                        + lScheme, WebSocketExceptionType.PROTOCOL_NOT_SUPPORTED);
                }
            }
        }

        private string GenerateSubProtocolsHeaderValue()
        {
            if (mSubProtocols == null || mSubProtocols.Count == 0)
            {
                return WebSocketConstants.WS_SUBPROT_DEFAULT;
            }
            else
            {
                StringBuilder lBuff = new StringBuilder();
                foreach (WebSocketSubProtocol lProt in mSubProtocols)
                {
                    lBuff.Append(lProt.SubProtocol).Append(' ');
                }
                return lBuff.ToString().Trim();
            }
        }

        private int CalcFragmentCount(int aBuildSize, int aFragmentSize)
        {
            if (aBuildSize % aFragmentSize == 0)
                return aBuildSize / aFragmentSize;
            else
                return (aBuildSize / aFragmentSize) + 1;
        }

        #region Atributes publics

        public Dictionary<string, string> RequestHeader
        {
            get { return mHeaders.GetRequestFields; }
        }

        public Dictionary<string, string> ResponseHeader
        {
            get { return mHeaders.GetResponseFields; }
        }

        public string GetRequestHeaderField(string aFieldName)
        {
            return mHeaders.GetRequestField(aFieldName);
        }

        public string GetResponseHeaderField(string aFieldName)
        {
            return mHeaders.GetResponseField(aFieldName);
        }

        public bool IsRunning
        {
            get { return mIsRunning; }
        }

        #endregion

        private void AddSubProtocol(WebSocketSubProtocol aSubProtocol)
        {
            if (mSubProtocols == null)
                mSubProtocols = new List<WebSocketSubProtocol>();
            else
                mSubProtocols.Add(aSubProtocol);
        }

        private void CheckReconnect()
        {
            if (mReliabilityOptions!=null)
            {
                if (mReliabilityOptions.IsAutoReconnect)
                {
                    if (mLog.IsDebugEnabled)
                        mLog.Debug(WebSocketMessage.CHECKING_RECONNECT);
                    mStatus = WebSocketStatus.RECONNECTING;
                    mReconnect = new Thread(new ThreadStart(Reconnet));
                    mReconnect.Start();
                }
            }
        }

        private void Reconnet()
        {
            OnError(new WebSocketError(WebSocketMessage.TRYING_TO_CONNECT_TO_THE_SERVER));
            if (mReliabilityOptions.ReconnectDelay > 0)
                Thread.Sleep(mReliabilityOptions.ReconnectDelay);
            if (mReliabilityOptions.ReconnectTimeout > 0)
                Open(mURI.AbsoluteUri, GenerateSubProtocolsHeaderValue(), mReliabilityOptions.ReconnectTimeout);
        }
    }
}
