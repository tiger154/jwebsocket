/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.JMSClient;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.jms.JMSException;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.apache.commons.io.FilenameUtils;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.JWSEndPoint;
import org.jwebsocket.jms.endpoint.JWSEndPointMessageListener;
import org.jwebsocket.jms.endpoint.JWSEndPointSender;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.ui.TestDialog;

/**
 *
 * @author Victor Antonio Barzana Crespo
 */
public class JMSClientDialog extends javax.swing.JFrame {

	public JTextArea mLog;
	private String mBrokerURL;
	private String mGatewayTopic;
	private String mGatewayId;
	private String mEndPointId;
	private JMSEndPoint mJMSEndPoint;
	private JWSEndPointSender mSender;
	private JWSEndPointMessageListener mListener;
	private Thread mEndpointThreadRunner;
	private boolean mIsThreadRunning = false;
	private final String TT_LOGIN = "login";
	private final String TT_USERNAME = "username";
	private final String TT_PASSWORD = "password";
	private String TT_DEFAULT_USERNAME = "root";
	private String TT_DEFAULT_PASSWORD = "root";
	String mUsername;
	String mPassword;
	private final String TT_WELCOME = "welcome";
	private final String TT_PING = "ping";
	private final String TT_IDENTIFY = "identify";
	private final String NS_SYSTEM = "org.jwebsocket.plugins.system";
	private Properties mProperties;
	public TestDialog mMainDialog;

	/**
	 * Creates new form JMSClientDialog
	 */
	public JMSClientDialog() {
		initComponents();
	}

	/**
	 *
	 * @param aField
	 */
	public JMSClientDialog(JTextArea aField, TestDialog aParentDialog) {
		initComponents();
		mLog = aField;
		loadPropertyFile();
		mMainDialog = aParentDialog;
		// tcp://172.20.116.68:61616 org.jwebsocket.jws2jms org.jwebsocket.jms2jws aschulze-dt
		// failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false org.jwebsocket.jws2jms org.jwebsocket.jms2jws aschulze-dt
		log("jWebSocket JMS Gateway Demo Client initialized");
	}

