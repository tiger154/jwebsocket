/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.JMSClient;

import java.util.Properties;
import org.jwebsocket.jms.endpoint.JWSEndPointMessageListener;
import org.jwebsocket.jms.endpoint.JWSEndPointSender;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Victor Antonio Barzana Crespo
 */
public class JMSSendPayloadDialog extends javax.swing.JFrame {

	private JWSEndPointSender mSender;
	private JWSEndPointMessageListener mListener;
	private JMSClientDialog mParentDialog;
	private int mLoop = 0;
	private int mResponses = 0;
	private int mTotalIterations = 0;
	private long mStart;
	private long mTotalTime;
	private long mElapsedTime;
	private long[] mAverage;
	private boolean mIsAsynchronous;

	/**
	 * Creates new form SendPayloadDialog
	 */
	public JMSSendPayloadDialog() {
		initComponents();
	}

	/**
	 *
	 * @param aParentDialog
	 * @param aSender
	 * @param aListener
	 */
	public JMSSendPayloadDialog(JMSClientDialog aParentDialog, JWSEndPointSender aSender, JWSEndPointMessageListener aListener) {
		initComponents();
		mSender = aSender;
		mParentDialog = aParentDialog;
		mListener = aListener;
		mAverage = new long[0];
		extractProperties();
	}

	private void extractProperties() {
		Properties lProperties = mParentDialog.getProperties();
		if (!lProperties.isEmpty()) {
			String lTargetId = lProperties.getProperty("targetID");
			String lTopic = lProperties.getProperty("gatewayTopic");
			String lType = lProperties.getProperty("type");
			String lArguments = lProperties.getProperty("arguments");
			String lPayload = lProperties.getProperty("payload");

			if (lTargetId != null) {
				jtTargetId.setText(lTargetId);
			}
			if (lTopic != null) {
				jtTopic.setText(lTopic);
			}
			if (lType != null) {
				jtType.setText(lType);
			}
			if (lArguments != null) {
				jtArgs.setText(lArguments);
			}
			if (lPayload != null) {
				jtPayload.setText(lPayload);
			}
		}
		if (mParentDialog.mMainDialog.getJcbWrap().isEnabled()) {
			jCBWordWrap.setEnabled(true);
		} else {
			jCBWordWrap.setEnabled(false);
		}
	}

