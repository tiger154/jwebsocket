//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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

/*
 * TestDialog.java
 *
 * Created on Mar 15, 2010, 2:55:47 PM
 */
package org.jwebsocket.ui;

import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.client.java.ReliabilityOptions;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.plugins.rpc.SampleRPCObject;
import org.jwebsocket.tests.StressTests;
import org.jwebsocket.token.BaseTokenResponseListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 * Java Swing client for jWebSocket
 * @author aschulze
 * @version $Id:$
 */
public class TestDialog extends javax.swing.JFrame implements WebSocketClientTokenListener {

	private static final long serialVersionUID = 1L;
	private BaseTokenClient mClient = null;
	private WebSocketStatus mPrevStatus = WebSocketStatus.CLOSED;
	private ImageIcon mIcoDisconnected = null;
	private ImageIcon mIcoConnected = null;
	private ImageIcon mIcoAuthenticated = null;
	private ReliabilityOptions mReliabilityOptions = null;

	/** Creates new form TestDialog */
	public TestDialog() {
		initComponents();
		try {
			lblTitle.setText(lblTitle.getText().replace("{ver}", JWebSocketClientConstants.VERSION_STR));
			mReliabilityOptions = new ReliabilityOptions(true, 1500, 3000, 1, -1);
			mClient = new BaseTokenClient(mReliabilityOptions);
			mClient.addListener(this);
			mIcoDisconnected = new ImageIcon(getClass().getResource("/images/disconnected.png"));
			mIcoConnected = new ImageIcon(getClass().getResource("/images/connected.png"));
			mIcoAuthenticated = new ImageIcon(getClass().getResource("/images/authenticated.png"));
			checkStatusIcon();
		} catch (Exception ex) {
			System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	private void checkStatusIcon() {
		WebSocketStatus lStatus = mClient.getStatus();
		String lClientId = mClient.getClientId();
		lblStatus.setText("Client-Id: " + (lClientId != null ? lClientId : "-"));
		if (lStatus != mPrevStatus) {
			mPrevStatus = lStatus;
			if (lStatus == WebSocketStatus.AUTHENTICATED) {
				lblStatus.setIcon(mIcoAuthenticated);
			} else if (lStatus == WebSocketStatus.OPEN) {
				lblStatus.setIcon(mIcoConnected);
			} else {
				lblStatus.setIcon(mIcoDisconnected);
			}
		}
	}

	private void mLog(String aMessage) {
		synchronized (txaLog) {
			int lMAX = 1000;
			int lLineCount = txaLog.getLineCount();
			if (lLineCount > lMAX) {
				String lText = txaLog.getText();
				int lIdx = 0;
				int lCnt = lLineCount;
				while (lIdx < lText.length() && lCnt > lMAX) {
					if (lText.charAt(lIdx) == '\n') {
						lCnt--;
					}
					lIdx++;
				}
				txaLog.replaceRange("", 0, lIdx);
			}
			if (null != aMessage) {
				txaLog.append(aMessage);
			} else {
				txaLog.setText("");
			}
			txaLog.setCaretPosition(txaLog.getText().length());
		}
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
		mLog("Opening...\n");
		checkStatusIcon();
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		mLog("Opened.\n");
		checkStatusIcon();
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		// ignore that here
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		mLog("Received Token: " + aToken.toString() + "\n");
		checkStatusIcon();
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		mLog("Closed (" + aEvent.getData() + ").\n");
		checkStatusIcon();
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
		mLog("Reconnecting...\n");
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
        mnbMain = new javax.swing.JMenuBar();
        pmnFile = new javax.swing.JMenu();
        mniExit = new javax.swing.JMenuItem();
        mniPreferences = new javax.swing.JMenuItem();
        pmnTests = new javax.swing.JMenu();
        mniConnect = new javax.swing.JMenuItem();
        mniDisconnect = new javax.swing.JMenuItem();
        mniSend = new javax.swing.JMenuItem();
        mniBroadcast = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jWebSocket Fundamental Demo");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Synapso16x16.png")));
        setMinimumSize(new java.awt.Dimension(640, 480));
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
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(lblTitle, gridBagConstraints);

        bntSend.setText("Send");
        bntSend.setToolTipText("Sends a message to the clients identified by the target field.");
        bntSend.setMaximumSize(new java.awt.Dimension(100, 20));
        bntSend.setMinimumSize(new java.awt.Dimension(100, 20));
        bntSend.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(bntSend, gridBagConstraints);

