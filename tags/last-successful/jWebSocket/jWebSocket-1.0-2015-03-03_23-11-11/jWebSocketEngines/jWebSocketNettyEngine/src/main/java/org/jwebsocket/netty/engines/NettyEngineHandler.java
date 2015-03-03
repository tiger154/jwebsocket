//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------

package org.jwebsocket.netty.engines;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketHandshake;
import org.jwebsocket.kit.WebSocketRuntimeException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.netty.connectors.NettyConnector;
import org.jwebsocket.netty.http.HttpHeaders;

/**
 * Handler class for the <tt>NettyEngine</tt> that recieves the events based on
 * event types and notifies the client connectors. This handler also handles the
 * initial handshaking for WebSocket connection with a appropriate hand shake
 * response. This handler is created for each new connection channel.
 * <p>
 * Once the handshaking is successful after sending the handshake {@code
 * HttpResponse} it replaces the {@code HttpRequestDecoder} and {@code
 * HttpResponseEncoder} from the channel pipeline with {@code
 * WebSocketFrameDecoder} as WebSocket frame data decoder and {@code
 * WebSocketFrameEncoder} as WebSocket frame data encoder. Also it starts the
 * <tt>NettyConnector</tt>.
 * </p>
 *
 * @author <a href="http://www.purans.net/">Puran Singh</a>
 * @version $Id: NettyEngineHandler.java 613 2010-07-01 07:13:29Z mailtopuran@gmail.com $
 */
public class NettyEngineHandler extends SimpleChannelUpstreamHandler {

	private static final Logger mLog = Logging.getLogger(NettyEngineHandler.class);
	private NettyEngine mEngine = null;
	private WebSocketConnector mConnector = null;
	private ChannelHandlerContext mContext = null;
	private static final ChannelGroup mChannels = new DefaultChannelGroup();
	private static final String CONTENT_LENGTH = "Content-Length";

