// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 jwebsocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.netty.connectors;

import java.util.Map;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.async.IOFutureListener;

/**
 * NIO implementation of {@code IOFuture} to support asynchronous
 * I/O operation for the NIO based connector.
 * @author puran
 * @version $Id: NIOFuture.java 1056 2010-09-23 04:33:00Z mailtopuran@gmail.com $
 */
public class NIOFuture implements IOFuture {
  
  private ChannelFuture internalFuture = null; 
  private WebSocketConnector connector = null;

  /**
   * internal map that maps the jWebSocket IOFutureListener to Netty's channel future listener.
   */
  private Map<IOFutureListener, ChannelFutureListener> listenerMap = new ConcurrentHashMap<IOFutureListener, ChannelFutureListener>();
  
  /**
   * The constructor
   * @param theConnector the connector with which this future is associated
   * @param nettyFuture the internal netty future object that does the most 
   * of the work
   */
  public NIOFuture(WebSocketConnector theConnector, ChannelFuture nettyFuture) {
    this.internalFuture = nettyFuture;
    this.connector = theConnector;
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public WebSocketConnector getConnector() {
    return connector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDone() {
    return internalFuture.isDone();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isCancelled() {
    return internalFuture.isCancelled();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSuccess() {
    return internalFuture.isSuccess();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Throwable getCause() {
    return internalFuture.getCause();
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancel() {
    return internalFuture.cancel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean setSuccess() {
    return internalFuture.setSuccess();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean setFailure(Throwable cause) {
    return internalFuture.setFailure(cause);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean setProgress(long amount, long current, long total) {
    return internalFuture.setProgress(amount, current, total);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addListener(IOFutureListener listener) {
    ChannelFutureListener internalListener = new NIOInternalFutureListener(this, listener);
    listenerMap.put(listener, internalListener);
    internalFuture.addListener(internalListener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeListener(IOFutureListener listener) {
    if (listenerMap.containsKey(listener)) {
      internalFuture.removeListener(listenerMap.get(listener));
    }
  }

  public ChannelFuture getInternalFuture() {
    return internalFuture;
  }

}
