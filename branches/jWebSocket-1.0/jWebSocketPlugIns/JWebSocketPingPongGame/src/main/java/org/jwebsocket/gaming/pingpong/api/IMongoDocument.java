/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.api;

import com.mongodb.DBObject;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author armando
 */
public interface IMongoDocument {

	/**
	 * 
	 * @return The object instance as a MongoDB document
	 */
	DBObject asDocument() throws UnsupportedEncodingException;
}
