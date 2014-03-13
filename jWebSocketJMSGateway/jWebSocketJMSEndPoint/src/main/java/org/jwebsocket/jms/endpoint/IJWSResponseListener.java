/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.endpoint;

import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public interface IJWSResponseListener extends IJMSResponseListener {

	/**
	 * Called when the sent token processing has timeout on the remote endpoint
	 *
	 * @param aToken
	 */
	void onProgress(Token aToken);
}
