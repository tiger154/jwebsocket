package org.jwebsocket.cardplatform;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import org.jwebsocket.cardplatform.api.ICardTerminalListener;

public class TerminalHandler {

	private List<CardTerminal> mActiveTerminals = new LinkedList<CardTerminal>();

	public List<CardTerminal> getActiveTerminals() {
		return mActiveTerminals;
	}
	private Map<String, CardChannel> mChannels =
			new LinkedHashMap<String, CardChannel>();

	public Map<String, CardChannel> getChannels() {
		return mChannels;
	}
	private Thread mWorker;
	private TerminalFactory mFactory = TerminalFactory.getDefault();
	private List<ICardTerminalListener> mListeners =
			new LinkedList<ICardTerminalListener>();

	public TerminalHandler() {
		mWorker = new Thread(new Runnable() {

			public void run() {
				try {
					java.util.List<CardTerminal> terminals =
							mFactory.terminals().list();
					while (true) {

						for (CardTerminal ct : terminals) {
							if (ct.isCardPresent()) {
								if (!mActiveTerminals.contains(ct)) {
									notifyTerminalReady(ct);
									mActiveTerminals.add(ct);
								}
							} else {
								if (mActiveTerminals.contains(ct)) {
									notifyTerminalNotReady(ct);
									mActiveTerminals.remove(ct);
								}
							}
						}

						//Sleeping the thread for a while...
						Thread.sleep(100);
					}
				} catch (Exception ex) {
					Logger.getLogger(TerminalHandler.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		}, "Card terminals listener thread");
	}

	public void initialize() {
		mWorker.start();
	}

	private void notifyTerminalReady(CardTerminal aTerminal) {
		try {
			getChannels().put(aTerminal.getName(),
					aTerminal.connect("*").getBasicChannel());
		} catch (CardException ex) {
			Logger.getLogger(TerminalHandler.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		for (ICardTerminalListener lListener : mListeners) {
			lListener.onTerminalReady(aTerminal);
		}
	}

	private void notifyTerminalNotReady(CardTerminal aTerminal) {
		for (ICardTerminalListener lListener : mListeners) {
			lListener.onTerminalNotReady(aTerminal);
		}
	}

	/**
	 * Subscribe a card terminal listener to listen the SmartCard reader events
	 *
	 * @param aListener
	 */
	public void subscribe(ICardTerminalListener aListener) {
		mListeners.add(aListener);
	}

	/**
	 * Unsubscribe a card terminal listener
	 *
	 * @param aListener
	 */
	public void unsubscribe(ICardTerminalListener aListener) {
		mListeners.remove(aListener);
	}
}
