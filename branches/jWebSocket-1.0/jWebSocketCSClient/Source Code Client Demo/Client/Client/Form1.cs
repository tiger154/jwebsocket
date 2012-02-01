using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using ClientLibrary.org.jwebsocket.client.csharp;
using ClientLibrary.org.jwebsocket.client.token;
using ClientLibrary.org.jwebsocket.client.api;
using ClientLibrary.org.jwebsocket.client.kit;

namespace Client
{
    public partial class Form1 : Form
    {
        private WebSocketBaseTokenClient mClient;
        public Form1()
        {
            InitializeComponent();
             
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            
            CheckForIllegalCrossThreadCalls = false;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (rb_true.Checked)
            {
                if (tb_delay.Text.Equals("") || tb_timeout.Text.Equals(""))
                    MessageBox.Show("must meet the data");
                else
                {
                    mClient = new WebSocketBaseTokenClient(new WebSocketReliabilityOptions(true, 5000, 5000));
                    mClient.AddListener(new MyListener(mClient, this));
                    mClient.Open(textBox1.Text);
                }
            }
            else if (rb_false.Checked)
            {
                mClient = new WebSocketBaseTokenClient();
                mClient.AddListener(new MyListener(mClient, this));
                mClient.Open(textBox1.Text);
            }
        }

        public ListBox myListBox1
        {
            get { return listBox1; }
            set { listBox1 = value; }
        }

        public ListBox myListBox2
        {
            get { return listBox2; }
            set { listBox2 = value; }
        }

        public Button SendButton
        {
            get { return button3; }
            set { button3 = value; }
        }

        public Button CloseButton
        {
            get { return button2; }
            set { button2 = value; }
        }

        public Button ConnectButton
        {
            get { return button1; }
            set { button1 = value; }
        }

        private void button3_Click(object sender, EventArgs e)
        {
            Token lMyToken = TokenFactory.CreateToken(WebSocketBaseTokenClient.NS_SYSTEM_PLUGIN, "echo");
            lMyToken.SetString("data",textBox2.Text);
            mClient.SendTokenText(lMyToken,new MyResponse(this));
            listBox1.Items.Add("Send Token - Type: " + lMyToken.GetType() + "- utid: " + lMyToken.GetInt("utid") + "- Data: " + lMyToken.GetString("data"));
        }

        private void button2_Click(object sender, EventArgs e)
        {
            mClient.Close();
        }

        private void Form1_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (mClient != null)
            {
                if (mClient.IsRunning())
                    mClient.Close();
            }
            Application.Exit();
        }

        private void button4_Click(object sender, EventArgs e)
        {
            listBox1.Items.Clear();
            listBox2.Items.Clear();
        }

        private void groupBox4_Enter(object sender, EventArgs e)
        {

        }

        private void rb_true_CheckedChanged(object sender, EventArgs e)
        {
            if (rb_true.Checked)
            {
                rb_false.Checked = false;
                tb_delay.Enabled = true;
                tb_timeout.Enabled = true;
            }
        }

        private void rb_false_CheckedChanged(object sender, EventArgs e)
        {
            if (rb_false.Checked)
            {
                rb_true.Checked = false;
                tb_delay.Enabled = false;
                tb_timeout.Enabled = false;
            }
        }
    }

    public class MyListener : WebSocketClientTokenListener
    {
        private WebSocketBaseTokenClient mClient;
        private Form1 mForm;

        public MyListener(WebSocketBaseTokenClient aClient,Form1 aForm)
        {
            this.mClient = aClient;
            this.mForm = aForm;
        }

        public void ProcessOnTokenText(WebSocketPacket aWebSocketPacket)
        {
           
        }

        public void ProcessOnBinaryMessage(WebSocketPacket aDataPacket)
        {
            
        }

        public void ProcessOnClose(WebSocketCloseReason aCloseReason)
        {
            try
            {
                mForm.myListBox1.Items.Add("Client close Connection");
                mForm.ConnectButton.Enabled = true;
                mForm.SendButton.Enabled = false;
                mForm.myListBox1.Items.Clear();
                mForm.myListBox2.Items.Clear();
            }
            catch (Exception lEx) { }
        }

        public void ProcessOnError(WebSocketError aError)
        {
            
        }

        public void ProcessOnFragment(WebSocketPacket aFragment, int aIndex, int aTotal)
        {
           
        }

        public void ProcessOnOpen(WebSocketHeaders aHeader)
        {
            mForm.SendButton.Enabled = true;
            mForm.CloseButton.Enabled = true;
            mForm.myListBox1.Items.Add("Client connected to Server");
            mForm.ConnectButton.Enabled = false;
        }

        public void ProcessOnPing()
        {
            mForm.myListBox2.Items.Add("Sending Ping");
        }

        public void ProcessOnPong()
        {
            mForm.myListBox2.Items.Add("Reciving Pong");
        }

        public void ProcessOnTextMessage(WebSocketPacket aDataPacket)
        {
            Token lToken = mClient.PacketToToken(aDataPacket);
            string lType = lToken.GetType();

            lock (mClient.MPendingResponseQueue)
            {
                if (!lType.Equals("welcome") && !lType.Equals("gooByte"))
                {
                    int lUTID = lToken.GetInt("utid");
                    int lCode = lToken.GetInt("code");

                    PendingResponseQueueItem lPRQI = mClient.MPendingResponseQueue[lUTID];
                    if (lPRQI != null)
                    {
                        WebSocketResponseTokenListener lWSRTL = lPRQI.GetListener();
                        if (lWSRTL != null)
                        {
                            lWSRTL.OnResponse(lToken);
                            if (lCode == 0)
                                lWSRTL.OnSuccess(lToken);
                            else
                                lWSRTL.OnFailure(lToken);
                        }
                        mClient.MPendingResponseQueue.Remove(lUTID);
                    }
                }
            }
        }
    }

    public class MyResponse : WebSocketResponseTokenListener
    {
        private Form1 mForm;

        public MyResponse(Form1 aForm)
        {
            this.mForm = aForm;
        }

        public void OnFailure(Token aToken)
        {
            mForm.myListBox1.Items.Add("The Server Response Failure");   
        }

        public void OnResponse(Token aToken)
        {
            
        }

        public void OnSuccess(Token aToken)
        {
            mForm.myListBox1.Items.Add("Receive Token - Type: " + aToken.GetType() + "- utid: " + aToken.GetInt("utid") + "- Data: " + aToken.GetString("data"));
        }

        public void OnTimeout(Token aToken)
        {
            throw new NotImplementedException();
        }

        public long getTimeout()
        {
            throw new NotImplementedException();
        }

        public void setTimeout(long aTimeout)
        {
            throw new NotImplementedException();
        }
    }
}
