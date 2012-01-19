package org.jwebsocket.watchdog;

import java.rmi.RemoteException;
import java.util.Map;
import org.jwebsocket.watchdog.api.IWatchDogService;

/**
 *
 * @author kyberneees
 */
public class WatchDogServiceImpl implements IWatchDogService {

    private ProcessBuilder pb = new ProcessBuilder("java", "-jar", "jWebSocketServer.jar");

    @Override
    public boolean restart() throws RemoteException {
        return true;
    }

    @Override
    public boolean shutdown() throws RemoteException {
        
        return true;
    }

    @Override
    public boolean start() throws RemoteException {
        pb = new ProcessBuilder("java", "-jar", "jWebSocketServer.jar");
        Map<String, String> env = pb.environment();

        /*
        env.put("VAR1", "myValue");
        env.remove("OTHERVAR");
        env.put("VAR2", env.get("VAR1") + "suffix");
        pb.directory("myDir");
         */
       // Process p = pb.start();
        return true;
    }
}
