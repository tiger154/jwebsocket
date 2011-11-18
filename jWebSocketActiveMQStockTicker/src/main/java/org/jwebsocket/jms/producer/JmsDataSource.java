//	---------------------------------------------------------------------------
//	jWebSocket - JmsDataSource
//	Copyright (c) 2011, Innotrade GmbH - jWebSocket.org, Alexander Schulze
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
package org.jwebsocket.jms.producer;

/**
 * 
 * @author Johannes Smutny
 */
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class JmsDataSource implements Runnable {

	private JmsTemplate jmsTemplate;

	public static void main(String[] args) {
		// ClassPathXmlApplicationContext ctx = new
		// ClassPathXmlApplicationContext("classpath*:JmsDataSource.xml");
		new JmsDataSource().run();
	}

	public JmsDataSource() {
		this.jmsTemplate = new JmsTemplate();
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false");
		this.jmsTemplate.setConnectionFactory(connectionFactory);
		this.jmsTemplate.setDefaultDestinationName("stock.topic");
		this.jmsTemplate.setDeliveryPersistent(false);
		this.jmsTemplate.setPubSubDomain(true);
		this.jmsTemplate.setSessionTransacted(false);
	}

	public void run() {
		System.out.println("JmsDataSource started");

		_random = new SecureRandom(new SecureRandom().generateSeed(20));
		System.arraycopy(_STOCKS_ORIG, 0, _STOCKS, 0, _STOCKS.length);
		try {
			while (true) {
				_changeStock();
				Thread.sleep(500L);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void _changeStock() {
		// pick the stock to update
		int stockIndex = _random.nextInt(STOCK_ROW_COUNT);

		float oldVal = (Float) _STOCKS[(stockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX];

		// calculate the percent change (and round it)

		// the maximum fluctuation is a percentage of the current value...
		float maxFluctuate = FLUCTUATION_PERCENT * oldVal;

		// ... take a random percentage of that maximum...
		float actualFluctuate = _random.nextFloat() * maxFluctuate;

		// now round it to two decimal places
		actualFluctuate = 100 * actualFluctuate;
		long delta = Math.round(actualFluctuate);

		// minimum delta change should never go below zero
		if (delta < 1)
			delta = 1;

		// randomly switch positive / negative delta
		if (_random.nextFloat() < 0.5)
			delta = -delta;

		delta += (long) (oldVal * 100);

		// if a stock ever falls too low, reset it to original value
		if ((delta < MIN_VALUE_PENNIES) || (delta > MAX_VALUE_PENNIES))
			_STOCKS[(stockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX] = _STOCKS_ORIG[(stockIndex * STOCK_COLUMN_COUNT)
					+ PRICE_COLUMN_INDEX];
		else {
			float newVal = (float) delta / 100;

			_STOCKS[(stockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX] = newVal;
		}
		_sendStock(stockIndex);

	}

	private void _sendStock(int stockIndex) {
		String body = _STOCKS[(stockIndex * STOCK_COLUMN_COUNT)] + ":"
				+ _STOCKS[(stockIndex * STOCK_COLUMN_COUNT) + TICKER_COLUMN_INDEX] + ":"
				+ _displayFormat.format(_STOCKS[(stockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX]);

		// try {
		// TextMessage message = _session.createTextMessage(body);
		// message.setStringProperty("symbol", (String) _STOCKS[(stockIndex
		// * STOCK_COLUMN_COUNT)
		// + TICKER_COLUMN_INDEX]);
		// _messageProducer.send(message, DeliveryMode.NON_PERSISTENT,
		// Message.DEFAULT_PRIORITY, 0L);
		jmsTemplate.convertAndSend(body);
		// } catch (JMSException e) {
		// _logger.error("Failure to send message: " + body);
		// }
	}

	static final private Object[] _STOCKS_ORIG = { "3m Co", "MMM", 71.72F, "AT&T Inc.", "T", 31.61F, "Boeing Co.",
			"BA", 75.43F, "Citigroup, Inc.", "C", 49.37F, "Hewlett-Packard Co.", "HPQ", 36.53F, "Intel Corporation",
			"INTC", 19.88F, "International Business Machines", "IBM", 81.41F, "McDonald\"s Corporation", "MCD", 36.76F,
			"Microsoft Corporation", "MSFT", 25.84F, "Verizon Communications", "VZ", 35.57F, "Wal-Mart Stores, Inc.",
			"WMT", 45.45F, };
	static private Object[] _STOCKS = new Object[_STOCKS_ORIG.length];

	private static final int STOCK_COLUMN_COUNT = 3;
	private static final int STOCK_ROW_COUNT = _STOCKS.length / STOCK_COLUMN_COUNT;
	private static final int TICKER_COLUMN_INDEX = 1;
	private static final int PRICE_COLUMN_INDEX = 2;
	private static final float FLUCTUATION_PERCENT = 0.02F;
	private static final int MIN_VALUE_PENNIES = 10;
	private static final int MAX_VALUE_PENNIES = 20000;

	static private Random _random;
	// formatter used to send prices as decimal currency (Locale.ENGLISH allows
	// us to always use period as separator)
	static private DecimalFormat _displayFormat = new DecimalFormat("####.00", new DecimalFormatSymbols(Locale.ENGLISH));

}