	private void loadPropertyFile() {
		mProperties = new Properties();
		String lHomeEnv = System.getenv("JWEBSOCKET_HOME");

		if (lHomeEnv != null) {
			try {
				lHomeEnv = FilenameUtils.separatorsToUnix(lHomeEnv);
				log("Loading properties file");
				// loading a properties file with the default data for the JMSSendPayloadDialog
				mProperties.load(new FileInputStream(lHomeEnv + "conf/jWebSocketSwingGUI/JMSClientDialog.properties"));
				if (!mProperties.isEmpty()) {
					String lTopic = mProperties.getProperty("topic");
					String lBrokerURL = mProperties.getProperty("brokerURL");
					String lGatewayId = mProperties.getProperty("gatewayID");
					String lEndpointId = mProperties.getProperty("endpointID");
					String lUsername = mProperties.getProperty("username");
					String lPassword = mProperties.getProperty("password");
					if (lTopic != null) {
						jtfTopic.setText(lTopic.trim());
					}
					if (lBrokerURL != null) {
						jtfBrokerURL.setText(lBrokerURL.trim());
					}
					if (lGatewayId != null) {
						jtfGatewayID.setText(lGatewayId.trim());
					}
					if (lEndpointId != null) {
						jtfEndpointID.setText(lEndpointId.trim());
					}
					if (lUsername != null) {
						jtfUsername.setText(lUsername.trim());
					}
					if (lPassword != null) {
						jPFPassword.setText(lPassword);
					}
				}

			} catch (IOException lException) {
				log(lException.getMessage());
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public Properties getProperties() {
		return this.mProperties;
	}

	private void initializeEndpoint() {
		mBrokerURL = jtfBrokerURL.getText();
		mEndPointId = jtfEndpointID.getText();
		mGatewayId = jtfGatewayID.getText();
		mGatewayTopic = jtfTopic.getText();
		mUsername = jtfUsername.getText();
		mPassword = new String(jPFPassword.getPassword());

		if (mUsername == null || mUsername.trim().equals("")) {
			mUsername = TT_DEFAULT_USERNAME;
		}
		if (mPassword == null || mPassword.trim().equals("")) {
			mPassword = TT_DEFAULT_PASSWORD;
		}

		log("Using: "
				+ mBrokerURL + ", "
				+ mGatewayTopic + ", "
				+ mGatewayId + ", "
				+ mEndPointId);
		// instantiate a new jWebSocket JMS Gateway Client
		try {
			mJMSEndPoint = JWSEndPoint.getInstance(
					mBrokerURL,
					mGatewayTopic, // gateway topic
					mGatewayId, // gateway endpoint id
					mEndPointId, // unique node id
					5, // thread pool size, messages being processed concurrently
					JMSEndPoint.TEMPORARY // durable (for servers) or temporary (for clients)
			);
		} catch (JMSException lEx) {
			log("ERROR: JMSEndpoint could not be instantiated: " + lEx.getMessage());
			return;
		}
		// instantiate a high level JWSEndPointMessageListener
		mListener = new JWSEndPointMessageListener(mJMSEndPoint);
		// instantiate a high level JWSEndPointSender
		mSender = new JWSEndPointSender(mJMSEndPoint);

		mListener.addMessageListener(new JWSMessageListener(mSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				log("Received JSON message from '" + aSourceId
						+ "': " + aToken.toString());
			}
		});

		// on welcome message from jWebSocket, authenticate against jWebSocket
		mListener.addRequestListener(mGatewayId, TT_WELCOME, new JWSMessageListener(mSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				log("Received 'welcome' from '" + aSourceId + ".");
				if (mGatewayTopic.equals(aSourceId)) {
					enableButtons();
					// create a login token...
					log("Authenticating against jWebSocket...");
					Token lToken = TokenFactory.createToken(NS_SYSTEM, TT_LOGIN);
					lToken.setString(TT_USERNAME, mUsername);
					lToken.setString(TT_PASSWORD, mPassword);
					// and send it to the gateway (which is was the source of the message)
					sendToken(aSourceId, lToken);
				}
			}
		});

		mListener.addResponseListener(mGatewayId, TT_PING, new JWSMessageListener(mSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				log("Received 'pong' from '" + aSourceId + ".");
//				if (mGatewayTopic.equals(aSourceId)) {
				log(aToken.toString());
//				}
			}
		});

		mListener.addResponseListener(mGatewayId, TT_IDENTIFY, new JWSMessageListener(mSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				log("Received 'identify' from '" + aSourceId + ".");
//				if (mGatewayTopic.equals(aSourceId)) {
				log(aToken.toString());
//				}
			}
		});

