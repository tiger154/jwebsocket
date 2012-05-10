/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.comet.sample;

import java.util.Collection;
import java.util.Iterator;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.token.Token;

/**
 *
 * @author naruto
 */
public class PollingSample extends TokenPlugIn {

    public PollingSample(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        setNamespace(aConfiguration.getNamespace());
    }

    @Override
    public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

        if (aToken.getNS().equals(getNamespace())) {
            if (aToken.getType().equals("chat")) {
                String name = aToken.getString("message");

                Token result = createResponse(aToken);

                result.setString("data", name);
                WebSocketConnector c;
                Iterator<WebSocketConnector> clients = getServer().getAllConnectors().values().iterator();
                while (clients.hasNext()) {
                    c = clients.next();
                    if (!c.equals(aConnector)) {
                        try {
                            getServer().sendToken(c, result);
                        } catch (Exception e) {
                            getServer().sendToken(c, result);
                        }
                        
                    }
                }
            }
        }
    }
}
