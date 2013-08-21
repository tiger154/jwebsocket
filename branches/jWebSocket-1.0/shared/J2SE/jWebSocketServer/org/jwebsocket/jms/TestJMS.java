package org.jwebsocket.jms;

import com.mongodb.Mongo;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kyberneees
 */
public class TestJMS {

	public static void main(String[] args) {
		try {
			Mongo mongo = new Mongo("localhost");
			
		} catch (Exception ex) {
			Logger.getLogger(TestJMS.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