		// process response of the JMS Gateway login...
		mListener.addResponseListener(NS_SYSTEM, TT_LOGIN,
				new JWSMessageListener(mSender) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						int lCode = aToken.getInteger("code", -1);
						if (0 == lCode) {
							log("Authentication against jWebSocket JMS Gateway successful.");
						} else {
							log("Authentication against jWebSocket JMS Gateway failed!");
						}
					}
				});

		// add a high level listener to listen in coming messages
		mJMSEndPoint.addListener(mListener);
		mEndpointThreadRunner = new Thread(new Runnable() {
			@Override
			public void run() {
				// add a listener to listen in coming messages
				// lJMSClient.addListener(new JMSClientMessageListener(lJMSClient));

				// this is a console app demo
				// so wait in a thread loop until the client get shut down
				mJMSEndPoint.start();
				try {
					while (!mJMSEndPoint.isShutdown()) {
						Thread.sleep(1000);
					}
				} catch (InterruptedException lEx) {
					// ignore a potential exception here
				}
				shutdownEndpoint();
			}
		}, "jWebSocketJMSClient");
		mIsThreadRunning = true;
		// start the endpoint all all listener have been assigned
		mEndpointThreadRunner.start();
	}

	private void shutdownEndpoint() {
		try {
			if (mIsThreadRunning) {
				mEndpointThreadRunner.join(2000);
				mEndpointThreadRunner.interrupt();
				mIsThreadRunning = false;
			}
		} catch (InterruptedException aException) {
			log(aException.getMessage());
		}
		if (mJMSEndPoint != null && !mJMSEndPoint.isShutdown()) {
			// if not yet done...
			log("Shutting down JMS Client Endpoint...");
			// shut the client properly down
			mJMSEndPoint.shutdown();
		}
		disableButtons();
		log("JMS Client Endpoint properly shutdown.");
	}

	private void pingEndpoint(String aEndpointId) {
		if (!mJMSEndPoint.isShutdown()) {
			mSender.ping(aEndpointId);
		}
	}

	private void getIdentification() {
		if (!mJMSEndPoint.isShutdown()) {
			mSender.getIdentification("*");
		}
	}

	private void enableButtons() {
		jtfBrokerURL.setEnabled(false);
		jtfEndpointID.setEnabled(false);
		jtfGatewayID.setEnabled(false);
		jtfTopic.setEnabled(false);
		jtfUsername.setEnabled(false);
		jPFPassword.setEnabled(false);
		jbIdentify.setEnabled(true);
		jbOpen.setEnabled(false);
		jbPing.setEnabled(true);
		jbSSO.setEnabled(true);
		jbShutdown.setEnabled(true);
		jbSendPayload.setEnabled(true);
	}

	private void disableButtons() {
		jtfBrokerURL.setEnabled(true);
		jtfEndpointID.setEnabled(true);
		jtfGatewayID.setEnabled(true);
		jtfTopic.setEnabled(true);
		jtfUsername.setEnabled(true);
		jPFPassword.setEnabled(true);
		jbIdentify.setEnabled(false);
		jbOpen.setEnabled(true);
		jbPing.setEnabled(false);
		jbSSO.setEnabled(false);
		jbShutdown.setEnabled(false);
		jbSendPayload.setEnabled(false);
	}

	/**
	 *
	 * @param aMessage
	 */
	public final void log(String aMessage) {
		System.out.println(aMessage);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jtfTopic = new javax.swing.JTextField();
        jtfBrokerURL = new javax.swing.JTextField();
        jtfGatewayID = new javax.swing.JTextField();
        jtfEndpointID = new javax.swing.JTextField();
        jbOpen = new javax.swing.JButton();
        jbShutdown = new javax.swing.JButton();
        jbPing = new javax.swing.JButton();
        jbIdentify = new javax.swing.JButton();
        jbSSO = new javax.swing.JButton();
        jbSendPayload = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jtfUsername = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPFPassword = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("JMS Client Test");
        setAlwaysOnTop(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                onWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Topic");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(28, 31, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setText("BrokerURL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(22, 31, 0, 0);
        getContentPane().add(jLabel2, gridBagConstraints);

        jLabel3.setText("Gateway ID");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 31, 0, 0);
        getContentPane().add(jLabel3, gridBagConstraints);

        jLabel4.setText("Endpoint ID");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 31, 0, 0);
        getContentPane().add(jLabel4, gridBagConstraints);

        jtfTopic.setToolTipText("");
        jtfTopic.setMinimumSize(null);
        jtfTopic.setPreferredSize(new java.awt.Dimension(450, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(22, 29, 0, 0);
        getContentPane().add(jtfTopic, gridBagConstraints);

        jtfBrokerURL.setToolTipText("");
        jtfBrokerURL.setMinimumSize(null);
        jtfBrokerURL.setPreferredSize(new java.awt.Dimension(450, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 29, 0, 0);
        getContentPane().add(jtfBrokerURL, gridBagConstraints);

        jtfGatewayID.setToolTipText("");
        jtfGatewayID.setMinimumSize(null);
        jtfGatewayID.setPreferredSize(new java.awt.Dimension(450, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 29, 0, 0);
        getContentPane().add(jtfGatewayID, gridBagConstraints);

        jtfEndpointID.setMinimumSize(null);
        jtfEndpointID.setPreferredSize(new java.awt.Dimension(450, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 29, 0, 0);
        getContentPane().add(jtfEndpointID, gridBagConstraints);

        jbOpen.setText("Open");
        jbOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 18, 11, 0);
        getContentPane().add(jbOpen, gridBagConstraints);

        jbShutdown.setText("Shutdown");
        jbShutdown.setEnabled(false);
        jbShutdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbShutdownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 6, 11, 0);
        getContentPane().add(jbShutdown, gridBagConstraints);

        jbPing.setText("Ping");
        jbPing.setEnabled(false);
        jbPing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbPingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 6, 11, 0);
        getContentPane().add(jbPing, gridBagConstraints);

        jbIdentify.setText("Identify");
        jbIdentify.setEnabled(false);
        jbIdentify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbIdentifyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 6, 11, 0);
        getContentPane().add(jbIdentify, gridBagConstraints);

        jbSSO.setText("SSO");
        jbSSO.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 6, 11, 0);
        getContentPane().add(jbSSO, gridBagConstraints);

        jbSendPayload.setText("Send Payload");
        jbSendPayload.setEnabled(false);
        jbSendPayload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSendPayloadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 6, 11, 19);
        getContentPane().add(jbSendPayload, gridBagConstraints);

        jLabel5.setText("Username");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(24, 31, 0, 0);
        getContentPane().add(jLabel5, gridBagConstraints);

        jtfUsername.setText("root");
        jtfUsername.setToolTipText("Username to login in jWebSocketServer");
        jtfUsername.setMinimumSize(null);
        jtfUsername.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 29, 0, 0);
        getContentPane().add(jtfUsername, gridBagConstraints);

        jLabel6.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(24, 31, 0, 0);
        getContentPane().add(jLabel6, gridBagConstraints);

        jPFPassword.setText("root");
        jPFPassword.setToolTipText("Password to login in jWebSocket Server");
        jPFPassword.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 29, 0, 0);
        getContentPane().add(jPFPassword, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel7.setText("Authenticate your endpoint with jWebSocket Server to have access to JMS Services");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 31, 0, 0);
        getContentPane().add(jLabel7, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenActionPerformed
		this.initializeEndpoint();
    }//GEN-LAST:event_jbOpenActionPerformed

    private void jbShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbShutdownActionPerformed
		this.shutdownEndpoint();
    }//GEN-LAST:event_jbShutdownActionPerformed

    private void jbPingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbPingActionPerformed
		String lEndpointId = JOptionPane.showInputDialog(this,
				"Please provide the ID of the endpoint you want to ping", "Ping an Endpoint", JOptionPane.QUESTION_MESSAGE);
		if (null != lEndpointId && !lEndpointId.trim().equals("")) {
			this.pingEndpoint(lEndpointId);
		}
    }//GEN-LAST:event_jbPingActionPerformed

    private void jbIdentifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbIdentifyActionPerformed
		this.getIdentification();
    }//GEN-LAST:event_jbIdentifyActionPerformed

    private void onWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosed
		this.shutdownEndpoint();
    }//GEN-LAST:event_onWindowClosed

    private void jbSendPayloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSendPayloadActionPerformed
		JMSSendPayloadDialog lPayloadDialog = new JMSSendPayloadDialog(this, mSender, mListener);
		lPayloadDialog.setLocation(this.getLocation().x,
				this.getLocation().y);
		lPayloadDialog.setAlwaysOnTop(true);
		lPayloadDialog.setVisible(true);
		final JMSClientDialog lMe = this;
		lMe.setVisible(false);
		lPayloadDialog.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
				lMe.setVisible(true);
				lMe.setAlwaysOnTop(true);
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
    }//GEN-LAST:event_jbSendPayloadActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(JMSClientDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(JMSClientDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(JMSClientDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(JMSClientDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JMSClientDialog().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPasswordField jPFPassword;
    private javax.swing.JButton jbIdentify;
    private javax.swing.JButton jbOpen;
    private javax.swing.JButton jbPing;
    private javax.swing.JButton jbSSO;
    private javax.swing.JButton jbSendPayload;
    private javax.swing.JButton jbShutdown;
    private javax.swing.JTextField jtfBrokerURL;
    private javax.swing.JTextField jtfEndpointID;
    private javax.swing.JTextField jtfGatewayID;
    private javax.swing.JTextField jtfTopic;
    private javax.swing.JTextField jtfUsername;
    // End of variables declaration//GEN-END:variables
}
