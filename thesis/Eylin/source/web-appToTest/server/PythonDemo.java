package org.jwebsocket.python.demo;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

public class PythonDemo extends TokenPlugIn {

	public PythonDemo(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
	}

	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if ((aToken.getNS().equals(getNamespace()))
				&& (aToken.getType().equals("chat"))) {
			String msg = aToken.getString("message");
			String from = aToken.getString("sender");

			Token result = createResponse(aToken);

			result.setString("message", msg);
			result.setString("sender", from);
			result.setString("type", "chat");

			Iterator clients = getServer().getAllConnectors().values().iterator();
			while (clients.hasNext()) {
				WebSocketConnector c = (WebSocketConnector) clients.next();
				if (c.equals(aConnector)) {
					continue;
				}
				try {
					getServer().sendToken(c, result);
				} catch (Exception e) {
					System.out.println("**** Aqui este el villano ****" + msg);
				}
			}
		}
	}
}
