package org.jwebsocket.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 * Massive tests client
 *
 * @author Rolando Santamaria Maso
 */
public class StressTest {

	/**
	 *
	 */
	public static int connections = 0;
	/**
	 *
	 */
	public static int concurrentConnections = 1000;

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		final List<BaseTokenClient> lClients = new ArrayList();
		try {
			for (int i = 0; i < concurrentConnections; i++) {
				final BaseTokenClient lClient = new BaseTokenClient();
				lClient.setPingInterval(1000);
				lClient.addListener(new WebSocketClientListener() {
					@Override
					public void processOpening(WebSocketClientEvent aEvent) {
					}

					@Override
					public void processOpened(WebSocketClientEvent aEvent) {
						System.out.println("Connections opened: " + (++connections));
					}

					@Override
					public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
					}

					@Override
					public void processClosed(WebSocketClientEvent aEvent) {
						System.out.println("Connection closed. Currently active: " + (--connections));
					}

					@Override
					public void processReconnecting(WebSocketClientEvent aEvent) {
					}
				});

				lClient.open("ws://localhost:8787/jWebSocket/jWebSocket");
				lClients.add(lClient);
				Thread.sleep(50);
			}

			Tools.getTimer().scheduleAtFixedRate(new JWSTimerTask() {
				@Override
				public void runTask() {
					for (BaseTokenClient lClient : lClients) {
						try {
							if (lClient.isConnected()) {
								lClient.ping(true);
							}
						} catch (WebSocketException ex) {
							Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}, 3000, 3000);
		} catch (Exception ex) {
			Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