        txaLog.setColumns(20);
        txaLog.setEditable(false);
        txaLog.setRows(5);
        scpTextArea.setViewportView(txaLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(btnClearLog, gridBagConstraints);

        txfMessage.setText("Message");
        txfMessage.setMinimumSize(new java.awt.Dimension(100, 20));
        txfMessage.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnLogout, gridBagConstraints);

        txfTarget.setMinimumSize(new java.awt.Dimension(100, 20));
        txfTarget.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txfTarget, gridBagConstraints);

        lblTarget.setText("Target");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblTarget, gridBagConstraints);

        lblMessage.setText("Message");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblMessage, gridBagConstraints);

        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/disconnected.png"))); // NOI18N
        lblStatus.setText("ID: -");
        lblStatus.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnGetUserRoles, gridBagConstraints);

        lblURL.setText("URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblURL, gridBagConstraints);

        txfURL.setText("ws://localhost:8787/jWebSocket/jWebSocket");
        txfURL.setToolTipText("Use wss:// for SSL together with port 9797.");
        txfURL.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnRPC, gridBagConstraints);

        lblUsername.setText("User");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblUsername, gridBagConstraints);

        lblPassword.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblPassword, gridBagConstraints);

        txfUser.setText("root");
        txfUser.setMinimumSize(new java.awt.Dimension(100, 20));
        txfUser.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txfUser, gridBagConstraints);

        pwfPassword.setText("root");
        pwfPassword.setMinimumSize(new java.awt.Dimension(100, 20));
        pwfPassword.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(btnTimeout, gridBagConstraints);

        txfTimeout.setText("1000");
        txfTimeout.setToolTipText("Specifies the timeout to wait for a response.");
        txfTimeout.setMaximumSize(new java.awt.Dimension(100, 20));
        txfTimeout.setMinimumSize(new java.awt.Dimension(100, 20));
        txfTimeout.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        getContentPane().add(txfTimeout, gridBagConstraints);

        lblReconnect.setText("Reconnect (ms)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(lblReconnect, gridBagConstraints);

        lblTimeout.setText("Timeout (ms)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        getContentPane().add(txfReconnectDelay, gridBagConstraints);

        lblConnection.setText("Connection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(lblConnection, gridBagConstraints);

        lblAuth.setText("Authentication");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(lblAuth, gridBagConstraints);

        lblSend.setText("Sending data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(lblSend, gridBagConstraints);

        jLabel1.setText("Authorization");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setText("Administration");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(jLabel2, gridBagConstraints);

        jLabel3.setText("Features");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 13;
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 13;
        getContentPane().add(btnGC, gridBagConstraints);

        lblFilename.setText("Filename");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        getContentPane().add(btnUpload, gridBagConstraints);

        txfFilename.setToolTipText("<html>Type the name for the file to be uploaded here.</html>");
        txfFilename.setMaximumSize(new java.awt.Dimension(100, 20));
        txfFilename.setMinimumSize(new java.awt.Dimension(100, 20));
        txfFilename.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 5);
        getContentPane().add(txfFilename, gridBagConstraints);

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

        setJMenuBar(mnbMain);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public void checkDisconnect() {
		// closes the connection, clears garbage and 
		// terminates potential re-connection tasks.
		mClient.close();
	}

	private void doCloseForm() {
		checkDisconnect();
		dispose();
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

	private void btnGetUserRightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetUserRightsActionPerformed
		try {
			mClient.getUserRights(txfTarget.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnGetUserRightsActionPerformed

	private void btnGetUserRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetUserRolesActionPerformed
		try {
			mClient.getUserRoles(txfTarget.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
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
		lToken.setString("method", "getMD5");
		lArgs.add(txfMessage.getText());
		 */

		// runOverloadDemo
		/*
		lToken.setString("method", "runOverloadDemo");
		// create the list of arguments to be applied to the method
		List lListArg = new ArrayList();
		lListArg.add(1);	lListArg.add(2);	lListArg.add(3);	lListArg.add(4);
		lListArg.add("a");	lListArg.add("b");	lListArg.add("c");	lListArg.add("d");
		lArgs.add(lListArg);
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
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnRPCActionPerformed

	private class MyResponseListener extends BaseTokenResponseListener {

		public MyResponseListener(long aTimeout) {
			super(aTimeout);
		}

		@Override
		public void OnTimeout(Token aToken) {
			// process potential exception
			mLog("Timeout: " + aToken.toString() + "\n");
		}

		@Override
		public void OnSuccess(Token aToken) {
			// process potential exception
			mLog("Success: " + aToken.toString() + "\n");
		}

		@Override
		public void OnFailure(Token aToken) {
			// process potential exception
			mLog("Failure: " + aToken.toString() + "\n");
		}

		@Override
		public void OnResponse(Token aToken) {
			// process potential exception
			mLog("Response: " + aToken.toString() + "\n");
		}
	}

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
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnTimeoutActionPerformed

	private void txfReconnectDelayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txfReconnectDelayActionPerformed
		try {
			long lTimeout = Long.parseLong(txfReconnectDelay.getText());
			mReliabilityOptions.setReconnectDelay(lTimeout);
		} catch (Exception ex) {
			// process potential exception
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_txfReconnectDelayActionPerformed

	class MyLogListener implements StressTests.LogListener {

		@Override
		public void log(String aMsg) {
			mLog(aMsg);
		}
	}

	private void btnStressTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStressTestActionPerformed
		for (int i = 0; i < 10; i++) {
			new Thread() {

				@Override
				public void run() {
					StressTests lTests = new StressTests(new MyLogListener());
					lTests.runStressTest(txfURL.getText());
				}
			}.start();
		}
	}//GEN-LAST:event_btnStressTestActionPerformed

	private void btnDebugOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDebugOutActionPerformed
		try {
			Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "getLostConnectors");
			mClient.sendToken(lToken);
		} catch (Exception ex) {
			// process potential exception
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnDebugOutActionPerformed

	private void btnGCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGCActionPerformed
		try {
			Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.admin", "gc");
			mClient.sendToken(lToken);
		} catch (Exception ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}

	}//GEN-LAST:event_btnGCActionPerformed

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
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnUploadActionPerformed

	private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnConnectActionPerformed
		mClient.open(13, txfURL.getText());
	}// GEN-LAST:event_btnConnectActionPerformed

	private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnDisconnectActionPerformed
		mClient.close();
	}// GEN-LAST:event_btnDisconnectActionPerformed

	private void btnShutdownActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnShutdownActionPerformed
		try {
			mClient.shutdown();
			// Thread.sleep(500);
			// mClient.close();
		} catch (Exception ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnShutdownActionPerformed

	private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLoginActionPerformed
		try {
			mClient.login(txfUser.getText(), new String(pwfPassword.getPassword()));
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnLoginActionPerformed

	private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLogoutActionPerformed
		try {
			mClient.logout();
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnLogoutActionPerformed

	private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnClearLogActionPerformed
		mLog(null); // null means clear log
	}// GEN-LAST:event_btnClearLogActionPerformed

	private void btnBroadcastActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnBroadcastActionPerformed
		try {
			mClient.broadcastText(txfMessage.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnBroadcastActionPerformed

	private void btnPingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPingActionPerformed
		try {
			mClient.ping(true);
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnPingActionPerformed

	private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSendActionPerformed
		try {
			mClient.sendText(txfTarget.getText(), txfMessage.getText());
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnSendActionPerformed

	private void btnGetSessionsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnGetSessionsActionPerformed
		try {
			mClient.getConnections();
		} catch (WebSocketException ex) {
			mLog(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnGetSessionsActionPerformed

	/**
	 * @param args
	 *            the command line arguments
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
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnShutdown;
    private javax.swing.JButton btnStressTest;
    private javax.swing.JButton btnTimeout;
    private javax.swing.JButton btnUpload;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
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
    private javax.swing.JMenu pmnFile;
    private javax.swing.JMenu pmnTests;
    private javax.swing.JPasswordField pwfPassword;
    private javax.swing.JScrollPane scpTextArea;
    private javax.swing.JTextArea txaLog;
    private javax.swing.JTextField txfFilename;
    private javax.swing.JTextField txfMessage;
    private javax.swing.JTextField txfReconnectDelay;
    private javax.swing.JTextField txfTarget;
    private javax.swing.JTextField txfTimeout;
    private javax.swing.JTextField txfURL;
    private javax.swing.JTextField txfUser;
    // End of variables declaration//GEN-END:variables
}
