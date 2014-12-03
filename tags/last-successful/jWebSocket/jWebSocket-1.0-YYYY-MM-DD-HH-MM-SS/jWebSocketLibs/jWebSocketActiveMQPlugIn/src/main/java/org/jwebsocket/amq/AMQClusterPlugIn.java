//	---------------------------------------------------------------------------
//	jWebSocket - AMQClusterPlugIn (Community Edition, CE)
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
package org.jwebsocket.amq;

import com.mongodb.Mongo;
import java.util.List;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class AMQClusterPlugIn implements BrokerPlugin {

	private List<String> mTargetDestinations;
	private Mongo mMongo;
	private String mUsername, mPassword;
	
	/**
	 *
	 * @param broker
	 * @return
	 * @throws Exception
	 */
	@Override
	public Broker installPlugin(Broker broker) throws Exception {
		return new AMQClusterFilter(broker, mTargetDestinations, mMongo, mUsername, mPassword);
	}
    
	/**
	 *
	 * @return
	 */
	public List<String> getTargetDestinations() {
		return mTargetDestinations;
	}

	/**
	 *
	 * @param aTargetDestinations
	 */
	public void setTargetDestinations(List<String> aTargetDestinations) {
		mTargetDestinations = aTargetDestinations;
	}

	/**
	 *
	 * @return
	 */
	public Mongo getMongo() {
		return mMongo;
	}

	/**
	 *
	 * @param aMongo
	 */
	public void setMongo(Mongo aMongo) {
		this.mMongo = aMongo;
	}

	/**
	 *
	 * @return
	 */
	public String getUsername() {
		return mUsername;
	}

	/**
	 *
	 * @param aUsername
	 */
	public void setUsername(String aUsername) {
		this.mUsername = aUsername;
	}

	/**
	 *
	 * @return
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 *
	 * @param aPassword
	 */
	public void setPassword(String aPassword) {
		this.mPassword = aPassword;
	}
}
