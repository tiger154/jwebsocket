using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.protocol;
using WebSocket.org.jwebsocket.protocol.kit;
using WebSocket.org.jwebsocket.token;
using WebSocket.org.jwebsocket.token.kit;
using WebSocket.org.jwebsocket.token.api;

namespace ClientDemo
{
    public partial class Demo : Form
    {
        private WebSocketTokenClient mClient;
        public Demo()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            CheckForIllegalCrossThreadCalls = false;
        }

        private void rb_true_CheckedChanged(object sender, EventArgs e)
        {
            if (rb_true.Checked)
            {
                cb_timeout.Enabled = true;
                cb_delay.Enabled = true;
            }
        }

        private void rb_false_CheckedChanged(object sender, EventArgs e)
        {
            if (rb_false.Checked)
            {
                cb_timeout.Enabled = false;
                cb_delay.Enabled = false;
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            try
            {
                if (rb_true.Checked)
                {
                    mClient = new WebSocketTokenClient(new WebSocketReliabilityOptions(true, int.Parse(cb_delay.Text), int.Parse(cb_timeout.Text)));
                }
                else
                {
                    mClient = new WebSocketTokenClient();
                }

                mClient.open += Open;
                mClient.close += Close;
                mClient.error += Error;

                button3.Enabled = true;
                tb_URL.Enabled = true;
                button4.Enabled = false;
                button1.Enabled = false;
                button2.Enabled = true;
            }
            catch (Exception lEx)
            {
                MessageBox.Show("jWebSocket Client Demo", lEx.Message, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void Error(WebSocketTokenClient sender, WebSocketError e)
        {
            listBox1.Items.Add("Error: " + e.Reason);
        }

        private void Close(WebSocketTokenClient sender, WebSocketCloseReason e)
        {
            gb_send.Enabled = false;
            listBox1.Items.Add("Connection Closed : " + e.ToString());
        }

        private void Open(WebSocketTokenClient sender, WebSocketHeaders e)
        {
            gb_send.Enabled = true;
            button3.Enabled = false;
            tb_URL.Enabled = false;
            button4.Enabled = true;
            listBox1.Items.Add("Connection opened");
            listBox1.Items.Add("JWSSESSIONID = " + e.GetCookies[0].Split('=')[1].Split(';')[0]);
        }

        private void button2_Click(object sender, EventArgs e)
        {
            if (mClient.IsRunning)
            {
                mClient.Close();
                mClient = null;
            }
            button3.Enabled = false;
            tb_URL.Enabled = false;
            button2.Enabled = false;
            button1.Enabled = true;
            button4.Enabled = false;
        }

        private void button3_Click(object sender, EventArgs e)
        {
            mClient.Open(tb_URL.Text);
        }

        private void button4_Click(object sender, EventArgs e)
        {
            mClient.Close();
            tb_URL.Enabled = true;
            button3.Enabled = true;
            button4.Enabled = false;
        }

        private void button5_Click(object sender, EventArgs e)
        {
            IToken lToken= TokenFactory.CreateToken(WebSocketMessage.NS_SYSTEM_PLUGIN,"string");
            lToken.SetString("data","Hello");
            mClient.SendToken(lToken, StringResponse);
        }

        private void StringResponse(WebSocketTokenClient sender, TokenResponse e)
        {
            IToken lToken = e.TokenRecive;
            listBox1.Items.Add("Recived token [ type:"+lToken.GetType()+" - NS:"+lToken.GetNS()+" - utid:"+lToken.GetInt("utid")+" - data:"+lToken.GetString("data")+" ]");
        }

        private void button6_Click(object sender, EventArgs e)
        {
            IToken lToken = TokenFactory.CreateToken(WebSocketMessage.NS_SYSTEM_PLUGIN, "int");
            lToken.SetInt("data", 5);
            mClient.SendToken(lToken, IntegerResponse);
        }

        private void IntegerResponse(WebSocketTokenClient sender, TokenResponse e)
        {
            IToken lToken = e.TokenRecive;
            listBox1.Items.Add("Recived token [ type:" + lToken.GetType() + " - NS:" + lToken.GetNS() + " - utid:" + lToken.GetInt("utid") + " - data:" + lToken.GetInt("data") + " ]");
        }

        private void button7_Click(object sender, EventArgs e)
        {
            IToken lToken = TokenFactory.CreateToken(WebSocketMessage.NS_SYSTEM_PLUGIN, "double");
            lToken.SetDouble("data", 1.3);
            mClient.SendToken(lToken, DoubleResponse);
        }

        private void DoubleResponse(WebSocketTokenClient sender, TokenResponse e)
        {
            IToken lToken = e.TokenRecive;
            listBox1.Items.Add("Recived token [ type:" + lToken.GetType() + " - NS:" + lToken.GetNS() + " - utid:" + lToken.GetInt("utid") + " - data:" + lToken.GetDouble("data") + " ]");
        }

        private void button8_Click(object sender, EventArgs e)
        {
            IToken lToken = TokenFactory.CreateToken(WebSocketMessage.NS_SYSTEM_PLUGIN, "bool");
            lToken.SetBool("data", true);
            mClient.SendToken(lToken, BooleanResponse);
        }

        private void BooleanResponse(WebSocketTokenClient sender, TokenResponse e)
        {
            IToken lToken = e.TokenRecive;
            listBox1.Items.Add("Recived token [ type:" + lToken.GetType() + " - NS:" + lToken.GetNS() + " - utid:" + lToken.GetInt("utid") + " - data:" + lToken.GetBool("data") + " ]");
        }

        private void button9_Click(object sender, EventArgs e)
        {
            IToken lToken = TokenFactory.CreateToken(WebSocketMessage.NS_SYSTEM_PLUGIN, "list");
            List<object> lList = new List<object>();
            for (int i = 0; i < 5; i++)
            {
                lList.Add(i + 1);
            }
            lToken.SetList("data", lList);
            mClient.SendToken(lToken, ListResponse);
        }

        private void ListResponse(WebSocketTokenClient sender, TokenResponse e)
        {
            IToken lToken = e.TokenRecive;
            string llist = string.Empty;
            for (int i = 0; i < lToken.GetList("data").Count; i++)
            {
                llist += lToken.GetList("data")[i] + " ";
            }
            listBox1.Items.Add("Recived token [ type:" + lToken.GetType() + " - NS:" + lToken.GetNS() + " - utid:" + lToken.GetInt("utid") + " - data:" + llist + " ]");
        }

        private void button10_Click(object sender, EventArgs e)
        {
            IToken lToken = TokenFactory.CreateToken(WebSocketMessage.NS_SYSTEM_PLUGIN, "dictionary");
            Dictionary<string, object> lDic = new Dictionary<string, object>();
            lDic.Add("data", 6);
            
            lToken.SetDictionary("data", lDic);
            mClient.SendToken(lToken, DictResponse);
        }

        private void DictResponse(WebSocketTokenClient sender, TokenResponse e)
        {
            IToken lToken = e.TokenRecive;
            listBox1.Items.Add("Recived token [ type:" + lToken.GetType() + " - NS:" + lToken.GetNS() + " - utid:" + lToken.GetInt("utid") + " - data:" + lToken.GetDictionary("data") + " ]");
        }

        private void button12_Click(object sender, EventArgs e)
        {
            listBox1.Items.Clear();
        }

        private void button13_Click(object sender, EventArgs e)
        {
            if (mClient!=null && mClient.IsRunning)
                mClient.Close();
            Application.Exit();
        }

        private void Demo_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (mClient != null && mClient.IsRunning)
                mClient.Close();
            Application.Exit();
        }
    }
}
