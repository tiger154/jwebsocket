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
using System.Net.Sockets;
using System.Net.Security;
using System.Security.Authentication;
using System.Security.Cryptography.X509Certificates;
using System.IO;
using System.Threading;
using ClientLibrary.org.jwebsocket.client.api;
using ClientLibrary.org.jwebsocket.client.config;
using ClientLibrary.org.jwebsocket.client.kit;
using ClientLibrary.org.jwebsocket.client.token;
using System.Diagnostics;


namespace ClientLibrary.org.jwebsocket.client.csharp
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketBaseClient : WebSocketClient
    {
        private Uri mURI;
        private TcpClient mSocket;
        private NetworkStream mNetStream;

        private List<WebSocketClientListener> mListeners = new List<WebSocketClientListener>();
        private List<WebSocketSubProtocol> mSubProtocols;
        protected WebSocketSubProtocol mNegotiatedSubProtocol;

        private WebSocketStatus mStatus = WebSocketStatus.CLOSED;
        private WebSocketEncoding mEncoding = WebSocketEncoding.TEXT;
        private WebSocketReliabilityOptions mReliabilityOptions = null;
        private WebSocketHeaders mHeaders = new WebSocketHeaders();
        private WebSocketCloseReason mCLose = WebSocketCloseReason.CLIENT;

        private string mCloseReason = null;
        private int mVersion = WebSocketConstants.WS_VERSION_DEFAULT;
        private object mWriteLock = new object();
        private bool mIsRunning = false;
        public static string CR_CLIENT = "Client closed connection";

        public WebSocketBaseClient() { }

        public WebSocketBaseClient(WebSocketReliabilityOptions aReliabilityOptions)
        {
            mReliabilityOptions = aReliabilityOptions;
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
                mCloseReason = "Connection could not be established.";
                mVersion = aVersion;
                mURI = new Uri(aURI);

                WebSocketHandshake lHandshake = new WebSocketHandshake(mURI, aSubProtocol, mVersion);

                if (mSocket != null && mSocket.Connected)
                    mSocket.Close();
                CreateSocket();

                byte[] sendBuffer = lHandshake.GenerateC2SRequest();
                mNetStream.Write(sendBuffer, 0, sendBuffer.Length);
                mNetStream.Flush();

                mStatus = WebSocketStatus.CONNECTING;

                mHeaders.ReadRequestFromBuffer(sendBuffer);
                WebSocketTimeout.CallWithTimeout(mHeaders.ReadResponseFromStream, aTimeout, mNetStream);
                lHandshake.VerifyS2CResponse(mHeaders);

                string lProtocol = GetResponseHeaderField(WebSocketConstants.SEC_WEBSOCKET_PROTOCOL);

                if (lProtocol != null)
                    mNegotiatedSubProtocol = new WebSocketSubProtocol(lProtocol, mEncoding);
                else
                    mNegotiatedSubProtocol = new WebSocketSubProtocol(WebSocketConstants.WS_SUBPROT_DEFAULT,
                        WebSocketConstants.WS_ENCODING_DEFAULT);

                Thread lReciver = new Thread(new ThreadStart(Receiver));
                lReciver.Start();

                mCloseReason = null;
                mStatus = WebSocketStatus.OPEN;
                OnOpen(mHeaders);
            }
            catch (TimeoutException lTE)
            {
                OnClose(WebSocketCloseReason.TIMEOUT);
                CheckReconnect();
            }
            catch (Exception lEx)
            {
                OnClose(WebSocketCloseReason.BROKEN);
                CheckReconnect();
            }
        }

        public void SendText(string aUTF8String)
        {
            lock (mWriteLock)
                SendPacketText(aUTF8String, -1);
        }

        public void SendText(string aUTF8String, int aFragmentSize)
        {
            lock (mWriteLock)
                SendPacketText(aUTF8String, aFragmentSize);
        }

        private void SendPacketText(string aUTF8String, int aFragmentSize)
        {
            if (!aFragmentSize.Equals(-1))
            {
                if (aFragmentSize <= WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendFragmentsText(aUTF8String, aFragmentSize);
                else
                    throw new WebSocketException("The Fragmentsize exceed default max frame size :" + WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
            }
            else
            {
                if (aUTF8String.Length > WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendFragmentsText(aUTF8String, WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
                else
                {
                    WebSocketPacket lPacket = new WebSocketRawPacket(WebSocketFrameType.TEXT, aUTF8String);
                    lPacket.SetEndFrame(true);
                    SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));
                }
            }
        }

        private void SendFragmentsText(string aUTF8String, int aFragmentSize)
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

                WebSocketPacket lPacket;
                if (lCurrentFragment.Equals(1))
                    lPacket = new WebSocketRawPacket(WebSocketFrameType.TEXT,
                            lBuild.ToString(lPos, aFragmentSize));
                else if (lCurrentFragment.Equals(lFragmentCount))
                {
                    lPacket = new WebSocketRawPacket(WebSocketFrameType.FRAGMENT,
                           lBuild.ToString(lPos, aFragmentSize));
                    lPacket.SetEndFrame(true);
                }
                else
                    lPacket = new WebSocketRawPacket(WebSocketFrameType.FRAGMENT,
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
            lock (mWriteLock)
                SendPacketBinary(aBinaryData, -1);
        }

        public void SendBinary(byte[] aBinaryData, int aFragmentSize)
        {
            lock (mWriteLock)
                SendPacketBinary(aBinaryData, aFragmentSize);
        }

        private void SendPacketBinary(byte[] aBinaryData, int aFragmentSize)
        {
            if (!aFragmentSize.Equals(-1))
            {
                if (aFragmentSize <= WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendfragmentsBinary(aBinaryData, aFragmentSize);
                else
                    throw new WebSocketException("The Fragmentsize exceed default max frame size :" + WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
            }
            else
            {
                if (aBinaryData.Length > WebSocketConstants.DEFAULT_MAX_FRAME_SIZE)
                    SendfragmentsBinary(aBinaryData, WebSocketConstants.DEFAULT_MAX_FRAME_SIZE);
                else
                {
                    WebSocketPacket lPacket = new WebSocketRawPacket(WebSocketFrameType.BINARY, aBinaryData);
                    lPacket.SetEndFrame(true);
                    SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));
                }
            }
        }

        private void SendfragmentsBinary(byte[] aBinaryData, int aFragmentSize)
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
                WebSocketPacket lPacket;
                if (lCurrentFragment.Equals(1))
                    lPacket = new WebSocketRawPacket(WebSocketFrameType.BINARY, lNewBinaryData);
                else if (lCurrentFragment.Equals(lFragmentCount))
                {
                    lPacket = new WebSocketRawPacket(WebSocketFrameType.FRAGMENT, lNewBinaryData);
                    lPacket.SetEndFrame(true);
                }
                else
                    lPacket = new WebSocketRawPacket(WebSocketFrameType.FRAGMENT, lNewBinaryData);

                SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));

                if (lPos + aFragmentSize > lBinaryDataSize)
                    lPos += lBinaryDataSize - lPos;
                else
                    lPos += aFragmentSize;
                lCurrentFragment++;
                Thread.Sleep((aFragmentSize * 1) / 100);
            }
        }

        private void SendPacket(WebSocketPacket aPacket)
        {
            try
            {
                lock (mWriteLock)
                    SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(aPacket));
            }
            catch (Exception lEx)
            {
                StopIt();
                throw new WebSocketException("Error while sending socket data: " + lEx);
            }
        }

        private void SendInternal(byte[] aData)
        {
            if (!WebSocketStateOfStatus.isWritable(mStatus))
                throw new WebSocketException("Error while sending binary data: not connected");
            try
            {
                lock (mWriteLock)
                {
                    mNetStream.Write(aData, 0, aData.Length);
                    mNetStream.Flush();
                }
            }
            catch (ObjectDisposedException lEx)
            {
                StopIt();
                throw new WebSocketException("Error while sending socket data: " + lEx);
            }
            catch (Exception lEx)
            {
                StopIt();
                throw new WebSocketException("Error while sending socket data: " + lEx);
            }

        }

        public void Receiver()
        {
            Thread.CurrentThread.Name = "jWebSocket-Client " + Thread.GetDomainID();

            mIsRunning = true;
            Ping(WebSocketConstants.DEFAULT_PING_TIMEOUT);
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
                throw new WebSocketException("Error while closing Socket :" + lIOEx.Message);
            }

            mStatus = WebSocketStatus.CLOSED;
            OnClose(mCLose);

            //if (!CR_CLIENT.Equals(mCloseReason))
            //    CheckReconnect();
        }

        private void ProcessHybi()
        {
            WebSocketFrameType lFrameType;
            WebSocketPacket lPacket;
            while (mIsRunning)
            {
                try
                {
                    lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(mNetStream);
                    lFrameType = (lPacket != null ? lPacket.GetFrameType() : WebSocketFrameType.INVALID);

                    if (lFrameType.Equals(null))
                    {
                        mIsRunning = false;
                        SetCloseReason("Connection broken");
                        mCLose = WebSocketCloseReason.BROKEN;
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.INVALID))
                    {
                        mIsRunning = false;
                        SetCloseReason("Invalid hybi frame type detected");
                        mCLose = WebSocketCloseReason.SERVER;
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.CLOSE))
                    {
                        mIsRunning = false;
                        SetCloseReason("Server closed connection");
                        mCLose = WebSocketCloseReason.SHUTDOWN;
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.PONG))
                    {
                        OnPong();
                        if (WebSocketStateOfStatus.isWritable(mStatus))
                        {
                            Ping(WebSocketConstants.DEFAULT_PING_TIMEOUT);
                            OnPing();
                        }
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.BINARY))
                    {
                        OnBinaryMessage(lPacket);
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.TEXT))
                    {
                        OnTextMessage(lPacket);
                    }
                    else if (lFrameType.Equals(WebSocketFrameType.FRAGMENT))
                    {
                        OnFragment(lPacket, 0, 0);
                    }
                }
                catch (Exception lEx)
                {
                    mIsRunning = false;
                    SetCloseReason(lEx.Source + " in hybi processor: " + lEx.Message);
                }
            }
        }

        public void Close()
        {
            if (!WebSocketStateOfStatus.isWritable(mStatus))
                return;
            try
            {
                lock (mWriteLock)
                    SendCloseHandshake();
            }
            catch (Exception lEx) { }
            StopIt();
        }

        public void SendCloseHandshake()
        {
            try
            {
                WebSocketPacket lPacket = new WebSocketRawPacket(WebSocketFrameType.CLOSE, "BYE");
                lPacket.SetEndFrame(true);
                lock (mWriteLock)
                    SendPacket(lPacket);
            }
            catch (IOException lIOEx)
            {
                throw new WebSocketException("Error while sending close handshake: " + lIOEx.Message);
            }
        }
        
        public void StopIt()
        {
            mIsRunning = false;
            mStatus = WebSocketStatus.CLOSED;
            SetCloseReason(CR_CLIENT);
            try
            {
                mNetStream.Close();
            }
            catch (IOException lIOEx)
            {
                throw new WebSocketException(lIOEx.Message);
            }
           
        }

        public void Ping(int aTimeout)
        {
            WebSocketPacket lPacketPing = new WebSocketRawPacket(WebSocketFrameType.PING, "Hello");
            lPacketPing.SetEndFrame(true);
            try
            {
                WebSocketTimeout.CallWithTimeout(SendPacket, aTimeout, lPacketPing);
            }
            catch (Exception lEx)
            {
                throw new WebSocketException("Error while sending Ping: " + lEx);
            }
        }

        private void CreateSocket()
        {
            string lScheme = mURI.Scheme;
            string lHost = mURI.DnsSafeHost;
            int lPort = mURI.Port;

            if (lScheme.Equals("ws"))
            {
                if (lPort <= 0)
                    lPort = 80;
                try
                {
                    mSocket = new TcpClient(lHost, lPort);
                    mSocket.NoDelay = true;
                    mNetStream = mSocket.GetStream();
                }
                catch (SocketException lUHEx)
                {
                    throw new WebSocketException("Unknown host: " + lHost,
                          WebSocketExceptionType.UNKNOWN_HOST);
                }
            }
            else
            {
                if (lScheme.Equals("wss"))
                {
                    if (lPort <= 0)
                        lPort = 443;

                    throw new WebSocketException("Unsupported protocol: " + lScheme,
                          WebSocketExceptionType.PROTOCOL_NOT_SUPPORTED);
                }
                else
                {
                    throw new WebSocketException("Unsupported protocol: " + lScheme,
                        WebSocketExceptionType.PROTOCOL_NOT_SUPPORTED);
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
                    lBuff.Append(lProt.GetSubProt()).Append(' ');
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

        public Dictionary<string, string> GetRequestHeader()
        {
            return mHeaders.GetRequestFields;
        }

        public Dictionary<string, string> GetResponseHeader()
        {
            return mHeaders.GetResponseFields;
        }

        public string GetRequestHeaderField(string aFieldName)
        {
            return mHeaders.GetRequestField(aFieldName);
        }

        public string GetResponseHeaderField(string aFieldName)
        {
            return mHeaders.GetResponseField(aFieldName);
        }

        public void AddSubProtocol(WebSocketSubProtocol aSubProtocol)
        {
            if (mSubProtocols == null)
                mSubProtocols = new List<WebSocketSubProtocol>();
            else
                mSubProtocols.Add(aSubProtocol);
        }

        public void AddListener(WebSocketClientListener aListener)
        {
            mListeners.Add(aListener);
        }

        public void RemoveListener(WebSocketClientListener aListener)
        {
            mListeners.Remove(aListener);
        }

        public List<WebSocketClientListener> GetLsteners()
        {
            return mListeners;
        }

        public void OnOpen(WebSocketHeaders aHeader)
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnOpen(aHeader);
            }
        }

        public void OnClose(WebSocketCloseReason aCloseReason)
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnClose(aCloseReason);
            }
        }

        public virtual void OnTextMessage(WebSocketPacket aDataPacket)
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnTextMessage(aDataPacket);
            }
        }

        public void OnBinaryMessage(WebSocketPacket aDataPacket)
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnBinaryMessage(aDataPacket);
            }
        }

        public void OnFragment(WebSocketPacket aFragment, int aIndex, int aTotal)
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnFragment(aFragment, aIndex, aTotal);
            }
        }

        public void OnPing()
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnPing();
            }
        }

        public void OnPong()
        {
            foreach (WebSocketClientListener lListener in mListeners)
            {
                lListener.ProcessOnPong();
            }
        }

        private void SetCloseReason(string aCloseReason)
        {
            mCloseReason = aCloseReason;
        }

        private void CheckReconnect()
        {
            if (!mReliabilityOptions.Equals(null) && mReliabilityOptions.IsAutoReconnect())
            {
                mStatus = WebSocketStatus.RECONNECTING;
                Thread lReconnect = new Thread(new ThreadStart(Reconnet));
                lReconnect.Start();
            }
        }

        private void Reconnet()
        {
            if (mReliabilityOptions.GetReconnectDelay() > 0)
                Thread.Sleep(mReliabilityOptions.GetReconnectDelay());
            if (mReliabilityOptions.GetReconnectTimeout() > 0)
                Open(mURI.AbsoluteUri, GenerateSubProtocolsHeaderValue(), mReliabilityOptions.GetReconnectTimeout());
        }

        public bool IsRunning()
        {
            return mIsRunning;
        }
    }
}
