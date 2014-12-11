//	---------------------------------------------------------------------------
//	jWebSocket - Load Balancer CPU updater (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import java.util.Timer;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 * Automatically send CPU statistics updates to the Load Balancer plug-in.
 *
 * @author Rolando Santamaria Maso
 */
public class JWSLoadBalancerCpuUpdater {

	private JWSEndPoint mEP;
	private String mTargetId;
	private boolean mIsServiceEndPoint = false;
	private Long mTimeInterval = new Long(1500);

	/**
	 * Default time interval is 1500 ms.
	 *
	 * @param aEP The jWebSocket endpoint instance
	 * @param aTargetId The target gateway identifier
	 */
	public JWSLoadBalancerCpuUpdater(JWSEndPoint aEP, String aTargetId) {
		mEP = aEP;
		mTargetId = aTargetId;
	}

	/**
	 * Set the CPU updater time interval.
	 *
	 * @param aTimeInterval
	 */
	public void setTimeInterval(Long aTimeInterval) {
		mTimeInterval = aTimeInterval;
	}

	/**
	 * Get the CPU updater time interval.
	 *
	 * @return
	 */
	public Long getTimeInterval() {
		return mTimeInterval;
	}

	/**
	 * Start the CPU statistics sending to the Load Balancer.
	 */
	public void start() {
		mIsServiceEndPoint = true;

		final Timer lTimer = Tools.getTimer();
		JWSTimerTask lTask = new JWSTimerTask() {

			@Override
			protected void runTask() {
				try {
					if (mEP.isOpen() && mIsServiceEndPoint) {
						mEP.sendCpuUsageToLoadBalancer(mTargetId, Tools.getCpuUsage());
						lTimer.schedule(this, mTimeInterval);
					}
				} catch (Exception lEx) {

				}
			}
		};

		lTimer.schedule(lTask, mTimeInterval);
	}

	/**
	 * Automatically start the CPU statistics sending to the Load Balancer once
	 * the endpoint is successfully registered as a service endpoint.
	 */
	public void autoStart() {
		mEP.addResponseListener("org.jwebsocket.plugins.loadbalancer",
				"registerServiceEndPoint", new JWSMessageListener(mEP) {

					@Override
					public void processToken(String aSourceId, Token aToken) {
						if (aToken.getCode() == 0) {
							start();
						}
					}
				});
		mEP.addResponseListener("org.jwebsocket.plugins.system",
				"logout", new JWSMessageListener(mEP) {

					@Override
					public void processToken(String aSourceId, Token aToken) {
						if (aToken.getCode() == 0) {
							mIsServiceEndPoint = false;
						}
					}
				});
	}

}
