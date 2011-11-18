//	---------------------------------------------------------------------------
//	jWebSocket - WebSoocket Demo MIDlet
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.midlets;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.me.BaseClientJ2ME;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.listener.WebSocketClientEvent;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenClient;

/**
 * @author aschulze
 */
public class JWebSocketJavaMEDemo extends MIDlet implements CommandListener, WebSocketClientTokenListener {

	private boolean midletPaused = false;
	private TokenClient mJWC = null;
	private int prevStatus = TokenClient.DISCONNECTED;
	//<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
	private Command cmdExit;
	private Command cmdConnect;
	private Command cmdDisconnect;
	private Command cmdLogin;
	private Command cmdLogout;
	private Command cmdSend;
	private Command cmdBroadcast;
	private Command cmdSetup;
	private Command cmdOk;
	private Command cmdCamera;
	private Form frmDemo;
	private StringItem stiLog;
	private TextField txfTarget;
	private TextField txfMessage;
	private TextField txfURL;
	private ImageItem imgStatus;
	private Image imgDisconnected;
	private Image imgConnected;
	private Image imgAuthenticated;
	//</editor-fold>//GEN-END:|fields|0|

	/**
	 * The JWebSocketJavaMEDemo constructor.
	 */
	public JWebSocketJavaMEDemo() {
	}

	private void checkStatusIcon() {
		int lStatus = TokenClient.DISCONNECTED;
		if (mJWC.getUsername() != null) {
			lStatus = TokenClient.AUTHENTICATED;
		} else if (mJWC.isConnected()) {
			lStatus = TokenClient.CONNECTED;
		}
		if (lStatus != prevStatus) {
			prevStatus = lStatus;
			if (lStatus == TokenClient.AUTHENTICATED) {
				imgStatus.setImage(getImgAuthenticated());
			} else if (lStatus == TokenClient.CONNECTED) {
				imgStatus.setImage(getImgConnected());
			} else {
				imgStatus.setImage(getImgDisconnected());
			}
		}
	}

	public void processOpened(WebSocketClientEvent aEvent) {
		stiLog.setText("Connection opened.");
		checkStatusIcon();
	}

	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		// stiLog.setText("received:" + aPacket.getString());
	}

	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		stiLog.setText("Received JSON: " + aToken.toString());
		checkStatusIcon();
	}

	public void processClosed(WebSocketClientEvent aEvent) {
		stiLog.setText("Connection closed.");
		checkStatusIcon();
	}

	//<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
	//</editor-fold>//GEN-END:|methods|0|
	//<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
	/**
	 * Initilizes the application.
	 * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
	 */
	private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
		// write pre-initialize user code here
