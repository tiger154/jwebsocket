package org.jwebsocket.eventmodel.event.card;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author kyberneees
 */
public class JcTerminalNotReady extends C2SEvent {

	public String getTerminal() {
		return getArgs().getString("terminal");
	}

	@ImportFromToken
	public void setTerminal(String aTerminal) {
		getArgs().setString("terminal", aTerminal);
	}
}
