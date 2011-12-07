package org.jwebsocket.eventmodel.event.card;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author kyberneees
 */
public class JcTerminalReady extends C2SEvent {

	private String terminal;

	public String getTerminal() {
		return terminal;
	}

	@ImportFromToken
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
}
