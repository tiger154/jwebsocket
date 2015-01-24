/*--------------------------------------------------------------------------------
 * jWebSocket - IWebSocketProtocol
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
using WebSocket.org.jwebsocket.protocol.kit;

namespace WebSocket.org.jwebsocket.protocol.api
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// API for low level client 
    /// </summary>
    public interface IWebSocketProtocol
    {
        /// <summary>
        /// Establish a connection to a websocket server.
        /// </summary>
        /// <param name="aURL">Server URL.</param>
        void Open(string aURL);

        /// <summary>
        /// Establish a connection to a websocket server.
        /// </summary>
        /// <param name="aURL">Server URL.</param>
        /// <param name="aSubProtocol">WebSocket protocol specification.</param>
        void Open(string aURL, string aSubProtocol);

        /// <summary>
        /// Establish a connection to a websocket server.
        /// </summary>
        /// <param name="aURL">Server URL.</param>
        /// <param name="aSubProtocol">WebSocket protocol specification.</param>
        /// <param name="aTimeout">Timeout for close the conection.</param>
        void Open(string aURL, string aSubProtocol, int aTimeout);

        /// <summary>
        /// Send a complete packet as UTF8 string.
        /// </summary>
        /// <param name="aUTF8String">Packet as UTF8 String.</param>
        void SendText(string aUTF8String);

        /// <summary>
        /// Send packet UTF8 string as multiple fragments.
        /// </summary>
        /// <param name="aUTF8String">Packet as UTF8 String.</param>
        /// <param name="aFragmentSize">Maximum fragment size.</param>
        void SendText(string aUTF8String, int aFragmentSize);

        /// <summary>
        /// Send a complete packet as binary data.
        /// </summary>
        /// <param name="aBinaryData">Binary data.</param>
        void SendBinary(byte[] aBinaryData);

        /// <summary>
        /// Send packet binary Data as multiple fragments.
        /// </summary>
        /// <param name="aBinaryData">Binary data.</param>
        /// <param name="aFragmentSize">Maximum fragment size.</param>
        void SendBinary(byte[] aBinaryData, int aFragmentSize);

        /// <summary>
        /// Send a ping frame to the server and starts a timeout observer.
        /// </summary>
        /// <param name="aTimeout">Timeout for close the conection.</param>
        //void Ping();

        /// <summary>
        /// Called when a text message has been received.
        /// </summary>
        /// <param name="aDataPacket">Data packet received .</param>
       // void OnTextMessage(WebSocketPacket aDataPacket);

        /// <summary>
        /// Called when a binary message has been received.
        /// </summary>
        /// <param name="aDataPacket">Data packet received.</param>
       /// void OnBinaryMessage(WebSocketPacket aDataPacket);

        /// <summary>
        /// Called when a fragment has been received.
        /// </summary>
        /// <param name="aFragment">Data packet fragment received.</param>
        /// <param name="aIndex">Index of fragment.</param>
        /// <param name="aTotal">Total size of fragment.</param>
     //   void OnFragment(WebSocketPacket aFragment, int aIndex, int aTotal);

        /// <summary>
        /// Called when connection to a websocket server has been Establish.
        /// </summary>
        /// <param name="aHeader">Header fields from the handshake.</param>
        //void OnOpen(WebSocketHeaders aHeader);

        /// <summary>
        /// Called when connection to a websocket server has been closed.
        /// </summary>
        /// <param name="aCloseReason">Represent the reason of the disconnect.</param>
     //   void OnClose(WebSocketCloseReason aCloseReason);

        /// <summary>
        /// Called when occurs any error.
        /// </summary>
        /// <param name="aError">Represents the error occurred.</param>
       // void OnError(WebSocketError aError);

        /// <summary>
        /// Called when client sent a ping.
        /// </summary>
     //   void OnPing();

        /// <summary>
        /// Called when server sent a pong.
        /// </summary>
      //  void OnPong();

        /// <summary>
        /// Adds the listener.
        /// </summary>
        /// <param name="aListener">A listener.</param>
     //   void AddListener(WebSocketClientListener aListener);

        /// <summary>
        /// Removes the listener.
        /// </summary>
        /// <param name="aListener">A listener.</param>
     //   void RemoveListener(WebSocketClientListener aListener);

    }
}