//GEN-LINE:|0-initialize|1|0-postInitialize
		// write post-initialize user code here
		mJWC = new TokenClient(new BaseClientJ2ME());
		mJWC.addListener(this);
	}//GEN-BEGIN:|0-initialize|2|
	//</editor-fold>//GEN-END:|0-initialize|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
	/**
	 * Performs an action assigned to the Mobile Device - MIDlet Started point.
	 */
	public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
		// write pre-action user code here
		switchDisplayable(null, getFrmDemo());//GEN-LINE:|3-startMIDlet|1|3-postAction
		// write post-action user code here
	}//GEN-BEGIN:|3-startMIDlet|2|
	//</editor-fold>//GEN-END:|3-startMIDlet|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
	/**
	 * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
	 */
	public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
		// write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
		// write post-action user code here
	}//GEN-BEGIN:|4-resumeMIDlet|2|
	//</editor-fold>//GEN-END:|4-resumeMIDlet|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
	/**
	 * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
	 * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
	 * @param nextDisplayable the Displayable to be set
	 */
	public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
		// write pre-switch user code here
		Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
		if (alert == null) {
			display.setCurrent(nextDisplayable);
		} else {
			display.setCurrent(alert, nextDisplayable);
		}//GEN-END:|5-switchDisplayable|1|5-postSwitch
		// write post-switch user code here
	}//GEN-BEGIN:|5-switchDisplayable|2|
	//</editor-fold>//GEN-END:|5-switchDisplayable|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
	/**
	 * Called by a system to indicated that a command has been invoked on a particular displayable.
	 * @param command the Command that was invoked
	 * @param displayable the Displayable where the command was invoked
	 */
	public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
		// write pre-action user code here
		if (displayable == frmDemo) {//GEN-BEGIN:|7-commandAction|1|33-preAction
			if (command == cmdBroadcast) {//GEN-END:|7-commandAction|1|33-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|2|33-postAction
				// write post-action user code here
				try {
					mJWC.broadcastText(txfMessage.getString());
				} catch (WebSocketException ex) {
					stiLog.setText(ex.getMessage());
				}
			} else if (command == cmdCamera) {//GEN-LINE:|7-commandAction|3|53-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|4|53-postAction
				// write post-action user code here
				Camera lCamera = new Camera(mJWC, this, frmDemo);

			} else if (command == cmdConnect) {//GEN-LINE:|7-commandAction|5|23-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|6|23-postAction
				// write post-action user code here
				try {
					stiLog.setText("Connecting... (start)");
					mJWC.open(txfURL.getString());
					stiLog.setText("Connecting... (done)");
				} catch (WebSocketException ex) {
					stiLog.setText(ex.getMessage());
				}
			} else if (command == cmdDisconnect) {//GEN-LINE:|7-commandAction|7|25-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|8|25-postAction
				// write post-action user code here
				try {
					mJWC.close();
				} catch (WebSocketException ex) {
					stiLog.setText(ex.getMessage());
				}
			} else if (command == cmdExit) {//GEN-LINE:|7-commandAction|9|19-preAction
				// write pre-action user code here
				try {
					mJWC.close();
				} catch (WebSocketException ex) {
					stiLog.setText(ex.getMessage());
				}
				exitMIDlet();//GEN-LINE:|7-commandAction|10|19-postAction
				// write post-action user code here
			} else if (command == cmdLogin) {//GEN-LINE:|7-commandAction|11|27-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|12|27-postAction
				// write post-action user code here
				try {
					mJWC.login("guest", "guest");
				} catch (WebSocketException ex) {
					stiLog.setText(ex.getMessage());
				}
			} else if (command == cmdLogout) {//GEN-LINE:|7-commandAction|13|29-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|14|29-postAction
				// write post-action user code here
				try {
					mJWC.logout();
				} catch (WebSocketException ex) {
					stiLog.setText(ex.getMessage());
				}
			} else if (command == cmdSend) {//GEN-LINE:|7-commandAction|15|31-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|16|31-postAction
				// write post-action user code here
			} else if (command == cmdSetup) {//GEN-LINE:|7-commandAction|17|38-preAction
				// write pre-action user code here
//GEN-LINE:|7-commandAction|18|38-postAction
				// write post-action user code here
			}//GEN-BEGIN:|7-commandAction|19|7-postCommandAction
		}//GEN-END:|7-commandAction|19|7-postCommandAction
		// write post-action user code here
	}//GEN-BEGIN:|7-commandAction|20|
	//</editor-fold>//GEN-END:|7-commandAction|20|

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdExit ">//GEN-BEGIN:|18-getter|0|18-preInit
	/**
	 * Returns an initiliazed instance of cmdExit component.
	 * @return the initialized component instance
	 */
	public Command getCmdExit() {
		if (cmdExit == null) {//GEN-END:|18-getter|0|18-preInit
			// write pre-init user code here
			cmdExit = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
			// write post-init user code here
		}//GEN-BEGIN:|18-getter|2|
		return cmdExit;
	}
	//</editor-fold>//GEN-END:|18-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: frmDemo ">//GEN-BEGIN:|14-getter|0|14-preInit
	/**
	 * Returns an initiliazed instance of frmDemo component.
	 * @return the initialized component instance
	 */
	public Form getFrmDemo() {
		if (frmDemo == null) {//GEN-END:|14-getter|0|14-preInit
			// write pre-init user code here
			frmDemo = new Form("jWebSocket Fundamental Demo", new Item[] { getTxfURL(), getTxfTarget(), getTxfMessage(), getImgStatus(), getStiLog() });//GEN-BEGIN:|14-getter|1|14-postInit
			frmDemo.addCommand(getCmdExit());
			frmDemo.addCommand(getCmdConnect());
			frmDemo.addCommand(getCmdDisconnect());
			frmDemo.addCommand(getCmdLogin());
			frmDemo.addCommand(getCmdLogout());
			frmDemo.addCommand(getCmdSend());
			frmDemo.addCommand(getCmdBroadcast());
			frmDemo.addCommand(getCmdSetup());
			frmDemo.addCommand(getCmdCamera());
			frmDemo.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
			// write post-init user code here
		}//GEN-BEGIN:|14-getter|2|
		return frmDemo;
	}
	//</editor-fold>//GEN-END:|14-getter|2|
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: stiLog ">//GEN-BEGIN:|16-getter|0|16-preInit
	/**
	 * Returns an initiliazed instance of stiLog component.
	 * @return the initialized component instance
	 */
	public StringItem getStiLog() {
		if (stiLog == null) {//GEN-END:|16-getter|0|16-preInit
			// write pre-init user code here
			stiLog = new StringItem("", "...", Item.PLAIN);//GEN-BEGIN:|16-getter|1|16-postInit
			stiLog.setLayout(ImageItem.LAYOUT_DEFAULT | ImageItem.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND | Item.LAYOUT_2);//GEN-END:|16-getter|1|16-postInit
			// write post-init user code here
		}//GEN-BEGIN:|16-getter|2|
		return stiLog;
	}
	//</editor-fold>//GEN-END:|16-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdConnect ">//GEN-BEGIN:|22-getter|0|22-preInit
	/**
	 * Returns an initiliazed instance of cmdConnect component.
	 * @return the initialized component instance
	 */
	public Command getCmdConnect() {
		if (cmdConnect == null) {//GEN-END:|22-getter|0|22-preInit
			// write pre-init user code here
			cmdConnect = new Command("Connect", Command.ITEM, 0);//GEN-LINE:|22-getter|1|22-postInit
			// write post-init user code here
		}//GEN-BEGIN:|22-getter|2|
		return cmdConnect;
	}
	//</editor-fold>//GEN-END:|22-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdDisconnect ">//GEN-BEGIN:|24-getter|0|24-preInit
	/**
	 * Returns an initiliazed instance of cmdDisconnect component.
	 * @return the initialized component instance
	 */
	public Command getCmdDisconnect() {
		if (cmdDisconnect == null) {//GEN-END:|24-getter|0|24-preInit
			// write pre-init user code here
			cmdDisconnect = new Command("Disconnect", Command.ITEM, 0);//GEN-LINE:|24-getter|1|24-postInit
			// write post-init user code here
		}//GEN-BEGIN:|24-getter|2|
		return cmdDisconnect;
	}
	//</editor-fold>//GEN-END:|24-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdLogin ">//GEN-BEGIN:|26-getter|0|26-preInit
	/**
	 * Returns an initiliazed instance of cmdLogin component.
	 * @return the initialized component instance
	 */
	public Command getCmdLogin() {
		if (cmdLogin == null) {//GEN-END:|26-getter|0|26-preInit
			// write pre-init user code here
			cmdLogin = new Command("Login", Command.ITEM, 0);//GEN-LINE:|26-getter|1|26-postInit
			// write post-init user code here
		}//GEN-BEGIN:|26-getter|2|
		return cmdLogin;
	}
	//</editor-fold>//GEN-END:|26-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdLogout ">//GEN-BEGIN:|28-getter|0|28-preInit
	/**
	 * Returns an initiliazed instance of cmdLogout component.
	 * @return the initialized component instance
	 */
	public Command getCmdLogout() {
		if (cmdLogout == null) {//GEN-END:|28-getter|0|28-preInit
			// write pre-init user code here
			cmdLogout = new Command("Logout", Command.ITEM, 0);//GEN-LINE:|28-getter|1|28-postInit
			// write post-init user code here
		}//GEN-BEGIN:|28-getter|2|
		return cmdLogout;
	}
	//</editor-fold>//GEN-END:|28-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdSend ">//GEN-BEGIN:|30-getter|0|30-preInit
	/**
	 * Returns an initiliazed instance of cmdSend component.
	 * @return the initialized component instance
	 */
	public Command getCmdSend() {
		if (cmdSend == null) {//GEN-END:|30-getter|0|30-preInit
			// write pre-init user code here
			cmdSend = new Command("Send", Command.ITEM, 0);//GEN-LINE:|30-getter|1|30-postInit
			// write post-init user code here
		}//GEN-BEGIN:|30-getter|2|
		return cmdSend;
	}
	//</editor-fold>//GEN-END:|30-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdBroadcast ">//GEN-BEGIN:|32-getter|0|32-preInit
	/**
	 * Returns an initiliazed instance of cmdBroadcast component.
	 * @return the initialized component instance
	 */
	public Command getCmdBroadcast() {
		if (cmdBroadcast == null) {//GEN-END:|32-getter|0|32-preInit
			// write pre-init user code here
			cmdBroadcast = new Command("Broadcast", Command.ITEM, 0);//GEN-LINE:|32-getter|1|32-postInit
			// write post-init user code here
		}//GEN-BEGIN:|32-getter|2|
		return cmdBroadcast;
	}
	//</editor-fold>//GEN-END:|32-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: txfTarget ">//GEN-BEGIN:|35-getter|0|35-preInit
	/**
	 * Returns an initiliazed instance of txfTarget component.
	 * @return the initialized component instance
	 */
	public TextField getTxfTarget() {
		if (txfTarget == null) {//GEN-END:|35-getter|0|35-preInit
			// write pre-init user code here
			txfTarget = new TextField("Target:", "*", 10, TextField.ANY);//GEN-BEGIN:|35-getter|1|35-postInit
			txfTarget.setLayout(ImageItem.LAYOUT_DEFAULT);
			txfTarget.setPreferredSize(-1, -1);//GEN-END:|35-getter|1|35-postInit
			// write post-init user code here
		}//GEN-BEGIN:|35-getter|2|
		return txfTarget;
	}
	//</editor-fold>//GEN-END:|35-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: txfMessage ">//GEN-BEGIN:|36-getter|0|36-preInit
	/**
	 * Returns an initiliazed instance of txfMessage component.
	 * @return the initialized component instance
	 */
	public TextField getTxfMessage() {
		if (txfMessage == null) {//GEN-END:|36-getter|0|36-preInit
			// write pre-init user code here
			txfMessage = new TextField("Message:", "Hello from BlackBerry Device!", 50, TextField.ANY);//GEN-BEGIN:|36-getter|1|36-postInit
			txfMessage.setLayout(ImageItem.LAYOUT_DEFAULT);
			txfMessage.setPreferredSize(-1, -1);//GEN-END:|36-getter|1|36-postInit
			// write post-init user code here
		}//GEN-BEGIN:|36-getter|2|
		return txfMessage;
	}
	//</editor-fold>//GEN-END:|36-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdSetup ">//GEN-BEGIN:|37-getter|0|37-preInit
	/**
	 * Returns an initiliazed instance of cmdSetup component.
	 * @return the initialized component instance
	 */
	public Command getCmdSetup() {
		if (cmdSetup == null) {//GEN-END:|37-getter|0|37-preInit
			// write pre-init user code here
			cmdSetup = new Command("Setup", Command.ITEM, 0);//GEN-LINE:|37-getter|1|37-postInit
			// write post-init user code here
		}//GEN-BEGIN:|37-getter|2|
		return cmdSetup;
	}
	//</editor-fold>//GEN-END:|37-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdOk ">//GEN-BEGIN:|43-getter|0|43-preInit
	/**
	 * Returns an initiliazed instance of cmdOk component.
	 * @return the initialized component instance
	 */
	public Command getCmdOk() {
		if (cmdOk == null) {//GEN-END:|43-getter|0|43-preInit
			// write pre-init user code here
			cmdOk = new Command("Save", Command.OK, 0);//GEN-LINE:|43-getter|1|43-postInit
			// write post-init user code here
		}//GEN-BEGIN:|43-getter|2|
		return cmdOk;
	}
	//</editor-fold>//GEN-END:|43-getter|2|
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: txfURL ">//GEN-BEGIN:|47-getter|0|47-preInit
	/**
	 * Returns an initiliazed instance of txfURL component.
	 * @return the initialized component instance
	 */
	public TextField getTxfURL() {
		if (txfURL == null) {//GEN-END:|47-getter|0|47-preInit
			// write pre-init user code here
			txfURL = new TextField("URL:", "ws://jwebsocket.org:8787", 50, TextField.ANY);//GEN-BEGIN:|47-getter|1|47-postInit
			txfURL.setLayout(ImageItem.LAYOUT_DEFAULT);
			txfURL.setPreferredSize(-1, -1);//GEN-END:|47-getter|1|47-postInit
			// write post-init user code here
		}//GEN-BEGIN:|47-getter|2|
		return txfURL;
	}
	//</editor-fold>//GEN-END:|47-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: imgStatus ">//GEN-BEGIN:|48-getter|0|48-preInit
	/**
	 * Returns an initiliazed instance of imgStatus component.
	 * @return the initialized component instance
	 */
	public ImageItem getImgStatus() {
		if (imgStatus == null) {//GEN-END:|48-getter|0|48-preInit
			// write pre-init user code here
			imgStatus = new ImageItem("", getImgDisconnected(), ImageItem.LAYOUT_DEFAULT, "<Missing Image>");//GEN-LINE:|48-getter|1|48-postInit
			// write post-init user code here
		}//GEN-BEGIN:|48-getter|2|
		return imgStatus;
	}
	//</editor-fold>//GEN-END:|48-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: imgAuthenticated ">//GEN-BEGIN:|49-getter|0|49-preInit
	/**
	 * Returns an initiliazed instance of imgAuthenticated component.
	 * @return the initialized component instance
	 */
	public Image getImgAuthenticated() {
		if (imgAuthenticated == null) {//GEN-END:|49-getter|0|49-preInit
			// write pre-init user code here
			try {//GEN-BEGIN:|49-getter|1|49-@java.io.IOException
				imgAuthenticated = Image.createImage("/images/authenticated.png");
			} catch (java.io.IOException e) {//GEN-END:|49-getter|1|49-@java.io.IOException
				e.printStackTrace();
			}//GEN-LINE:|49-getter|2|49-postInit
			// write post-init user code here
		}//GEN-BEGIN:|49-getter|3|
		return imgAuthenticated;
	}
	//</editor-fold>//GEN-END:|49-getter|3|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: imgConnected ">//GEN-BEGIN:|50-getter|0|50-preInit
	/**
	 * Returns an initiliazed instance of imgConnected component.
	 * @return the initialized component instance
	 */
	public Image getImgConnected() {
		if (imgConnected == null) {//GEN-END:|50-getter|0|50-preInit
			// write pre-init user code here
			try {//GEN-BEGIN:|50-getter|1|50-@java.io.IOException
				imgConnected = Image.createImage("/images/connected.png");
			} catch (java.io.IOException e) {//GEN-END:|50-getter|1|50-@java.io.IOException
				e.printStackTrace();
			}//GEN-LINE:|50-getter|2|50-postInit
			// write post-init user code here
		}//GEN-BEGIN:|50-getter|3|
		return imgConnected;
	}
	//</editor-fold>//GEN-END:|50-getter|3|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: imgDisconnected ">//GEN-BEGIN:|51-getter|0|51-preInit
	/**
	 * Returns an initiliazed instance of imgDisconnected component.
	 * @return the initialized component instance
	 */
	public Image getImgDisconnected() {
		if (imgDisconnected == null) {//GEN-END:|51-getter|0|51-preInit
			// write pre-init user code here
			try {//GEN-BEGIN:|51-getter|1|51-@java.io.IOException
				imgDisconnected = Image.createImage("/images/disconnected.png");
			} catch (java.io.IOException e) {//GEN-END:|51-getter|1|51-@java.io.IOException
				e.printStackTrace();
			}//GEN-LINE:|51-getter|2|51-postInit
			// write post-init user code here
		}//GEN-BEGIN:|51-getter|3|
		return imgDisconnected;
	}
	//</editor-fold>//GEN-END:|51-getter|3|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdCamera ">//GEN-BEGIN:|52-getter|0|52-preInit
	/**
	 * Returns an initiliazed instance of cmdCamera component.
	 * @return the initialized component instance
	 */
	public Command getCmdCamera() {
		if (cmdCamera == null) {//GEN-END:|52-getter|0|52-preInit
			// write pre-init user code here
			cmdCamera = new Command("Camera", Command.ITEM, 0);//GEN-LINE:|52-getter|1|52-postInit
			// write post-init user code here
		}//GEN-BEGIN:|52-getter|2|
		return cmdCamera;
	}
	//</editor-fold>//GEN-END:|52-getter|2|

	/**
	 * Returns a display instance.
	 * @return the display instance.
	 */
	public Display getDisplay() {
		return Display.getDisplay(this);
	}

	/**
	 * Exits MIDlet.
	 */
	public void exitMIDlet() {
		switchDisplayable(null, null);
		destroyApp(true);
		notifyDestroyed();
	}

	/**
	 * Called when MIDlet is started.
	 * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
	 */
	public void startApp() {
		if (midletPaused) {
			resumeMIDlet();
		} else {
			initialize();
			startMIDlet();
		}
		midletPaused = false;
	}

	/**
	 * Called when MIDlet is paused.
	 */
	public void pauseApp() {
		midletPaused = true;
	}

	/**
	 * Called to signal the MIDlet to terminate.
	 * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
	 */
	public void destroyApp(boolean unconditional) {
	}
}
