package org.jwebsocket.cardplatform;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.swing.JApplet;
import org.jwebsocket.cardplatform.listener.CardTerminalListener;
import org.jwebsocket.cardplatform.util.Hex;

/**
 * This is the Java applet that acts as bridge in the communication
 * between the SmartCard reader and the JavaScrip side.
 *
 */
public class CardPlugIn extends JApplet {

	TerminalHandler mTerminalHandler = new TerminalHandler();

	public TerminalHandler getTerminalHandler() {
		return mTerminalHandler;
	}

	public void setTerminalHandler(TerminalHandler aTerminalHandler) {
		this.mTerminalHandler = aTerminalHandler;
	}

	/**
	 *
	 * @return A comma separated string with the active terminal names list
	 */
	public String getActiveTerminalNames() {
		String lTerminalNames = "";

		if (!mTerminalHandler.getActiveTerminals().isEmpty()) {
			for (CardTerminal lTerminal :
                            mTerminalHandler.getActiveTerminals()) {
			     lTerminalNames =
                             lTerminalNames.concat(lTerminal.getName() + ",");
			}
	        return lTerminalNames.substring(0, lTerminalNames.length() - 1);
		}
	 return lTerminalNames;
	}

	/**
	 * Transmit and APDU command to the card
	 * 
	 * @param aTerminalName
	 * @param aCommandAPDU
	 * 
	 * @return The response APDU or null in case of failure
	 */
	public String transmit(String aTerminalName, String aCommandAPDU) {

		if (mTerminalHandler.getChannels().containsKey(aTerminalName)) {
		 try {
		  ResponseAPDU lResponse =
                  mTerminalHandler.getChannels().get(aTerminalName).transmit(
                       new CommandAPDU(Hex.hexStringToByteArray(aCommandAPDU)));

		  return Hex.hexByteArrayToString(lResponse.getBytes());
		 } catch (CardException ex) {
		    Logger.getLogger(CardPlugIn.class.getName()).log(
                            Level.SEVERE, null, ex);
		 }
		}

	 return null;
	}

	@Override
	public void init() {
	 mTerminalHandler.subscribe(new CardTerminalListener(getAppletContext()));
	 mTerminalHandler.initialize();
	}
}
