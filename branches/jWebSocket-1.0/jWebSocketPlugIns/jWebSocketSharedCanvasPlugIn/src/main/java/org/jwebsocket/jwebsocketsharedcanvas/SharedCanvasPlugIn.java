package org.jwebsocket.jwebsocketsharedcanvas;

import java.util.Collection;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

public class SharedCanvasPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(SharedCanvasPlugIn.class);
	private Collection<WebSocketConnector> mClients;

	public SharedCanvasPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
		mClients = new FastList<WebSocketConnector>().shared();
		if(mLog.isDebugEnabled()){
			mLog.debug("Canvas Plugin instantiated correctly...");
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if ("register".equals(aToken.getType())) {
				mClients.add(aConnector);
			} else {
				broadcast(aToken);
			}
		}
	}

	public void broadcast(Token aToken) {
		for (WebSocketConnector c : mClients) {
			getServer().sendToken(c, aToken);
		}
	}
}