	public NettyEngineHandler(NettyEngine aEngine) {
		this.mEngine = aEngine;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelBound(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.channelBound(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelClosed(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.channelClosed(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelConnected(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		// Get the SslHandler in the current pipeline.
		final SslHandler sslHandler = aCtx.getPipeline().get(SslHandler.class);
		// Get notified when SSL handshake is done.

		// Added by Alex to prevent exceptions
		// TODO: Fix this exceptions on connect!
		// ADD-START
		if (sslHandler != null) 
		// ADD-END
		{
			try {
				ChannelFuture lHandshakeFuture = sslHandler.handshake();
				lHandshakeFuture.addListener(new SecureWebSocketConnectionListener(sslHandler));
			} catch (Exception es) {
				es.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelDisconnected(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Channel is disconnected");
		}
		// remove the channel
		mChannels.remove(aEvent.getChannel());

		this.mContext = aCtx;
		super.channelDisconnected(aCtx, aEvent);
		mEngine.connectorStopped(mConnector, CloseReason.CLIENT);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelInterestChanged(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.channelInterestChanged(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void channelOpen(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.channelOpen(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void channelUnbound(ChannelHandlerContext aCtx, ChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.channelUnbound(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void childChannelClosed(ChannelHandlerContext aCtx, ChildChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.childChannelClosed(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void childChannelOpen(ChannelHandlerContext aCtx, ChildChannelStateEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.childChannelOpen(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext aCtx, ExceptionEvent aEvent) throws Exception {
		this.mContext = aCtx;
		if (mLog.isDebugEnabled()) {
			mLog.debug("Channel is disconnected:" + aEvent.getCause().getLocalizedMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleUpstream(ChannelHandlerContext aCtx, ChannelEvent aEvent) throws Exception {
		this.mContext = aCtx;
		super.handleUpstream(aCtx, aEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageReceived(ChannelHandlerContext aCtx, MessageEvent aEvent) throws Exception {
		this.mContext = aCtx;
		if (mLog.isDebugEnabled()) {
			mLog.debug("message received in the engine handler");
		}
		Object lMsg = aEvent.getMessage();
		if (lMsg instanceof HttpRequest) {
			handleHttpRequest(aCtx, (HttpRequest) lMsg);
		} else if (lMsg instanceof WebSocketFrame) {
			handleWebSocketFrame(aCtx, (WebSocketFrame) lMsg);
		}
	}

	/**
	 * private method that sends the handshake response for WebSocket connection
	 *
	 * @param aCtx the channel context
	 * @param aReq http request object
	 * @param aResp http response object
	 */
	private void sendHttpResponse(ChannelHandlerContext aCtx, HttpRequest aReq, HttpResponse aResp) {
		// Generate an error page if response status code is not OK (200).
		if (aResp.getStatus().getCode() != 200) {
			aResp.setContent(ChannelBuffers.copiedBuffer(aResp.getStatus().toString(), CharsetUtil.UTF_8));
			setContentLength(aResp, aResp.getContent().readableBytes());
		}
		// Send the response and close the connection if necessary.
		ChannelFuture lCF = aCtx.getChannel().write(aResp);
		if (!isKeepAlive(aReq) || aResp.getStatus().getCode() != 200) {
			lCF.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Check if the request header has Keep-Alive
	 *
	 * @param aReq the http request object
	 * @return {@code true} if keep-alive is set in the header {@code false}
	 *         otherwise
	 */
	private boolean isKeepAlive(HttpRequest aReq) {
		String lKeepAlive = aReq.getHeader(HttpHeaders.Values.KEEP_ALIVE);
		if (lKeepAlive != null && lKeepAlive.length() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Set the content length in the response
	 *
	 * @param res the http response object
	 * @param aReadableBytes the length of the bytes
	 */
	private void setContentLength(HttpResponse aResp, int aReadableBytes) {
		aResp.setHeader(CONTENT_LENGTH, aReadableBytes);
	}

	/**
	 * private method that handles the web socket frame data, this method is
	 * used only after the WebSocket connection is established.
	 *
	 * @param aCtx the channel handler context
	 * @param aMsg the web socket frame data
	 */
	private void handleWebSocketFrame(ChannelHandlerContext aCtx, WebSocketFrame aMsg) throws WebSocketRuntimeException {
		String lTextData = "";
		if (aMsg.isBinary()) {
			// TODO: handle binary data
		} else if (aMsg.isText()) {
			lTextData = aMsg.getTextData();
		} else {
			throw new WebSocketRuntimeException("Frame Doesn't contain any type of data");
		}
		mEngine.processPacket(mConnector, new RawPacket(lTextData));
	}

	/**
	 * Handles the initial HTTP request for handshaking if the http request
	 * contains Upgrade header value as WebSocket then this method sends the
	 * handshake response and also fires the events on client connector.
	 *
	 * @param aCtx the channel handler context
	 * @param req  the request message
	 */
	private void handleHttpRequest(ChannelHandlerContext aCtx, HttpRequest aReq) {
		// Allow only GET methods.
		if (aReq.getMethod() != HttpMethod.GET) {
			sendHttpResponse(aCtx, aReq, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}
		// Serve the WebSocket handshake request.
		if (HttpHeaders.Values.UPGRADE.equalsIgnoreCase(aReq.getHeader(HttpHeaders.Names.CONNECTION))
				&& HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(aReq.getHeader(HttpHeaders.Names.UPGRADE))) {
			// Create the WebSocket handshake response.
			HttpResponse lResp = null;
			try {
				lResp = constructHandShakeResponse(aReq, aCtx);
			} catch (NoSuchAlgorithmException lNSAEx) {
				// better to close the channel
				mLog.debug("Channel is disconnected");
				aCtx.getChannel().close();
			}

			// write the response
			aCtx.getChannel().write(lResp);

			mChannels.add(aCtx.getChannel());

			// since handshaking is done, replace the encoder/decoder with
			// web socket data frame encoder/decoder
			ChannelPipeline lPipeline = aCtx.getChannel().getPipeline();
			lPipeline.remove("aggregator");
			EngineConfiguration lConfig = mEngine.getConfiguration();
			if (lConfig == null || lConfig.getMaxFramesize() == 0) {
				lPipeline.replace("decoder", "jwsdecoder", new WebSocketFrameDecoder(JWebSocketCommonConstants.DEFAULT_MAX_FRAME_SIZE));
			} else {
				lPipeline.replace("decoder", "jwsdecoder", new WebSocketFrameDecoder(lConfig.getMaxFramesize()));
			}
			lPipeline.replace("encoder", "jwsencoder", new WebSocketFrameEncoder());

			//if the WebSocket connection URI is wss then start SSL TLS handshaking
			if (aReq.getUri().startsWith("wss:")) {
				// Get the SslHandler in the current pipeline.
				final SslHandler sslHandler = aCtx.getPipeline().get(SslHandler.class);
				// Get notified when SSL handshake is done.
				ChannelFuture lHandshakeFuture = sslHandler.handshake();
				lHandshakeFuture.addListener(new SecureWebSocketConnectionListener(sslHandler));
			}
			// initialize the connector
			mConnector = initializeConnector(aCtx, aReq);

			return;
		}

		// Send an error page otherwise.
		sendHttpResponse(aCtx, aReq, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
	}

	/**
	 * Constructs the <tt>HttpResponse</tt> object for the handshake response
	 *
	 * @param aReq the http request object
	 * @param aCtx the channel handler context
	 * @return the http handshake response
	 * @throws NoSuchAlgorithmException
	 */
	private HttpResponse constructHandShakeResponse(HttpRequest aReq, ChannelHandlerContext aCtx) throws NoSuchAlgorithmException {
		boolean secKey = aReq.containsHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY);
		HttpResponse lResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
		if (secKey) {
			lResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101, "Switching Protocols")); 
		}
		lResp.addHeader(HttpHeaders.Names.UPGRADE, "websocket");
		lResp.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);

		String lProtocol = aReq.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL);
		
		// Fill in the headers and contents depending on handshake method.
		if (aReq.containsHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY1) && aReq.containsHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY2)) {
			// New handshake method with a challenge:
			lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN, aReq.getHeader(HttpHeaders.Names.ORIGIN));
			lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(aReq));
			
			// Added by Alex 2010-10-25:
			// fallback for FlashBridge (which sends "WebSocket-Protocol"
			// instead of "Sec-WebSocket-Protocol"
			if (lProtocol != null) {
				lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, lProtocol);
			} else {
				lProtocol = aReq.getHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL);
				if (lProtocol != null) {
					lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, lProtocol);
				}
			}
			// Calculate the answer of the challenge.
			String lKey1 = aReq.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY1);
			String lKey2 = aReq.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY2);
			int lA = (int) (Long.parseLong(lKey1.replaceAll("[^0-9]", "")) / lKey1.replaceAll("[^ ]", "").length());
			int lB = (int) (Long.parseLong(lKey2.replaceAll("[^0-9]", "")) / lKey2.replaceAll("[^ ]", "").length());
			long lC = aReq.getContent().readLong();
			ChannelBuffer lInput = ChannelBuffers.buffer(16);
			lInput.writeInt(lA);
			lInput.writeInt(lB);
			lInput.writeLong(lC);
			ChannelBuffer lOutput = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(lInput.array()));
			lResp.setContent(lOutput);
		} else if (secKey){
			//version 14, http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-14			
			String key = aReq.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY);
			
			//get the accept key from handshake util
			String acceptKey = WebSocketHandshake.calcHybiSecKeyAccept(key);
			lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_ACCEPT, acceptKey);
			if (lProtocol != null) {
				lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, lProtocol);
			} else {
				lProtocol = aReq.getHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL);
				if (lProtocol != null) {
					lResp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, lProtocol);
				}
			}
			
		} else {
			// Older handshake method with no challenge:
			lResp.addHeader(HttpHeaders.Names.WEBSOCKET_ORIGIN, aReq.getHeader(HttpHeaders.Names.ORIGIN));
			lResp.addHeader(HttpHeaders.Names.WEBSOCKET_LOCATION, getWebSocketLocation(aReq));
			if (lProtocol != null) {
				lResp.addHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL, lProtocol);
			}
		}
		return lResp;

	}

	/**
	 * Initialize the {@code NettyConnector} after initial handshaking is
	 * successfull.
	 *
	 * @param aCtx the channel handler context
	 * @param req the http request object
	 */
	private WebSocketConnector initializeConnector(ChannelHandlerContext aCtx, HttpRequest aReq) {

		RequestHeader lHeader = getRequestHeader(aReq);
		int lSessionTimeout = lHeader.getTimeout(JWebSocketCommonConstants.DEFAULT_TIMEOUT);
		if (lSessionTimeout > 0) {
			aCtx.getChannel().getConfig().setConnectTimeoutMillis(lSessionTimeout);
		}
		// create connector
		WebSocketConnector lConnector = new NettyConnector(mEngine, this);
		lConnector.setHeader(lHeader);

		mEngine.getConnectors().put(lConnector.getId(), lConnector);
		lConnector.startConnector();
		// allow descendant classes to handle connector started event
		mEngine.connectorStarted(lConnector);
		return lConnector;

	}

	/**
	 * Construct the request header to save it in the connector
	 *
	 * @param aReq the http request header
	 * @return the request header
	 */
	private RequestHeader getRequestHeader(HttpRequest aReq) {
		RequestHeader lHeader = new RequestHeader();
		Map<String, String> lArgs = new FastMap<String, String>();
		String lSearchString = "";
		String lPath = aReq.getUri();

		// isolate search string
		int lPos = lPath.indexOf(JWebSocketCommonConstants.PATHARG_SEPARATOR);
		if (lPos >= 0) {
			lSearchString = lPath.substring(lPos + 1);
			if (lSearchString.length() > 0) {
				String[] lKeyValPairs = lSearchString.split(JWebSocketCommonConstants.ARGARG_SEPARATOR);
				for (int lIdx = 0; lIdx < lKeyValPairs.length; lIdx++) {
					String[] lKeyVal = lKeyValPairs[lIdx].split(JWebSocketCommonConstants.KEYVAL_SEPARATOR, 2);
					if (lKeyVal.length == 2) {
						lArgs.put(lKeyVal[0], lKeyVal[1]);
						if (mLog.isDebugEnabled()) {
							mLog.debug("arg" + lIdx + ": " + lKeyVal[0] + "=" + lKeyVal[1]);
						}
					}
				}
			}
		}

		// set default sub protocol if none passed
		// if no sub protocol given in request header,
		// try to get it from arguments
		String lSubProt = aReq.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL);
		if (lSubProt == null) {
			lSubProt = lArgs.get(RequestHeader.WS_PROTOCOL);
		}
		if (lSubProt == null) {
			lSubProt = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}
		lHeader.put(RequestHeader.URL_ARGS, lArgs);
		lHeader.put(RequestHeader.WS_ORIGIN, aReq.getHeader(HttpHeaders.Names.ORIGIN));
		lHeader.put(RequestHeader.WS_LOCATION, getWebSocketLocation(aReq));
		lHeader.put(RequestHeader.WS_PATH, aReq.getUri());

		lHeader.put(RequestHeader.WS_PROTOCOL, lSubProt);

		lHeader.put(RequestHeader.WS_SEARCHSTRING, lSearchString);
		lHeader.put(RequestHeader.WS_HOST, aReq.getHeader(HttpHeaders.Names.HOST));
		return lHeader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeComplete(ChannelHandlerContext aCtx, WriteCompletionEvent aEvent) throws Exception {
		super.writeComplete(aCtx, aEvent);
	}

	/**
	 * Returns the web socket location URL
	 *
	 * @param aReq the http request object
	 * @return the location url string
	 */
	private String getWebSocketLocation(HttpRequest aReq) {
		//TODO: fix this URL for wss: (secure)
		String location = "ws://" + aReq.getHeader(HttpHeaders.Names.HOST) + aReq.getUri();
		return location;
	}

	/**
	 * Returns the channel context
	 *
	 * @return the channel context
	 */
	public ChannelHandlerContext getChannelHandlerContext() {
		return mContext;
	}

	/**
	 * Listener class for SSL TLS handshake completion.
	 */
	private static final class SecureWebSocketConnectionListener implements ChannelFutureListener {

		private final SslHandler mSSLHandler;

		SecureWebSocketConnectionListener(SslHandler aSSLHandler) {
			this.mSSLHandler = aSSLHandler;
		}

		@Override
		public void operationComplete(ChannelFuture aFuture) throws Exception {
			if (aFuture.isSuccess()) {
				// that means SSL handshaking is done.
				if (mLog.isInfoEnabled()) {
					mLog.info("SSL handshaking success");
				}
			} else {
				aFuture.getChannel().close();
			}
		}
	}
}
