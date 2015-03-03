//	---------------------------------------------------------------------------
//	jWebSocket AMQBrokerService (Community Edition, CE)
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
package org.jwebsocket.util;

import org.apache.activemq.broker.BrokerService;

/**
 * Wraps the ActiveMQ BrokerService allowing developers to easily set the temporary storage limit.
 * See https://issues.apache.org/jira/browse/AMQ-4888
 *
 * @author Rolando Santamaria Maso
 */
public class AMQBrokerService extends BrokerService {

	private long mTempStorageLimit = 1024 * 100;

	/**
	 * Set the broker temporary storage usage limit.
	 *
	 * @param aLimit
	 */
	public void setTempStorageLimit(long aLimit) {
		getSystemUsage().getTempUsage().setLimit(mTempStorageLimit);
	}

	/**
	 * Get the broker temporary storage usage limit.
	 *
	 * @return
	 */
	public long getTempStorageLimit() {
		return getSystemUsage().getTempUsage().getLimit();
	}

}
