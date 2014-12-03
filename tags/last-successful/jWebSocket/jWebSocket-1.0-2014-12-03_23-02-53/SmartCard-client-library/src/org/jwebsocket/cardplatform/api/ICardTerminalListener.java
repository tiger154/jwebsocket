package org.jwebsocket.cardplatform.api;

import javax.smartcardio.CardTerminal;


public interface ICardTerminalListener {

    /**
     * A card terminal gets ready
     *
     * @param aTerminal
     */
    void onTerminalReady(CardTerminal aTerminal);

    /**
     * A card terminal gets unready
     *
     * @param aTerminal
     */
    void onTerminalNotReady(CardTerminal aTerminal);
}
