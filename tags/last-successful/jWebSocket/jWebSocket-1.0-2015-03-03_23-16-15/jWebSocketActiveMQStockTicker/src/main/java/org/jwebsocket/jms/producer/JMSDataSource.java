//	---------------------------------------------------------------------------
//	jWebSocket - JMSDataSource (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.jms.producer;

/**
 *
 * @author Johannes Smutny, Alexander Schulze
 */
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author Alexander Schulze
 */
@Service
public class JMSDataSource implements Runnable {

	private final JmsTemplate mJMSTemplate;
	private static final Object[] mSTOCKS_ORIG = {
		"3m Co", "MMM", 71.72F,
		"AT&T Inc.", "T", 31.61F,
		"Boeing Co.", "BA", 75.43F,
		"Citigroup, Inc.", "C", 49.37F,
		"Hewlett-Packard Co.", "HPQ", 36.53F,
		"Intel Corporation", "INTC", 19.88F,
		"International Business Machines", "IBM", 81.41F,
		"McDonald\"s Corporation", "MCD", 36.76F,
		"Microsoft Corporation", "MSFT", 25.84F,
		"Verizon Communications", "VZ", 35.57F,
		"Wal-Mart Stores, Inc.", "WMT", 45.45F,};
	private static final Object[] mSTOCKS = new Object[mSTOCKS_ORIG.length];
	private static final int STOCK_COLUMN_COUNT = 3;
	private static final int STOCK_ROW_COUNT = mSTOCKS.length / STOCK_COLUMN_COUNT;
	private static final int TICKER_COLUMN_INDEX = 1;
	private static final int PRICE_COLUMN_INDEX = 2;
	private static final float FLUCTUATION_PERCENT = 0.02F;
	private static final int MIN_VALUE_PENNIES = 10;
	private static final int MAX_VALUE_PENNIES = 20000;
	static private Random mRandom;
	// formatter used to send prices as decimal currency (Locale.ENGLISH allows
	// us to always use period as separator)
	private static final DecimalFormat mDisplayFormat = new DecimalFormat("####.00", new DecimalFormatSymbols(Locale.ENGLISH));

	/**
	 *
	 * @param aArgs
	 */
	public static void main(String[] aArgs) {
		// ClassPathXmlApplicationContext ctx = new
		// ClassPathXmlApplicationContext("classpath*:JMSDataSource.xml");
		new JMSDataSource().run();
	}

	/**
	 *
	 */
	public JMSDataSource() {
		this.mJMSTemplate = new JmsTemplate();
		ActiveMQConnectionFactory lConnectionFactory = new ActiveMQConnectionFactory(
				"failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false");
		this.mJMSTemplate.setConnectionFactory(lConnectionFactory);
		this.mJMSTemplate.setDefaultDestinationName("stock.topic");
		this.mJMSTemplate.setDeliveryPersistent(false);
		this.mJMSTemplate.setPubSubDomain(true);
		this.mJMSTemplate.setSessionTransacted(false);
	}

	@Override
	public void run() {
		System.out.println("JMS datasource started.");

		mRandom = new SecureRandom(new SecureRandom().generateSeed(20));
		System.arraycopy(mSTOCKS_ORIG, 0, mSTOCKS, 0, mSTOCKS.length);
		try {
			while (true) {
				mChangeStock();
				Thread.sleep(500L);
			}
		} catch (InterruptedException lEx) {
			lEx.printStackTrace();
		}
	}

	private void mChangeStock() {
		// pick the stock to update
		int lStockIndex = mRandom.nextInt(STOCK_ROW_COUNT);

		float lOldVal = (Float) mSTOCKS[(lStockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX];

		// calculate the percent change (and round it)
		// the maximum fluctuation is a percentage of the current value...
		float lMaxFluctuate = FLUCTUATION_PERCENT * lOldVal;

		// ... take a random percentage of that maximum...
		float lActualFluctuate = mRandom.nextFloat() * lMaxFluctuate;

		// now round it to two decimal places
		lActualFluctuate = 100 * lActualFluctuate;
		long lDelta = Math.round(lActualFluctuate);

		// minimum delta change should never go below zero
		if (lDelta < 1) {
			lDelta = 1;
		}

		// randomly switch positive / negative delta
		if (mRandom.nextFloat() < 0.5) {
			lDelta = -lDelta;
		}

		lDelta += (long) (lOldVal * 100);

		// if a stock ever falls too low, reset it to original value
		if ((lDelta < MIN_VALUE_PENNIES) || (lDelta > MAX_VALUE_PENNIES)) {
			mSTOCKS[(lStockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX] = mSTOCKS_ORIG[(lStockIndex * STOCK_COLUMN_COUNT)
					+ PRICE_COLUMN_INDEX];
		} else {
			float lNewVal = (float) lDelta / 100;

			mSTOCKS[(lStockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX] = lNewVal;
		}
		mSendStock(lStockIndex);
	}

	private void mSendStock(int aStockIndex) {
		String lBody = mSTOCKS[(aStockIndex * STOCK_COLUMN_COUNT)] + ":"
				+ mSTOCKS[(aStockIndex * STOCK_COLUMN_COUNT) + TICKER_COLUMN_INDEX] + ":"
				+ mDisplayFormat.format(mSTOCKS[(aStockIndex * STOCK_COLUMN_COUNT) + PRICE_COLUMN_INDEX]);

		// try {
		// TextMessage message = _session.createTextMessage(body);
		// message.setStringProperty("symbol", (String) _STOCKS[(stockIndex
		// * STOCK_COLUMN_COUNT)
		// + TICKER_COLUMN_INDEX]);
		// _messageProducer.send(message, DeliveryMode.NON_PERSISTENT,
		// Message.DEFAULT_PRIORITY, 0L);
		mJMSTemplate.convertAndSend(lBody);
		// } catch (JMSException e) {
		// _logger.error("Failure to send message: " + body);
		// }
	}
}
