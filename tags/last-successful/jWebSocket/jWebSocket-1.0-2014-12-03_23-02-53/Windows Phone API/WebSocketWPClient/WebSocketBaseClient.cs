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
using System.Net.Sockets;
using ClientLibrary.org.jwebsocket.client.kit;
using ClientLibrary.org.jwebsocket.client.config;
using ClientLibrary.org.jwebsocket.client.api;
using System.ComponentModel;
using System.Collections.ObjectModel;
using System.Threading;
using System.Diagnostics;
using System.Runtime.CompilerServices;

namespace WebSocketWPClient
{
    /// <summary>
    /// 
    /// </summary>
    public abstract class WebSocketBaseClient
    {

        private Uri _uri;
        private Socket _socket;
        private WebSocketSubProtocol _subProtocol;
        private BackgroundWorker _backgroundReceiver;
        private bool _isRunning;
        private WebSocketPacketReader _webSocketPacketReader;
        private WebSocketStatus _status = WebSocketStatus.CLOSED;

        protected WebSocketSubProtocol SubProtocol
        {
            get { return _subProtocol; }
            set { _subProtocol = value; }
        }

        private Collection<WebSocketClientListener> _listeners = new Collection<WebSocketClientListener>();

        /// <summary>
        /// Adds the instance of <see cref="WebSocketClientListener"/> to the collection of listeners.
        /// </summary>
        /// <param name="listener"></param>
        public void AddListener(WebSocketClientListener listener)
        {
            _listeners.Add(listener);
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="WebSocketBaseClient"/> class.
        /// </summary>
        internal WebSocketBaseClient() 
        {
            _backgroundReceiver = new BackgroundWorker { WorkerReportsProgress = true, WorkerSupportsCancellation = true };
            _backgroundReceiver.ProgressChanged += PacketReceived;
            _backgroundReceiver.RunWorkerCompleted += ReceiveCompleted;
            _backgroundReceiver.DoWork += ReceiveData;
        }

        #region public member functions

        /// <summary>
        /// The functions will connect the client to the specified websocket server.
        /// </summary>
        /// <param name="uri">The string represention of the uri of the web server to connect to.</param>
        public void Connect(string uri)
        {
            if (_status != WebSocketStatus.CLOSED)
            {
                return;
            }
            try
            {
                _status = WebSocketStatus.CONNECTING;
                _uri = new Uri(uri);
                var webSocketHandshake = new WebSocketHandshake(_uri, SubProtocol.ToString(), WebSocketConstants.WS_VERSION_DEFAULT);
                if (_socket != null && _socket.Connected)
                    _socket.Close();
                var scheme = _uri.Scheme;
                var port = _uri.Port;
                var host = _uri.Host;
                _socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                _socket.NoDelay = true;
                var hostEntry = new DnsEndPoint(host, port);
                // Create a SocketAsyncEventArgs object to be used in the connection request
                var socketEventArg = new SocketAsyncEventArgs();
                socketEventArg.RemoteEndPoint = hostEntry;
                socketEventArg.Completed += (sender, args) =>
                {
                    if (args.SocketError == SocketError.Success)
                    {
                        webSocketHandshake.PerformHandShakeWithServer(_socket, HandshakeCompleted, HandshakeFailed);
                    }
                    else
                    {
                        ProcessSocketError(args.SocketError);
                    }
                };
                _socket.ConnectAsync(socketEventArg);

            }
            catch (Exception ex)
            {
                var webSocketErrorArgs = new WebSocketErrorEventArgs { ErrorType = WebSocketExceptionType.UNABLE_TO_CONNECT, Message = ex.Message };
                OnError(webSocketErrorArgs);
            }

        }


        /// <summary>
        /// Pings the server.
        /// </summary>
        public void Ping()
        {
            WebSocketPacket pingPacket = new WebSocketRawPacket(WebSocketFrameType.PING, "Hello");
            pingPacket.SetEndFrame(true);
            SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(pingPacket));
        }

        /// <summary>
        /// Disconnects the client from server.
        /// </summary>
        public void Close()
        {
            WebSocketPacket pingPacket = new WebSocketRawPacket(WebSocketFrameType.CLOSE, "Bye");
            pingPacket.SetEndFrame(true);
            SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(pingPacket));
        }
        #endregion

        internal void SendText(string aUTF8String)
        {
            SendPacketText(aUTF8String, -1);
        }

        internal void SendText(string aUTF8String, int aFragmentSize)
        {
            SendPacketText(aUTF8String, aFragmentSize);
        }

