//	---------------------------------------------------------------------------
//	jWebSocket - TestDialog (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

/*
 * TestDialog.java
 *
 * Created on Mar 15, 2010, 2:55:47 PM
 */
package org.jwebsocket.ui;

import java.awt.Toolkit;
import java.io.*;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jwebsocket.JMSClient.JMSClientDialog;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.client.java.JWebSocketHTTPClient;
import org.jwebsocket.client.java.JWebSocketJMSClient;
import org.jwebsocket.client.token.JWebSocketTokenClient;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.lb.LoadBalancerDialog;
import org.jwebsocket.plugins.rpc.SampleRPCObject;
import org.jwebsocket.tests.StressTests;
import org.jwebsocket.token.BaseTokenResponseListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.OutputStreamConsole;
import tld.yourname.jms.server.JMSServer;

/**
 * Java Swing client for jWebSocket
 *
 * @author Alexander Schulze
 * @version $Id:$
 */
public class TestDialog extends javax.swing.JFrame implements WebSocketClientTokenListener {

	private static final long serialVersionUID = 1L;
	private JWebSocketTokenClient mClient = null;
	private WebSocketStatus mPrevStatus = WebSocketStatus.CLOSED;
	private ImageIcon mIcoDisconnected = null;
	private ImageIcon mIcoConnected = null;
	private ImageIcon mIcoAuthenticated = null;
	private ReliabilityOptions mReliabilityOptions = null;
	private JMSClientDialog mJMSClient;
	private boolean mJMSServerIsRunning = false;
	private Thread mJMSServerThread;
	private Thread mStressTestsThread;
	private Properties mJMSServerProperties;

