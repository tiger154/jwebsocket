using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Microsoft.Phone.Controls;
using ClientLibrary.org.jwebsocket.client.api;
using ClientLibrary.org.jwebsocket.client.csharp;
using WebSocketWPClient;
using ClientLibrary.org.jwebsocket.client.token;
using ClientLibrary.org.jwebsocket.client.kit;
using System.Collections.ObjectModel;

namespace WebSocketEchoApp
{
    public partial class MainPage : PhoneApplicationPage, WebSocketClientListener
    {

        private WebSocketTokenClient _webSocketTokenClient;

        // Constructor
        public MainPage()
        {
            InitializeComponent();
            _webSocketTokenClient = new WebSocketTokenClient();
            _webSocketTokenClient.AddListener(this);
        }

        private void ConnectBtn_Click(object sender, RoutedEventArgs e)
        {
            StatusList.Items.Add("Connecting to server..");
            _webSocketTokenClient.Connect(UrlBox.Text);

           
        }

        private void PingBtn_Click(object sender, RoutedEventArgs e)
        {
            StatusList.Items.Add("Pinging Server..");
            _webSocketTokenClient.Ping();
        }

        private void Echo_Click(object sender, RoutedEventArgs e)
        {
            StatusList.Items.Add("sending echo token..");
            var token = TokenFactory.CreateToken(WebSocketTokenClient.NS_SYSTEM_PLUGIN, "echo");
            token.SetString("data", EchoMessage.Text);
            _webSocketTokenClient.SendTokenText(token);
        }

        private void DisconnectBtn_Click(object sender, RoutedEventArgs e)
        {
            StatusList.Items.Add("Disconnecting..");
            _webSocketTokenClient.Close();
        }

        public void ProcessOnTextMessage(WebSocketPacket packet)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                StatusList.Items.Add("Packet received from server.");
                var token = _webSocketTokenClient.PacketToToken(packet as WebSocketRawPacket);
                if (token.Type.ToUpperInvariant().Equals("WELCOME"))
                {
                    StatusList.Items.Add("welcome message received from server");
                    return;
                }
                EchoMessageResult.Text = token.GetString("data");
            });
            
            
        }

        public void ProcessOnBinaryMessage(WebSocketPacket aDataPacket)
        {
            
        }

        public void ProcessOnFragment(WebSocketPacket aFragment, int aIndex, int aTotal)
        {
            
        }

        public void ProcessOnOpen()
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                StatusList.Items.Add("Connection Successful.");
                PingBtn.IsEnabled = true;
                DisconnectBtn.IsEnabled = true;
                Echo.IsEnabled = true;
            });
            
        }

        public void ProcessOnClose(WebSocketCloseReason aCloseReason)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                StatusList.Items.Add("Disconnected!");
                PingBtn.IsEnabled = false;
                DisconnectBtn.IsEnabled = false;
                Echo.IsEnabled = false;
            });
        }

        public void ProcessOnError(WebSocketErrorEventArgs aError)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                StatusList.Items.Add("Error: " + aError.Message);
            });
            
        }

        public void ProcessOnPing()
        {
            
        }

        public void ProcessOnPong()
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                StatusList.Items.Add("Ping Response: Pong");
            });
        }
    }

    
}