package org.jwebsocket.watchdog.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author kyberneees
 */
public interface IWatchDogService  extends Remote{

	boolean start() throws RemoteException;

	boolean shutdown() throws RemoteException;

	boolean restart() throws RemoteException;
}