        private void ProcessSocketError(SocketError socketError)
        {
            var webSocketErrorArgs = new WebSocketErrorEventArgs();
            if (socketError == SocketError.HostDown)
            {
                webSocketErrorArgs.ErrorType = WebSocketExceptionType.HOST_DOWN;
            }
            else if (socketError == SocketError.HostNotFound)
            {
                webSocketErrorArgs.ErrorType = WebSocketExceptionType.UNKNOWN_HOST;
            }
            else if (socketError == SocketError.HostUnreachable)
            {
                webSocketErrorArgs.ErrorType = WebSocketExceptionType.HOST_NOT_REACHABLE;
            }
            else
            {
                webSocketErrorArgs.ErrorType = WebSocketExceptionType.UNABLE_TO_CONNECT;
            }
            string message = "Error connecting to host: " + _uri.Host;
            OnError(webSocketErrorArgs);
            _status = WebSocketStatus.CLOSED;
        }

        private void HandshakeCompleted()
        {
            Debug.WriteLine("HandShake Completed");
            foreach (var listener in _listeners)
            {
                listener.ProcessOnOpen();
            }
            _status = WebSocketStatus.OPEN;
            //now start receiving data as a background thread
            _webSocketPacketReader = new WebSocketPacketReader(_socket);
            _isRunning = true;
            _backgroundReceiver.RunWorkerAsync();
        }

        private void HandshakeFailed()
        {
            var webSocketErrorArgs = new WebSocketErrorEventArgs { ErrorType = WebSocketExceptionType.OPENING_HANDSHAKE_FAILED, Message = "Opening handshake failed."};
            OnError(webSocketErrorArgs);
        }

        private void OnError(WebSocketErrorEventArgs eventArgs)
        {
             _status = WebSocketStatus.CLOSED;
            foreach (var listener in _listeners)
            {
                listener.ProcessOnError(eventArgs);
            }
           
        }
        private void SendPacketText(string aUTF8String, int aFragmentSize)
        {
            WebSocketPacket lPacket = new WebSocketRawPacket(WebSocketFrameType.TEXT, aUTF8String);
            lPacket.SetEndFrame(true);
            SendInternal(WebSocketProtocolAbstraction.RawToProtocolPacket(lPacket));
        }

        private void SendInternal(byte[] data)
        {
            var sendPacketEventArg = new SocketAsyncEventArgs();
            sendPacketEventArg.RemoteEndPoint = _socket.RemoteEndPoint;
            sendPacketEventArg.SetBuffer(data, 0, data.Length);



            sendPacketEventArg.Completed += (sender, args) =>
                                                            {
                                                                if (args.SocketError == SocketError.Success)
                                                                {
                                                                   
                                                                }
                                                            };
            _socket.SendAsync(sendPacketEventArg);
        }

        private void ReceiveCompleted(object sender, RunWorkerCompletedEventArgs args)
        {

        }

        private void PacketReceived(object sender, ProgressChangedEventArgs args)
        {
            var packet = args.UserState as WebSocketRawPacket;
            foreach (var listener in _listeners)
            {
                if (packet.GetFrameType() == WebSocketFrameType.TEXT)
                {
                    listener.ProcessOnTextMessage(packet);
                }
                else if (packet.GetFrameType() == WebSocketFrameType.PONG)
                {
                    listener.ProcessOnPong();
                }
                else if (packet.GetFrameType() == WebSocketFrameType.CLOSE)
                {
                    listener.ProcessOnClose(WebSocketCloseReason.SHUTDOWN);
                }
            }
        }
        

        private void ReceiveData(object sender, DoWorkEventArgs args)
        {
            while (_isRunning)
            {
                try
                {
                    Debug.WriteLine("reading packet");
                    var packet = _webSocketPacketReader.ReadPacket();
                    Debug.WriteLine(packet);
                    var frameType = packet != null ? packet.GetFrameType() : WebSocketFrameType.INVALID;
                    Debug.WriteLine(frameType);
                    switch (frameType)
                    {
                        case WebSocketFrameType.TEXT:
                            _backgroundReceiver.ReportProgress(0, packet);
                            break;
                        case WebSocketFrameType.BINARY:
                            _backgroundReceiver.ReportProgress(0, packet);
                            break;
                        case WebSocketFrameType.INVALID:
                            _isRunning = false;
                            OnError(new WebSocketErrorEventArgs { ErrorType = WebSocketExceptionType.INVALID_FRAME_TYPE, Message = "Invalid frame." });
                            break;
                        case WebSocketFrameType.CLOSE:
                            _isRunning = false;
                            _backgroundReceiver.ReportProgress(0, packet);
                            break;
                        case WebSocketFrameType.PONG:
                            _backgroundReceiver.ReportProgress(0, packet);                            
                            break;
                    }
                }
                catch (Exception ex)
                {
                    _isRunning = false;
                    OnError(new WebSocketErrorEventArgs { Message = ex.Message });
                }
            }

        }
    }
}
