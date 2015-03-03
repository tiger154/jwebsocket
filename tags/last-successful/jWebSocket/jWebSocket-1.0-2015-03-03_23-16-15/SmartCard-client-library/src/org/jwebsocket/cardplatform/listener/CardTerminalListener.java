package org.jwebsocket.cardplatform.listener;

import java.applet.AppletContext;
import java.net.MalformedURLException;
import java.net.URL;
import javax.smartcardio.CardTerminal;
import org.jwebsocket.cardplatform.api.ICardTerminalListener;

/**
 * Notify the JavaScript side about the SmartCard reader events
 *
 */
public class CardTerminalListener implements ICardTerminalListener {

	private AppletContext mContext;

	public CardTerminalListener(AppletContext aContext) {
		this.mContext = aContext;
	}

	/**
	 * Notify the JavaScript side that a SmartCard reader is ready
	 */
	public void onTerminalReady(CardTerminal aTerminal) {
		try {
			mContext.showDocument(new URL("javascript:JcOnTerminalReady('"
					+ aTerminal.getName() + "')"));
		} catch (MalformedURLException ex) {
			System.out.println("ERROR: JcOnTerminalReady javascript function not found!"
					+ ex.getMessage());
		}
	}

	/**
	 * Notify the JavaScript side that a SmartCard reader is not ready
	 */
	public void onTerminalNotReady(CardTerminal aTerminal) {
		try {
			mContext.showDocument(new URL("javascript:JcOnTerminalNotReady('"
					+ aTerminal.getName() + "')"));
		} catch (MalformedURLException ex) {
			System.out.println("ERROR: JcOnTerminalNotReady javascript function not found!"
					+ ex.getMessage());
		}
	}
}