	/**
	 * Creates new form TestDialog
	 */
	public TestDialog() {
		initComponents();
		try {
			lblTitle.setText(lblTitle.getText().replace("{ver}", JWebSocketClientConstants.VERSION_STR));
			mReliabilityOptions = new ReliabilityOptions(true, 1500, 3000, 1, -1);
			mIcoDisconnected = new ImageIcon(getClass().getResource("/images/disconnected.png"));
			mIcoConnected = new ImageIcon(getClass().getResource("/images/connected.png"));
			mIcoAuthenticated = new ImageIcon(getClass().getResource("/images/authenticated.png"));
			checkStatusIcon();
			initializeLogs();
		} catch (Exception ex) {
			System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void initializeLogs() {
		PrintStream lPrintStream;
		OutputStreamConsole lConsole = new OutputStreamConsole(txaLog);
		lPrintStream = new PrintStream(lConsole);
		System.setOut(lPrintStream);
		System.setErr(lPrintStream);
	}

	private void checkStatusIcon() {
		WebSocketStatus lStatus = (null != mClient) ? mClient.getStatus() : WebSocketStatus.CLOSED;
		String lClientId = (null != mClient) ? mClient.getClientId() : "";
		lblStatus.setText("Client-Id: " + (lClientId != null ? lClientId : "-"));

		if (lStatus != mPrevStatus) {

			if (lStatus == WebSocketStatus.AUTHENTICATED) {
				lblStatus.setIcon(mIcoAuthenticated);
				mPrevStatus = lStatus;
			} else if (lStatus == WebSocketStatus.OPEN
					&& mPrevStatus == WebSocketStatus.AUTHENTICATED) {
				lblStatus.setIcon(mIcoAuthenticated);
			} else if (lStatus == WebSocketStatus.OPEN) {
				lblStatus.setIcon(mIcoConnected);
				mPrevStatus = lStatus;
			} else {
				lblStatus.setIcon(mIcoDisconnected);
				mPrevStatus = lStatus;
			}
		}
	}

	private void mLog(String aMessage) {
		synchronized (txaLog) {
			try {
				int lMAX = 1000;
				int lLineCount = txaLog.getLineCount();

				if (lLineCount > lMAX) {
					int lLinePosStart = txaLog.getLineEndOffset(0);
					int lLinePosEnd = txaLog.getLineEndOffset(lMAX);
					String lTextToReplace = txaLog.getText(lLinePosStart, txaLog.getText().length() - lLinePosStart);
					txaLog.replaceRange(lTextToReplace, 0, lLinePosEnd);
				}

				if (null != aMessage) {
					System.out.println(aMessage);
				}

			} catch (BadLocationException ex) {
				mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
			}
		}
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
		mLog("Opening...");
		checkStatusIcon();
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		mLog("Opened.");
		checkStatusIcon();
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		// ignore that here
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		String lReqType = aToken.getString("reqType", "");
		if (!(lReqType.equals("clustersInfo")
				|| lReqType.equals("stickyRoutes")
				|| lReqType.equals("registerServiceEndPoint")
				|| lReqType.equals("shutdownServiceEndPoint")
				|| lReqType.equals("deregisterServiceEndPoint")
				|| lReqType.equals("changeAlgorithm"))) {
			mLog("Received Token: " + aToken.toString());
		}
		checkStatusIcon();
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		mLog("Closed (" + aEvent.getData() + ").");
		checkStatusIcon();
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
		mLog("Reconnecting...");
		checkStatusIcon();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        bntSend = new javax.swing.JButton();
        scpTextArea = new javax.swing.JScrollPane();
        txaLog = new javax.swing.JTextArea();
        btnClearLog = new javax.swing.JButton();
        txfMessage = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        btnBroadcast = new javax.swing.JButton();
        btnPing = new javax.swing.JButton();
        btnShutdown = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        txfTarget = new javax.swing.JTextField();
        lblTarget = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnGetSessions = new javax.swing.JButton();
        btnGetUserRights = new javax.swing.JButton();
        btnGetUserRoles = new javax.swing.JButton();
        lblURL = new javax.swing.JLabel();
        txfURL = new javax.swing.JTextField();
        btnRPC = new javax.swing.JButton();
        lblUsername = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        txfUser = new javax.swing.JTextField();
        pwfPassword = new javax.swing.JPasswordField();
        btnTimeout = new javax.swing.JButton();
        txfTimeout = new javax.swing.JTextField();
        lblReconnect = new javax.swing.JLabel();
        lblTimeout = new javax.swing.JLabel();
        txfReconnectDelay = new javax.swing.JTextField();
        lblConnection = new javax.swing.JLabel();
        lblAuth = new javax.swing.JLabel();
        lblSend = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnStressTest = new javax.swing.JButton();
        btnDebugOut = new javax.swing.JButton();
        btnGC = new javax.swing.JButton();
        lblFilename = new javax.swing.JLabel();
        btnUpload = new javax.swing.JButton();
        txfFilename = new javax.swing.JTextField();
        chkJMSClient = new javax.swing.JCheckBox();
        txfClusterName = new javax.swing.JTextField();
        jcbWrap = new javax.swing.JCheckBox();
        btnSaveLog = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 50), new java.awt.Dimension(10, 50), new java.awt.Dimension(10, 50));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(50, 10), new java.awt.Dimension(50, 10), new java.awt.Dimension(50, 10));
        mnbMain = new javax.swing.JMenuBar();
        pmnFile = new javax.swing.JMenu();
        mniExit = new javax.swing.JMenuItem();
        mniPreferences = new javax.swing.JMenuItem();
        pmnTests = new javax.swing.JMenu();
        mniConnect = new javax.swing.JMenuItem();
        mniDisconnect = new javax.swing.JMenuItem();
        mniSend = new javax.swing.JMenuItem();
        mniBroadcast = new javax.swing.JMenuItem();
        pmnLoadBalancer = new javax.swing.JMenu();
        mniView = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jmJMSServer = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jmiViewJMSCLient = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jWebSocket Fundamental Demo");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Synapso16x16.png")));
        setMinimumSize(new java.awt.Dimension(640, 490));
        setPreferredSize(new java.awt.Dimension(830, 700));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        lblTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Synapso32x32.png"))); // NOI18N
        lblTitle.setText("jWebSocket Java Client {ver}");
        lblTitle.setToolTipText("jWebSocket - the open source solution for real-time developers");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 18;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 166;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        getContentPane().add(lblTitle, gridBagConstraints);

        bntSend.setText("Send");
        bntSend.setToolTipText("Sends a message to the clients identified by the target field.");
        bntSend.setMaximumSize(new java.awt.Dimension(100, 20));
        bntSend.setMinimumSize(new java.awt.Dimension(100, 20));
        bntSend.setPreferredSize(new java.awt.Dimension(100, 20));
        bntSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntSendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        getContentPane().add(bntSend, gridBagConstraints);

        txaLog.setColumns(20);
        txaLog.setEditable(false);
        txaLog.setRows(5);
        scpTextArea.setViewportView(txaLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.gridheight = 43;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 288;
        gridBagConstraints.ipady = 279;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        getContentPane().add(scpTextArea, gridBagConstraints);

        btnClearLog.setText("Clear");
        btnClearLog.setToolTipText("Clears the log window.");
        btnClearLog.setMaximumSize(new java.awt.Dimension(100, 20));
        btnClearLog.setMinimumSize(new java.awt.Dimension(100, 20));
        btnClearLog.setPreferredSize(new java.awt.Dimension(100, 20));
        btnClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearLogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        getContentPane().add(btnClearLog, gridBagConstraints);

        txfMessage.setText("Message");
        txfMessage.setMinimumSize(new java.awt.Dimension(100, 20));
        txfMessage.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        getContentPane().add(txfMessage, gridBagConstraints);

        btnConnect.setText("Connect");
        btnConnect.setToolTipText("Connects to the Websocket Server at the given URL.");
        btnConnect.setMaximumSize(new java.awt.Dimension(100, 20));
        btnConnect.setMinimumSize(new java.awt.Dimension(100, 20));
        btnConnect.setPreferredSize(new java.awt.Dimension(100, 20));
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(btnConnect, gridBagConstraints);

        btnDisconnect.setText("Disconnect");
        btnDisconnect.setToolTipText("Disconnects from the WebSocket Server.");
        btnDisconnect.setMaximumSize(new java.awt.Dimension(100, 20));
        btnDisconnect.setMinimumSize(new java.awt.Dimension(100, 20));
        btnDisconnect.setPreferredSize(new java.awt.Dimension(100, 20));
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 8);
        getContentPane().add(btnDisconnect, gridBagConstraints);

        btnSend.setText("Send");
        btnSend.setToolTipText("Sends a message to the clients identified by the target field.");
        btnSend.setMaximumSize(new java.awt.Dimension(100, 20));
        btnSend.setMinimumSize(new java.awt.Dimension(100, 20));
        btnSend.setPreferredSize(new java.awt.Dimension(100, 20));
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        getContentPane().add(btnSend, gridBagConstraints);

        btnBroadcast.setText("Broadcast");
        btnBroadcast.setToolTipText("Briadcasts a message to all clients\ncurrently connected to the server.");
        btnBroadcast.setMaximumSize(new java.awt.Dimension(100, 20));
        btnBroadcast.setMinimumSize(new java.awt.Dimension(100, 20));
        btnBroadcast.setPreferredSize(new java.awt.Dimension(100, 20));
        btnBroadcast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBroadcastActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 8);
        getContentPane().add(btnBroadcast, gridBagConstraints);

        btnPing.setText("Ping");
        btnPing.setToolTipText("Sends a ping token and expects an answer.");
        btnPing.setMaximumSize(new java.awt.Dimension(100, 20));
        btnPing.setMinimumSize(new java.awt.Dimension(100, 20));
        btnPing.setPreferredSize(new java.awt.Dimension(100, 20));
        btnPing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        getContentPane().add(btnPing, gridBagConstraints);

        btnShutdown.setText("Shutdown");
        btnShutdown.setToolTipText("Shuts the server down from remote after a confirmation.");
        btnShutdown.setMaximumSize(new java.awt.Dimension(100, 20));
        btnShutdown.setMinimumSize(new java.awt.Dimension(100, 20));
        btnShutdown.setPreferredSize(new java.awt.Dimension(100, 20));
        btnShutdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShutdownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 8);
        getContentPane().add(btnShutdown, gridBagConstraints);

        btnLogin.setText("Login");
        btnLogin.setToolTipText("Authenticates against the WebSocket Server's user repository.");
        btnLogin.setMaximumSize(new java.awt.Dimension(100, 20));
        btnLogin.setMinimumSize(new java.awt.Dimension(100, 20));
        btnLogin.setPreferredSize(new java.awt.Dimension(100, 20));
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        getContentPane().add(btnLogin, gridBagConstraints);

        btnLogout.setText("Logout");
        btnLogout.setToolTipText("Logs out the current user out while the connection is kept.");
        btnLogout.setMaximumSize(new java.awt.Dimension(100, 20));
        btnLogout.setMinimumSize(new java.awt.Dimension(100, 20));
        btnLogout.setPreferredSize(new java.awt.Dimension(100, 20));
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 8);
        getContentPane().add(btnLogout, gridBagConstraints);

        txfTarget.setMinimumSize(new java.awt.Dimension(100, 20));
        txfTarget.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        getContentPane().add(txfTarget, gridBagConstraints);

        lblTarget.setText("Target");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 5, 0, 0);
        getContentPane().add(lblTarget, gridBagConstraints);

        lblMessage.setText("Message");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 10, 0, 0);
        getContentPane().add(lblMessage, gridBagConstraints);

        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/disconnected.png"))); // NOI18N
        lblStatus.setText("ID: -");
        lblStatus.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 8);
        getContentPane().add(lblStatus, gridBagConstraints);

        btnGetSessions.setToolTipText("Lists all currently active sessions on the server (admin feature).");
        btnGetSessions.setLabel("Get Sessions");
        btnGetSessions.setMaximumSize(new java.awt.Dimension(100, 20));
        btnGetSessions.setMinimumSize(new java.awt.Dimension(100, 20));
        btnGetSessions.setPreferredSize(new java.awt.Dimension(100, 20));
        btnGetSessions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetSessionsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 8);
        getContentPane().add(btnGetSessions, gridBagConstraints);

        btnGetUserRights.setText("Rights");
        btnGetUserRights.setToolTipText("Returns the rights of the currently authenticated user.");
        btnGetUserRights.setMaximumSize(new java.awt.Dimension(100, 20));
        btnGetUserRights.setMinimumSize(new java.awt.Dimension(100, 20));
        btnGetUserRights.setPreferredSize(new java.awt.Dimension(100, 20));
        btnGetUserRights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetUserRightsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 8);
        getContentPane().add(btnGetUserRights, gridBagConstraints);

        btnGetUserRoles.setText("Roles");
        btnGetUserRoles.setToolTipText("Returns the roles of the currently authenticated user.");
        btnGetUserRoles.setMaximumSize(new java.awt.Dimension(100, 20));
        btnGetUserRoles.setMinimumSize(new java.awt.Dimension(100, 20));
        btnGetUserRoles.setPreferredSize(new java.awt.Dimension(100, 20));
        btnGetUserRoles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetUserRolesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        getContentPane().add(btnGetUserRoles, gridBagConstraints);

        lblURL.setText("URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 48;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 5, 0, 0);
        getContentPane().add(lblURL, gridBagConstraints);

        txfURL.setText("ws://localhost:8787/jWebSocket/jWebSocket");
        txfURL.setToolTipText("Use wss:// for SSL together with port 9797.");
        txfURL.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 48;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 268;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        getContentPane().add(txfURL, gridBagConstraints);

        btnRPC.setText("RPC-Demo");
        btnRPC.setToolTipText("Calls the getMD5 method on the \nserver using the message as input.");
        btnRPC.setMaximumSize(new java.awt.Dimension(100, 20));
        btnRPC.setMinimumSize(new java.awt.Dimension(100, 20));
        btnRPC.setPreferredSize(new java.awt.Dimension(100, 20));
        btnRPC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRPCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        getContentPane().add(btnRPC, gridBagConstraints);

        lblUsername.setText("User");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        getContentPane().add(lblUsername, gridBagConstraints);

        lblPassword.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 0);
        getContentPane().add(lblPassword, gridBagConstraints);

        txfUser.setText("root");
        txfUser.setMinimumSize(new java.awt.Dimension(100, 20));
        txfUser.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 5, 0, 0);
        getContentPane().add(txfUser, gridBagConstraints);

        pwfPassword.setText("root");
        pwfPassword.setMinimumSize(new java.awt.Dimension(100, 20));
        pwfPassword.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 5, 0, 0);
        getContentPane().add(pwfPassword, gridBagConstraints);

        btnTimeout.setText("Timeout");
        btnTimeout.setToolTipText("<html>Sends a delay token (answer delay predefined to 2000ms).<br> \n1000ms will cause a timeout, 3000ms a success response.</html>");
        btnTimeout.setMaximumSize(new java.awt.Dimension(100, 20));
        btnTimeout.setMinimumSize(new java.awt.Dimension(100, 20));
        btnTimeout.setPreferredSize(new java.awt.Dimension(100, 20));
        btnTimeout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimeoutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 8);
        getContentPane().add(btnTimeout, gridBagConstraints);

        txfTimeout.setText("1000");
        txfTimeout.setToolTipText("Specifies the timeout to wait for a response.");
        txfTimeout.setMaximumSize(new java.awt.Dimension(100, 20));
        txfTimeout.setMinimumSize(new java.awt.Dimension(100, 20));
        txfTimeout.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
        getContentPane().add(txfTimeout, gridBagConstraints);

        lblReconnect.setText("Reconnect (ms)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        getContentPane().add(lblReconnect, gridBagConstraints);

        lblTimeout.setText("Timeout (ms)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
        getContentPane().add(lblTimeout, gridBagConstraints);

        txfReconnectDelay.setText("1500");
        txfReconnectDelay.setToolTipText("<html>Specifiies the automatic re-connect timeout<br>\nin case of a server restart (0 for none).<html>");
        txfReconnectDelay.setMaximumSize(new java.awt.Dimension(100, 20));
        txfReconnectDelay.setMinimumSize(new java.awt.Dimension(100, 20));
        txfReconnectDelay.setPreferredSize(new java.awt.Dimension(100, 20));
        txfReconnectDelay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txfReconnectDelayActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        getContentPane().add(txfReconnectDelay, gridBagConstraints);

        lblConnection.setText("Connection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 0);
        getContentPane().add(lblConnection, gridBagConstraints);

        lblAuth.setText("Authentication");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(lblAuth, gridBagConstraints);

        lblSend.setText("Sending data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(lblSend, gridBagConstraints);

        jLabel1.setText("Authorization");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setText("Administration");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(jLabel2, gridBagConstraints);

        jLabel3.setText("Features");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(jLabel3, gridBagConstraints);

        btnStressTest.setText("StressTest");
        btnStressTest.setMaximumSize(new java.awt.Dimension(100, 20));
        btnStressTest.setMinimumSize(new java.awt.Dimension(100, 20));
        btnStressTest.setPreferredSize(new java.awt.Dimension(100, 20));
        btnStressTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStressTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 8, 0, 0);
        getContentPane().add(btnStressTest, gridBagConstraints);

        btnDebugOut.setLabel("Debug Out");
        btnDebugOut.setMaximumSize(new java.awt.Dimension(100, 20));
        btnDebugOut.setMinimumSize(new java.awt.Dimension(100, 20));
        btnDebugOut.setPreferredSize(new java.awt.Dimension(100, 20));
        btnDebugOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDebugOutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 8);
        getContentPane().add(btnDebugOut, gridBagConstraints);

        btnGC.setText("Garb.Coll.");
        btnGC.setMaximumSize(new java.awt.Dimension(100, 20));
        btnGC.setMinimumSize(new java.awt.Dimension(100, 20));
        btnGC.setPreferredSize(new java.awt.Dimension(100, 20));
        btnGC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        getContentPane().add(btnGC, gridBagConstraints);

        lblFilename.setText("Filename");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 42;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
        getContentPane().add(lblFilename, gridBagConstraints);

        btnUpload.setText("Upload");
        btnUpload.setToolTipText("<html>Uploads the given file to the server. The file<br>will be located in the root folder of the public area.</html>");
        btnUpload.setMaximumSize(new java.awt.Dimension(100, 20));
        btnUpload.setMinimumSize(new java.awt.Dimension(100, 20));
        btnUpload.setPreferredSize(new java.awt.Dimension(100, 20));
        btnUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 25;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 8);
        getContentPane().add(btnUpload, gridBagConstraints);

        txfFilename.setToolTipText("<html>Type the name for the file to be uploaded here.</html>");
        txfFilename.setMaximumSize(new java.awt.Dimension(100, 20));
        txfFilename.setMinimumSize(new java.awt.Dimension(100, 20));
        txfFilename.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 42;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 8);
        getContentPane().add(txfFilename, gridBagConstraints);

        chkJMSClient.setText("Cluster(JMS)");
        chkJMSClient.setToolTipText("Select it if the server runs a JMS Cluster");
        chkJMSClient.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 48;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 0);
        getContentPane().add(chkJMSClient, gridBagConstraints);

        txfClusterName.setText("clusterName");
        txfClusterName.setToolTipText("Enter the cluster name to connecto to");
        txfClusterName.setMaximumSize(new java.awt.Dimension(100, 20));
        txfClusterName.setMinimumSize(new java.awt.Dimension(100, 20));
        txfClusterName.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 48;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        getContentPane().add(txfClusterName, gridBagConstraints);

        jcbWrap.setText("Word wrap");
        jcbWrap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbWrapActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 46;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 0);
        getContentPane().add(jcbWrap, gridBagConstraints);

        btnSaveLog.setText("Save Logs");
        btnSaveLog.setToolTipText("Clears the log window.");
        btnSaveLog.setMaximumSize(new java.awt.Dimension(100, 20));
        btnSaveLog.setMinimumSize(new java.awt.Dimension(100, 20));
        btnSaveLog.setPreferredSize(new java.awt.Dimension(100, 20));
        btnSaveLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveLogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        getContentPane().add(btnSaveLog, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 28;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = 61;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 60;
        gridBagConstraints.gridwidth = 29;
        getContentPane().add(filler2, gridBagConstraints);

        pmnFile.setText("File");

        mniExit.setText("Exit");
        mniExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExitActionPerformed(evt);
            }
        });
        pmnFile.add(mniExit);

        mniPreferences.setText("Preferences");
        mniPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniPreferencesActionPerformed(evt);
            }
        });
        pmnFile.add(mniPreferences);

        mnbMain.add(pmnFile);

        pmnTests.setText("Edit");

        mniConnect.setText("Connect");
        pmnTests.add(mniConnect);

        mniDisconnect.setText("Disconnect");
        pmnTests.add(mniDisconnect);

        mniSend.setText("Send");
        pmnTests.add(mniSend);

        mniBroadcast.setText("Broadcast");
        pmnTests.add(mniBroadcast);

        mnbMain.add(pmnTests);

        pmnLoadBalancer.setText("Load Balancer");

        mniView.setText("Demo");
        mniView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniViewActionPerformed(evt);
            }
        });
        pmnLoadBalancer.add(mniView);

        mnbMain.add(pmnLoadBalancer);

        jMenu1.setText("JMS");

        jmJMSServer.setText("JMS Server");

        jMenuItem1.setText("Run");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jmJMSServer.add(jMenuItem1);

        jMenuItem2.setText("Stop");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jmJMSServer.add(jMenuItem2);

        jMenu1.add(jmJMSServer);

        jmiViewJMSCLient.setText("JMS Client");
        jmiViewJMSCLient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiViewJMSCLientActionPerformed(evt);
            }
        });
        jMenu1.add(jmiViewJMSCLient);

        mnbMain.add(jMenu1);

        setJMenuBar(mnbMain);

        getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 *
	 */
	public void checkDisconnect() {
		// closes the connection, clears garbage and 
		// terminates potential re-connection tasks.
		if (null != mClient && mClient.isConnected()) {
			mClient.close();
		}
	}

	private void doCloseForm() {
		checkDisconnect();
		System.exit(0);
	}

	private void mniExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExitActionPerformed
		doCloseForm();
	}//GEN-LAST:event_mniExitActionPerformed

	private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		checkDisconnect();
	}//GEN-LAST:event_formWindowClosing

	private void mniPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniPreferencesActionPerformed
		// open preferences dialog
	}//GEN-LAST:event_mniPreferencesActionPerformed

	private void btnGetUserRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetUserRolesActionPerformed
		try {
			mClient.getUserRoles(txfTarget.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
	}//GEN-LAST:event_btnGetUserRolesActionPerformed

	private void btnRPCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRPCActionPerformed
		// create a new token, use namespace of RPC plugin and "rpc" for type
		Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.rpc", "rpc");
		// pass the path of the class
		lToken.setString("classname", "org.jwebsocket.rpc.sample.SampleRPCLibrary");

		List lArgs = new ArrayList();

                // getMD5
		// pass the method to be called
		/*
		 * lToken.setString("method", "getMD5");
		 * lArgs.add(txfMessage.getText());
		 */
                // runOverloadDemo
		/*
		 * lToken.setString("method", "runOverloadDemo"); // create the
		 * list of arguments to be applied to the method List lListArg =
		 * new ArrayList(); lListArg.add(1);	lListArg.add(2);
		 * lListArg.add(3);	lListArg.add(4); lListArg.add("a");
		 * lListArg.add("b");	lListArg.add("c");	lListArg.add("d");
		 * lArgs.add(lListArg);
		 */
		// demo for getRPCObject
		lToken.setString("method", "getRPCObject");
		// instantiate a new tokenizable object to be used for a RPC
		SampleRPCObject lRPCObj = new SampleRPCObject(
				"Alexander",
				"Schulze",
				"An Vieslapp 29",
				"52134",
				"Herzogenrath");
		// add this object to the list of arguments
		lArgs.add(lRPCObj.toToken());

		// pass the list of arguments to the method (automatic methods matching)
		lToken.setList("args", lArgs);
		try {
			mClient.sendToken(lToken);
		} catch (WebSocketException ex) {
			// process potential exception
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());

		}
	}//GEN-LAST:event_btnRPCActionPerformed

	/**
	 * @return the jcbWrap
	 */
	public javax.swing.JCheckBox getJcbWrap() {
		return jcbWrap;
	}

	private class MyResponseListener extends BaseTokenResponseListener {

		public MyResponseListener(long aTimeout) {
			super(aTimeout);
		}

		@Override
		public void OnTimeout(Token aToken) {
			// process potential exception
			mLog("Timeout: " + aToken.toString());
		}

		@Override
		public void OnSuccess(Token aToken) {
			// process potential exception
			mLog("Success: " + aToken.toString());
		}

		@Override
		public void OnFailure(Token aToken) {
			// process potential exception
			mLog("Failure: " + aToken.toString());
		}

		@Override
		public void OnResponse(Token aToken) {
			// process potential exception
			mLog("Response: " + aToken.toString());
		}
	}
	private void txfReconnectDelayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txfReconnectDelayActionPerformed
		try {
			long lTimeout = Long.parseLong(txfReconnectDelay.getText());
			mReliabilityOptions.setReconnectDelay(lTimeout);
		} catch (Exception ex) {
			// process potential exception
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());

		}
	}//GEN-LAST:event_txfReconnectDelayActionPerformed

	class MyLogListener implements StressTests.LogListener {

		@Override
		public void log(String aMsg) {
			mLog(aMsg);
		}
	}
	private void btnStressTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStressTestActionPerformed
		mStressTestsThread = new Thread(new Runnable() {

			@Override
			public void run() {
				StressTests lTests = new StressTests(new MyLogListener());
				lTests.runStressTest(txfURL.getText(), chkJMSClient.isSelected(), txfClusterName.getText());
			}
		}, "jWebSocketStressTests");
		mStressTestsThread.start();
	}//GEN-LAST:event_btnStressTestActionPerformed

	private void btnGCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGCActionPerformed
		try {
			Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.admin", "gc");
			mClient.sendToken(lToken);
		} catch (Exception ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}

	}//GEN-LAST:event_btnGCActionPerformed

    private void bntSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntSendActionPerformed
		try {
			mClient.sendText(txfTarget.getText(), txfMessage.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}

    }//GEN-LAST:event_bntSendActionPerformed

	private void mniViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniViewActionPerformed
		if (mClient != null) {
			LoadBalancerDialog lLb = new LoadBalancerDialog(mClient, txaLog);
			lLb.setLocation(this.getX() + 300,
					this.getLocation().y + 200);
			lLb.setVisible(true);
		} else {
			mLog("INFO - Press key 'Connect' to open connection with the jWebSocket Server!.");
		}
	}//GEN-LAST:event_mniViewActionPerformed

    private void jmiViewJMSCLientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiViewJMSCLientActionPerformed
		mJMSClient = new JMSClientDialog(txaLog, this);
		mJMSClient.setLocation(this.getX() + 400,
				this.getLocation().y + 200);

		mJMSClient.setVisible(true);
    }//GEN-LAST:event_jmiViewJMSCLientActionPerformed

    private void jcbWrapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbWrapActionPerformed
		txaLog.setLineWrap(getJcbWrap().isSelected());
    }//GEN-LAST:event_jcbWrapActionPerformed

	/**
	 *
	 * @param aDefaultFilename
	 */
	public void saveLogs(String aDefaultFilename) {
		if (aDefaultFilename == null || aDefaultFilename.equals("")) {
			aDefaultFilename = "jWebSocketTests_"
					+ new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + ".log";
		}
		JFileChooser lChooser = new JFileChooser();
		FileNameExtensionFilter lFilter = new FileNameExtensionFilter(
				"*.log files", "log");
		lChooser.setFileFilter(lFilter);
		lChooser.setSelectedFile(new File(aDefaultFilename));
		int lReturnVal = lChooser.showSaveDialog(this);
		if (lReturnVal == JFileChooser.APPROVE_OPTION) {
			FileWriter lWriter = null;
			try {
				File lFile = lChooser.getSelectedFile();
				lWriter = new FileWriter(lFile);
				lWriter.write(txaLog.getText());
			} catch (IOException aException) {
				mLog(aException.getMessage());
			} finally {
				try {
					if (lWriter != null) {
						lWriter.close();
					}
				} catch (IOException aIoException) {
					mLog(aIoException.getMessage());
				}
			}
		}
	}
    private void btnSaveLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveLogActionPerformed
		if (mJMSClient != null) {
			saveLogs("jWebSocketJMSClientTests_"
					+ new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + ".log");
		} else {
			saveLogs(null);
		}
    }//GEN-LAST:event_btnSaveLogActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		runJMSServerDemo();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
		stopJMSServerDemo();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadActionPerformed
		try {
			// saves a file in the public area of the jWebSocket server
			Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.filesystem", "save");
			lToken.setString("scope", "public");
			lToken.setBoolean("notify", false);

			File lFile = new File(txfFilename.getText());
			byte[] lBA = FileUtils.readFileToByteArray(lFile);
			String lBase64Enc = Base64.encodeBase64String(lBA);

			// isoloate name from filename to be uploaded, it's just for demo purposes!
			lToken.setString("filename", FilenameUtils.getName(txfFilename.getText()));
			lToken.setString("data", lBase64Enc);
			lToken.setString("encoding", "base64");
			mClient.sendToken(lToken);
		} catch (Exception ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnUploadActionPerformed

    private void btnDebugOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDebugOutActionPerformed
		try {
			Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "getLostConnectors");
			mClient.sendToken(lToken);
		} catch (Exception ex) {
			// process potential exception
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnDebugOutActionPerformed

    private void btnTimeoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimeoutActionPerformed
		Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.test", "delay");
		lToken.setInteger("delay", 2000);
		long lTimeout;
		try {
			lTimeout = Integer.parseInt(txfTimeout.getText());
		} catch (Exception ex) {
			lTimeout = 3000;
		}
		WebSocketResponseTokenListener lResponseListener = new MyResponseListener(lTimeout);
		try {
			mClient.sendToken(lToken, lResponseListener);
		} catch (WebSocketException ex) {
			// process potential exception
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnTimeoutActionPerformed

    private void btnGetUserRightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetUserRightsActionPerformed
		try {
			mClient.getUserRights(txfTarget.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnGetUserRightsActionPerformed

    private void btnGetSessionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetSessionsActionPerformed
		try {
			mClient.getConnections();
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnGetSessionsActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
		try {
			mClient.logout();
			lblStatus.setIcon(mIcoConnected);
			mPrevStatus = WebSocketStatus.OPEN;
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShutdownActionPerformed
		try {
			mClient.shutdown();
			// Thread.sleep(500);
			// mClient.close();
		} catch (Exception ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnShutdownActionPerformed

    private void btnBroadcastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBroadcastActionPerformed
		try {
			mClient.broadcastText(txfMessage.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
    }//GEN-LAST:event_btnBroadcastActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
		try {
			mClient.close();
		} catch (Exception lEx) {
			mLog("Error captured while closing the connection: " + lEx.getLocalizedMessage() + Arrays.toString(lEx.getStackTrace()));
		}
    }//GEN-LAST:event_btnDisconnectActionPerformed

	private void loadPropertyFile() {
		mJMSServerProperties = new Properties();
		String lHomeEnv = System.getenv("JWEBSOCKET_HOME");
		if (lHomeEnv != null) {
			try {
				lHomeEnv = FilenameUtils.separatorsToUnix(lHomeEnv);
				mLog("Loading JMSServer properties file");
				// loading a properties file with the default data for the JMSSendPayloadDialog
				mJMSServerProperties.load(new FileInputStream(lHomeEnv + "conf/jWebSocketSwingGUI/JMSDemoServer.properties"));
				mLog("JMSServer properties file loaded correctly with the following configurations: " + mJMSServerProperties.toString());
			} catch (IOException lException) {
				mLog(lException.getMessage());
			}
		}
	}

	/*
	 * This method will automatically run a demo server and provide as
	 * endpoint a few services in your configured brokerURL
	 */
	private void runJMSServerDemo() {

		if (mJMSServerIsRunning) {
			stopJMSServerDemo();
		}
		mJMSServerIsRunning = true;
		try {
			mJMSServerThread = new Thread(new Runnable() {

				@Override
				public void run() {
					String lTopic = "org.jwebsocket.jms.gateway",
							lBrokerURL = "tcp://127.0.0.1:61616?connectionTimeout=3000",
							lGatewayId = "org.jwebsocket.jms.gateway",
							lEndpointId = "jWebSocketJMSDemoServer";
					if (mJMSServerProperties == null) {
						loadPropertyFile();
					}
					if (mJMSServerProperties != null && !mJMSServerProperties.isEmpty()) {
						if (mJMSServerProperties.getProperty("JMSServerGatewayTopic") != null) {
							lTopic = mJMSServerProperties.getProperty("JMSServerGatewayTopic");
						}
						if (mJMSServerProperties.getProperty("JMSServerBrokerURL") != null) {
							lBrokerURL = mJMSServerProperties.getProperty("JMSServerBrokerURL");
						}
						if (mJMSServerProperties.getProperty("JMSServerGatewayId") != null) {
							lGatewayId = mJMSServerProperties.getProperty("JMSServerGatewayId");
						}
						if (mJMSServerProperties.getProperty("JMSServerEndpointId") != null) {
							lEndpointId = mJMSServerProperties.getProperty("JMSServerEndpointId");
						}
					}
					String[] lArgs = new String[4];
					lArgs[0] = lBrokerURL;
					lArgs[1] = lTopic;
					lArgs[2] = lGatewayId;
					lArgs[3] = lEndpointId;
					JMSServer lJMSServer = new JMSServer();
					JMSEndPoint lEndpoint = lJMSServer.start(lArgs);
					// add a primitive listener to listen in coming messages
					// this one is deprecated, only left for reference purposes!
					// lJMSClient.addListener(new JMSServerMessageListener(lJMSClient));
					// this is a console app demo
					// so wait in a thread loop until the client get shut down
					try {
						while (mJMSServerIsRunning) {
							Thread.sleep(1000);
						}
					} catch (InterruptedException lEx) {
						// ignore a potential exception here
					}

					// check if JMS client has already been shutdown by logic
					if (!lEndpoint.isShutdown()) {
						// if not yet done...
						System.out.println("Shutting down JMS Server Endpoint...");
						// shut the client properly down
						lEndpoint.shutdown();
					}
					// and show final status message in the console

					System.out.println("JMS Server Endpoint properly shutdown.");
				}
			}, "jWebSocketJMSDemoServer");
			mJMSServerThread.start();
		} catch (Exception aException) {
			mLog(aException.getMessage());
		}
	}

	private void stopJMSServerDemo() {
		mJMSServerIsRunning = false;
		try {
			mJMSServerThread.join(2000);
			mJMSServerThread.interrupt();
		} catch (InterruptedException aException) {
			mLog(aException.getMessage());
		}
	}

	private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnConnectActionPerformed
		try {
			String lURL = txfURL.getText();
			if (!chkJMSClient.isSelected()) {
				if (lURL.startsWith("http")) {
					mClient = new JWebSocketTokenClient(new JWebSocketHTTPClient());
					mClient.setReliabilityOptions(mReliabilityOptions);
					mClient.addListener(this);
					mClient.open(lURL);
				} else {
					mClient = new JWebSocketTokenClient(mReliabilityOptions);
					mClient.addListener(this);
					mClient.open(13, lURL);
				}
			} else {
				mClient = new JWebSocketTokenClient(new JWebSocketJMSClient(txfClusterName.getText()));
				mClient.setReliabilityOptions(mReliabilityOptions);
				mClient.addListener(this);
				mClient.open(lURL);
			}

		} catch (WebSocketException ex) {
			mLog(ex.getMessage());
		}
	}

	private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLoginActionPerformed
		try {
			mClient.login(txfUser.getText(), new String(pwfPassword.getPassword()));
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
	}// GEN-LAST:event_btnLoginActionPerformed

	private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnClearLogActionPerformed
		txaLog.setText("");
		initializeLogs();
	}

	private void btnPingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPingActionPerformed
		try {
			mClient.ping(true);
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
	}// GEN-LAST:event_btnPingActionPerformed

	private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSendActionPerformed
		try {
			mClient.sendText(txfTarget.getText(), txfMessage.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new TestDialog().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntSend;
    private javax.swing.JButton btnBroadcast;
    private javax.swing.JButton btnClearLog;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDebugOut;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnGC;
    private javax.swing.JButton btnGetSessions;
    private javax.swing.JButton btnGetUserRights;
    private javax.swing.JButton btnGetUserRoles;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnPing;
    private javax.swing.JButton btnRPC;
    private javax.swing.JButton btnSaveLog;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnShutdown;
    private javax.swing.JButton btnStressTest;
    private javax.swing.JButton btnTimeout;
    private javax.swing.JButton btnUpload;
    private javax.swing.JCheckBox chkJMSClient;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JCheckBox jcbWrap;
    private javax.swing.JMenu jmJMSServer;
    private javax.swing.JMenuItem jmiViewJMSCLient;
    private javax.swing.JLabel lblAuth;
    private javax.swing.JLabel lblConnection;
    private javax.swing.JLabel lblFilename;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblReconnect;
    private javax.swing.JLabel lblSend;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTarget;
    private javax.swing.JLabel lblTimeout;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblURL;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JMenuBar mnbMain;
    private javax.swing.JMenuItem mniBroadcast;
    private javax.swing.JMenuItem mniConnect;
    private javax.swing.JMenuItem mniDisconnect;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniPreferences;
    private javax.swing.JMenuItem mniSend;
    private javax.swing.JMenuItem mniView;
    private javax.swing.JMenu pmnFile;
    private javax.swing.JMenu pmnLoadBalancer;
    private javax.swing.JMenu pmnTests;
    private javax.swing.JPasswordField pwfPassword;
    private javax.swing.JScrollPane scpTextArea;
    private javax.swing.JTextArea txaLog;
    private javax.swing.JTextField txfClusterName;
    private javax.swing.JTextField txfFilename;
    private javax.swing.JTextField txfMessage;
    private javax.swing.JTextField txfReconnectDelay;
    private javax.swing.JTextField txfTarget;
    private javax.swing.JTextField txfTimeout;
    private javax.swing.JTextField txfURL;
    private javax.swing.JTextField txfUser;
    // End of variables declaration//GEN-END:variables
}
