using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Linq;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using ClientLibrary.org.jwebsocket.client.api;
using System.Net.Sockets;
using System.Threading;
using ClientLibrary.org.jwebsocket.client.kit;
using System.Runtime.CompilerServices;
using System.Diagnostics;

namespace WebSocketWPClient
{
    internal class WebSocketPacketReader
    {
        private Socket _socket;
        private ManualResetEvent _asyncCallDone;

        internal WebSocketPacketReader(Socket socket)
        {
            _socket = socket;
            
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        internal WebSocketPacket ReadPacket()
        {
            var receivePacketEventArg = new SocketAsyncEventArgs();
            receivePacketEventArg.RemoteEndPoint = _socket.RemoteEndPoint;
            Debug.WriteLine("Reading metadata");
            var flagMaskAndPayloadMetadata = ReadDataFromSocket(receivePacketEventArg, 2);
            //var fragmented = false;
            //read the first byte
            var flag = (int)flagMaskAndPayloadMetadata[0];
            //if ((flag & 0x80) == 0x00)
            //{
            //    fragmented = true;
            //}
            byte[] payload = null;
            bool masked = true;
            byte[] mask = null;
            var opcode = flag & 0x0F;

            WebSocketFrameType frameType = (WebSocketFrameType)opcode;
            if (frameType == WebSocketFrameType.INVALID)
            {
                //TODO - throw exception
            }
            else
            {
                Debug.WriteLine("Reading payload length");
                //read the payload length
                var payLoadLength = (int)flagMaskAndPayloadMetadata[1];
                masked = (payLoadLength & 0x80) == 0x80;
                payLoadLength &= 0x7F;
                if (payLoadLength == 126)
                {
                    var payLoadMetaData = ReadDataFromSocket(receivePacketEventArg, 2);
                    payLoadLength = ((int)payLoadMetaData[0]) & 0xFF;
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData[1]) & 0xFF);
                }
                else if (payLoadLength == 127)
                {
                    var payLoadMetaData1 = ReadDataFromSocket(receivePacketEventArg, 8);
                    payLoadLength = payLoadMetaData1[0] & 0xFF;
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[1]) & 0xFF);
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[2]) & 0xFF);
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[3]) & 0xFF);
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[4]) & 0xFF);
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[5]) & 0xFF);
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[6]) & 0xFF);
                    payLoadLength = (payLoadLength << 8) ^ (((int)payLoadMetaData1[7]) & 0xFF);
                }
                if (masked)
                {
                    mask = ReadDataFromSocket(receivePacketEventArg, 4);
                }
                if (payLoadLength > 0)
                {
                    payload = ReadDataFromSocket(receivePacketEventArg, payLoadLength);
                    if (masked)
                    {
                        //TODO - implement for masking
                    }
                }
            }
            return new WebSocketRawPacket(frameType, payload);

        }

        private byte[] ReadDataFromSocket(SocketAsyncEventArgs receivePacketEventArg, int byteCountToRead)
        {
            _asyncCallDone = new ManualResetEvent(false);
            receivePacketEventArg.SetBuffer(new Byte[byteCountToRead], 0, byteCountToRead);
            byte[] result = null;
            receivePacketEventArg.Completed += (object sender, SocketAsyncEventArgs args)=>
                                            {
                                                if (args.SocketError == SocketError.Success)
                                                {
                                                    result = args.Buffer;
                                                }
                                                else
                                                {
                                                    Debug.WriteLine(args.SocketError.ToString());
                                                }
                                                _asyncCallDone.Set();
                                            };
            try
            {
                _socket.ReceiveAsync(receivePacketEventArg);
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }
            
            _asyncCallDone.WaitOne();
            return result;
        }
    }
}
