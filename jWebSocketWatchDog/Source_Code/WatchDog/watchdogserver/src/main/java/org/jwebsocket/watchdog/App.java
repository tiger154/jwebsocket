package org.jwebsocket.watchdog;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.jwebsocket.watchdog.api.IWatchDogService;

/**
 * Hello world!
 *
 */
public class App {
    // System.setSecurityManager(

    public static void main(String[] args) {
        new RMISecurityManager() {

            @Override
            public void checkConnect(String host, int port) {
            }

            @Override
            public void checkConnect(String host, int port, Object context) {
            }
        };


        try {
            String name = "WatchDog";
            IWatchDogService service = new WatchDogServiceImpl();
            IWatchDogService server = (IWatchDogService) UnicastRemoteObject.exportObject(service, 0);
            Registry registry = LocateRegistry.createRegistry(1234);
            registry.rebind(name, server);
            System.out.println(">> WatchDog running...");
        } catch (Exception e) {
            System.err.println("WatchDog exception:");
            e.printStackTrace();
        }
    }
}
