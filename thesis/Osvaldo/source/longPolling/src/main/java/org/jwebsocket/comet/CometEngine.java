/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.comet;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javolution.util.FastMap;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.comet.servlet.TomcatServlet;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.logging.Logging;

/**
 * @author Osvaldo Aguilar Lauzurique
 * @email osvaldo2627@hab.uci.cu
 */
public class CometEngine extends BaseEngine {

    TomcatServlet mServlet;
    private static Logger mLog = Logging.getLogger(CometEngine.class);
    Map<String, Queue<WebSocketPacket>> mPacketsQueue = new FastMap();

    public boolean isQueuePacketEmpty(String aIdConnector){
        return mPacketsQueue.get(aIdConnector).isEmpty();
    }
    
    
    
    public TomcatServlet getServlet() {
        return mServlet;
    }

    public void setServlet(TomcatServlet aServlet) {
        this.mServlet = aServlet;
    }

    public CometEngine(EngineConfiguration aConfiguration) {
        super(aConfiguration);
    }

    public Map<String, Queue<WebSocketPacket>> getPacketsQueue() {
        return mPacketsQueue;
    }

    public void setPacketsQueue(Map<String, Queue<WebSocketPacket>> aPacketsQueue) {
        this.mPacketsQueue = aPacketsQueue;
    }

    @Override
    public void addConnector(WebSocketConnector aConnector) {
        super.addConnector(aConnector);
        mPacketsQueue.put(aConnector.getId(), new ConcurrentLinkedQueue());
    }

    @Override
    public void startEngine() throws WebSocketException {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Starting engine...");
        }

        super.startEngine();
    }

    @Override
    public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Stoping engine...");
        }

        //@TODO check this
        mServlet.destroy();
        super.stopEngine(aCloseReason);
    }
}