	private void sendPayload() {
		String lTargetId = jtTargetId.getText();
		String lNS = jtTopic.getText();
		final String lType = jtType.getText();

		Token lArgs;
		lArgs = TokenFactory.createToken();
		try {
			String lArgsText = jtArgs.getText();
			if (lArgsText != null) {
				lArgs = JSONProcessor.JSONStringToToken(lArgsText);
			}
		} catch (Exception lEx) {
			mParentDialog.log("There was an error while parsing the arguments, "
					+ "please check that you are entering the entries separated by comma");
			return;
		}

		String lPayload = jtPayload.getText();

		if (!mListener.hasResponseListener(lNS, lType)) {
			mListener.addResponseListener(lNS, lType, new JWSMessageListener(mSender) {
				@Override
				public void processToken(String aSourceId, Token aToken) {
					if (jcbRepeat.isSelected()) {
						mParentDialog.log("-----------------------------------------------------");
						mAverage[mResponses] = (System.currentTimeMillis() - mAverage[mResponses]);
						mParentDialog.log("FINISHED ITERATION: " + (mResponses + 1)
								+ ", with: " + mAverage[mResponses] + " miliseconds");
						mResponses++;
					}
//					mParentDialog.log("Received '" + lType + "' from '" + aSourceId + ".");
					mParentDialog.log("Response: " + aToken.toString());

					if (!mIsAsynchronous && mLoop > 0) {
						mLoop--;
						sendPayload();
						return;
					}
					if (mResponses == mTotalIterations + 1) {
						if (jcbRepeat.isSelected()) {
							mParentDialog.log("--------------------------------------------------------");
							mParentDialog.log("                 TOTAL TEST RESULTS");
							mParentDialog.log("  Miliseconds per iteration:            ");
							mParentDialog.log("--------------------------------------------------------");
							long lSum = 0;
							for (int lIdx = 0; lIdx < mAverage.length; lIdx++) {
								lSum += mAverage[lIdx];
								mParentDialog.log("Iteration " + (lIdx + 1) + ": " + " ----------------- " + mAverage[lIdx] + " ms");
							}
							mTotalTime = (System.currentTimeMillis() - mElapsedTime) / 1000;
							mParentDialog.log("--------------------------------------------------------");
							mParentDialog.log("AVERAGE: " + lSum / mAverage.length + " ms");
							mParentDialog.log("TOTAL TIME: " + mTotalTime + " seconds to send "
									+ (mTotalIterations + 1) + " " + jtType.getText()
									+ " operations to target: " + jtTargetId.getText());
							mParentDialog.log("--------------------------------------------------------");
						}
						mTotalIterations = 0;
						mResponses = 0;
						mAverage = new long[mTotalIterations];
					}
				}
			});
		}

		if (mIsAsynchronous) {
			while (mLoop >= 0) {
				if (jcbRepeat.isSelected()) {
					mParentDialog.log("-----------------------------------------------------");
					mParentDialog.log("STARTED LOAD TEST ITERATION: "
							+ (mTotalIterations - mLoop + 1));
					// Saving start time
					mAverage[mTotalIterations - mLoop] = System.currentTimeMillis();
				}
				mParentDialog.log("Sending payload:\" "
						+ "Target ID: " + lTargetId
						+ ", Gateway ID: " + lNS
						+ ", Type: " + lType
						+ ", Arguments: " + lArgs.toString()
						+ ", Payload: " + lPayload
						+ "\"");
				mSender.sendPayload(lTargetId, lNS, lType, lArgs.getMap(), lPayload);
				mLoop--;
			}
		} else {
			if (jcbRepeat.isSelected()) {
				mParentDialog.log("-----------------------------------------------------");
				mParentDialog.log("STARTED LOAD TEST ITERATION: "
						+ (mTotalIterations - mLoop + 1));
				// Saving start time
				mAverage[mTotalIterations - mLoop] = System.currentTimeMillis();
			}
			mSender.sendPayload(lTargetId, lNS, lType, lArgs.getMap(), lPayload);
			mParentDialog.log("Sending payload:\" "
					+ "Target ID: " + lTargetId
					+ ", Gateway ID: " + lNS
					+ ", Type: " + lType
					+ ", Arguments: " + lArgs.toString()
					+ ", Payload: " + lPayload
					+ "\"");
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jtTargetId = new javax.swing.JTextField();
        jtTopic = new javax.swing.JTextField();
        jtType = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtPayload = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtArgs = new javax.swing.JTextArea();
        jbSend = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jcbRepeat = new javax.swing.JCheckBox();
        jtfRepeatTest = new javax.swing.JTextField();
        jBClear = new javax.swing.JButton();
        jCBWordWrap = new javax.swing.JCheckBox();
        jCBAsinchronous = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Send payload to a specified target");
        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                onWindowClosed(evt);
            }
        });

        jLabel1.setText("Target ID");

        jLabel3.setText("Namespace");

        jLabel4.setText("Type of message");

        jLabel5.setText("Arguments");

        jLabel6.setText("Payload");

        jtPayload.setColumns(20);
        jtPayload.setRows(5);
        jtPayload.setText("{}");
        jScrollPane1.setViewportView(jtPayload);

        jtArgs.setColumns(20);
        jtArgs.setLineWrap(true);
        jtArgs.setRows(5);
        jScrollPane2.setViewportView(jtArgs);

        jbSend.setText("Send");
        jbSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSendActionPerformed(evt);
            }
        });

        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jcbRepeat.setText("Repeat Test");
        jcbRepeat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbRepeatActionPerformed(evt);
            }
        });

        jtfRepeatTest.setText("5");
        jtfRepeatTest.setEnabled(false);

        jBClear.setText("Clear");
        jBClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBClearActionPerformed(evt);
            }
        });

        jCBWordWrap.setText("Word Wrap");
        jCBWordWrap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBWordWrapActionPerformed(evt);
            }
        });

        jCBAsinchronous.setText("Asynchronous ");
        jCBAsinchronous.setEnabled(false);
        jCBAsinchronous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAsinchronousActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtTopic, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jtType)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtTargetId, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(111, 111, 111)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jcbRepeat)
                            .addComponent(jCBWordWrap))
                        .addGap(18, 18, 18)
                        .addComponent(jtfRepeatTest))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jbSend)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBClear)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jCBAsinchronous)))
                .addGap(92, 92, 92))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jtTargetId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jtTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jtType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jcbRepeat)
                            .addComponent(jtfRepeatTest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, Short.MAX_VALUE)
                        .addComponent(jCBWordWrap))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCBAsinchronous)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbSend)
                    .addComponent(jButton2)
                    .addComponent(jBClear))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jbSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSendActionPerformed
		if (jcbRepeat.isSelected()) {
			try {
				mLoop = Integer.parseInt(jtfRepeatTest.getText()) - 1;
				mIsAsynchronous = jCBAsinchronous.isSelected();
				mTotalIterations = mLoop;
				mAverage = new long[mTotalIterations + 1];
				mElapsedTime = System.currentTimeMillis();
			} catch (Exception aException) {
				mParentDialog.log(aException.getMessage());
			}
		}
		this.sendPayload();
    }//GEN-LAST:event_jbSendActionPerformed

    private void jcbRepeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbRepeatActionPerformed
		if (jcbRepeat.isSelected()) {
			jtfRepeatTest.setEnabled(true);
			jCBAsinchronous.setEnabled(true);
		} else {
			jtfRepeatTest.setEnabled(false);
			jCBAsinchronous.setEnabled(false);
		}
    }//GEN-LAST:event_jcbRepeatActionPerformed

    private void onWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosed
		String lNS = jtTopic.getText();
		final String lType = jtType.getText();
		if (mListener.hasResponseListener(lNS, lType)) {
			mParentDialog.log("Removing response listener: " + lType + " from: " + lNS);
			mListener.removeResponseListener(lNS, lType);
		}
    }//GEN-LAST:event_onWindowClosed

    private void jCBWordWrapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBWordWrapActionPerformed
		mParentDialog.mLog.setLineWrap(jCBWordWrap.isSelected());
		mParentDialog.mMainDialog.getJcbWrap().setSelected(jCBWordWrap.isSelected());
    }//GEN-LAST:event_jCBWordWrapActionPerformed

    private void jBClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBClearActionPerformed
		mParentDialog.mLog.setText("");
		mParentDialog.mMainDialog.initializeLogs();
    }//GEN-LAST:event_jBClearActionPerformed

    private void jCBAsinchronousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAsinchronousActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_jCBAsinchronousActionPerformed

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
			java.util.logging.Logger.getLogger(JMSSendPayloadDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(JMSSendPayloadDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(JMSSendPayloadDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(JMSSendPayloadDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JMSSendPayloadDialog().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClear;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCBAsinchronous;
    private javax.swing.JCheckBox jCBWordWrap;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jbSend;
    private javax.swing.JCheckBox jcbRepeat;
    private javax.swing.JTextArea jtArgs;
    private javax.swing.JTextArea jtPayload;
    private javax.swing.JTextField jtTargetId;
    private javax.swing.JTextField jtTopic;
    private javax.swing.JTextField jtType;
    private javax.swing.JTextField jtfRepeatTest;
    // End of variables declaration//GEN-END:variables
}
