package org.jwebsocket.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.broker.BrokerService;

/**
 *
 * @author kyberneees
 */
public class TestJMS {

	public static void main(String[] args) {
		try {
			BrokerService broker = new BrokerService();
		} catch (Exception ex) {
			Logger.getLogger(TestJMS.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
