using System;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Security.Cryptography;
using ClientLibrary.org.jwebsocket.client.kit;
using System.Net.Sockets;
using ClientLibrary.org.jwebsocket.client.config;
using System.Collections.ObjectModel;
using System.Collections.Generic;
using System.Diagnostics;

namespace WebSocketWPClient
{
    internal class WebSocketHandshake
    {
        private Uri _uri = null;
        private string _hybiKey = null;
        private string _expectedHybiResponseKey = null;
        private string _protocol = null;
        private int _version;
        private Random _random = new Random();

        private Action _handshakeCompleted;
        private Action _handshakeFailed;

        internal WebSocketHandshake(Uri uri, string protocol, int version)
        {
            _uri = uri;
            _protocol = protocol;
            _version = version;
            GenerateHybiKeys();
        }

        private void GenerateHybiKeys()
        {
            Guid uid = Guid.NewGuid();
            byte[] uidByteArray = uid.ToByteArray();
            _hybiKey = Convert.ToBase64String(uidByteArray);
            _expectedHybiResponseKey = CalcHybiSecKeyAccept();
        }

        private string CalcHybiSecKeyAccept()
        {
            var key = _hybiKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            string acceptedResponseKey = null;
            var sha1 = new SHA1Managed();
            byte[] keyByteArray = WebSocketConvert.StringToBytes(key, WebSocketTypeEncoding.UTF8);
            try
            {
                byte[] hashedKeyByteArray = sha1.ComputeHash(keyByteArray);
                acceptedResponseKey = Convert.ToBase64String(hashedKeyByteArray);
            }
            catch (Exception)
            {
                //TODO - handle exception
            }
            return acceptedResponseKey;
        }

        private byte[] GenerateC2SRequest()
        {
            string host = _uri.DnsSafeHost;
            
            string path = _uri.AbsolutePath;
            if (!string.IsNullOrEmpty(_uri.Query))
            {
                path += "?" + _uri.Query;
            }
            var origin = "http://" + host;

            if ("".Equals(path))
                path = "/";

            string handShake =
                     "GET " + path + " HTTP/1.1 \r\n"
                   + "Host: " + host + "\r\n"
                   + "Upgrade: WebSocket\r\n"
                   + "Connection: Upgrade\r\n"
                   + "Sec-WebSocket-Key: " + _hybiKey + "\r\n"
                   + "Origin: " + origin + "\r\n"
                   + "Sec-WebSocket-Protocol: " + _protocol + "\r\n"
                   + "Sec-WebSocket-Version: " + _version + "\r\n\r\n";

            return WebSocketConvert.StringToBytes(handShake, WebSocketTypeEncoding.UTF8); ;
        }

        internal void PerformHandShakeWithServer(Socket socket, Action completed, Action failed)
        {
            try
            {
                _handshakeCompleted = completed;
                _handshakeFailed = failed;
                var handshakeBuffer = GenerateC2SRequest();
                var sendHandshakeEventArg = new SocketAsyncEventArgs();
                sendHandshakeEventArg.RemoteEndPoint = socket.RemoteEndPoint;
                sendHandshakeEventArg.SetBuffer(handshakeBuffer, 0, handshakeBuffer.Length);

                var receiveHandshakeResponseEventArg = new SocketAsyncEventArgs();
                receiveHandshakeResponseEventArg.RemoteEndPoint = socket.RemoteEndPoint;
                receiveHandshakeResponseEventArg.SetBuffer(new Byte[WebSocketConstants.MAX_RECEIVE_BUFFER_SIZE], 0, WebSocketConstants.MAX_RECEIVE_BUFFER_SIZE);
                var handshakeResponse = new Collection<byte>();

                sendHandshakeEventArg.Completed += (sender, args) =>
                {
                    if (args.SocketError == SocketError.Success)
                    {
                        socket.ReceiveAsync(receiveHandshakeResponseEventArg);
                    }
                };
                receiveHandshakeResponseEventArg.Completed += (sender, args) =>
                {
                    if (args.SocketError == SocketError.Success)
                    {
                        var handshakeComplete = false;
                        for (int i = 0; i < args.BytesTransferred; i++)
                        {
                            handshakeResponse.Add(args.Buffer[i]);
                            if (handshakeResponse.Count > 4 && ((int)handshakeResponse[handshakeResponse.Count - 4]).Equals(0x0D) && ((int)handshakeResponse[handshakeResponse.Count - 3]).Equals(0x0A) && ((int)handshakeResponse[handshakeResponse.Count - 2]).Equals(0x0D) && ((int)handshakeResponse[handshakeResponse.Count - 1]).Equals(0x0A))
                            {
                                handshakeComplete = true;
                                break;
                            }
                        }
                        if (handshakeComplete)
                        {
                            try
                            {
                                VerifyS2CResponse(handshakeResponse.ToArray());
                                completed();
                            }
                            catch (WebSocketException)
                            {
                                //TODO - log exception
                                failed();
                            }


                        }
                        else
                        {
                            socket.ReceiveAsync(receiveHandshakeResponseEventArg);
                        }
                    }
                    else
                    {
                        failed();
                    }
                };
                Debug.WriteLine("Sending Handshake");
                socket.SendAsync(sendHandshakeEventArg);
            }
            catch (Exception)
            {
                //TODO - log exception
                failed();
            }
            
            
        }

        private void VerifyS2CResponse(byte[] handshakeResponse)
        {
            var handshakeResponseString = WebSocketConvert.BytesToString(handshakeResponse, WebSocketTypeEncoding.UTF8);
            var responseFields = new Dictionary<string, string>();
            var headers = handshakeResponseString.Split(new[] { "\r\n" }, StringSplitOptions.RemoveEmptyEntries);
            var firstlineResponse = headers[0];
            foreach (var headerLine in headers.Skip(1))
            {
                var headerKeyValue = headerLine.Split(new[] { ":" }, StringSplitOptions.None);
                responseFields.Add(headerKeyValue[0].Trim(), headerKeyValue[1].Trim());
            }
            if (!firstlineResponse.Equals("HTTP/1.1 101 Switching Protocols"))
                throw new WebSocketException("connection failed: missing header field in server handshake: HTTP/1.1");
            if (!responseFields[WebSocketConstants.UPGRADE].Equals("websocket"))
                throw new WebSocketException("connection failed: missing header field in server handshake: Upgrade");
            if (!responseFields[WebSocketConstants.CONNECTION].Equals("Upgrade"))
                throw new WebSocketException("connection failed: missing header field in server handshake: Connection");
            if (!responseFields[WebSocketConstants.SEC_WEBSOCKET_ACCEPT].Equals(_expectedHybiResponseKey))
                throw new WebSocketException("connection failed: missing header field in server handshake: Sec-WebSocket-Key");
            if (!responseFields[WebSocketConstants.SEC_WEBSOCKET_PROTOCOL].Equals(_protocol))
                throw new WebSocketException("connection failed: missing header field in server handshake: Sec-WebSocket-Protocol");
        }

    }
}
