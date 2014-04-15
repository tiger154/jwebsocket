//	---------------------------------------------------------------------------
//	jWebSocket Monitoring Demo (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

/*
 * @author Orlando Miranda Gómez, vbarzana
 */
$.widget("jws.monitoring", {
	_init: function( ) {
		this.NS = jws.NS_BASE + ".plugins.monitoring";
		this.NS_STREAMING = jws.NS_BASE + ".plugins.streaming";
		this.TT_REGISTER = "register";
		this.TT_UNREGISTER = "unregister";
		this.TT_INFO = "computerInfo";
		this.mMemGauge = bindows.loadGaugeIntoDiv("js/bindows/gauges/g_memoryRam_memorySwap.xml", "memDiv");
		this.mCPUGauge = bindows.loadGaugeIntoDiv("js/bindows/gauges/g_cpu.xml", "cpuDiv");
		this.mHDDGauge = bindows.loadGaugeIntoDiv("js/bindows/gauges/g_hdd.xml", "hddDiv");
		this.mClockGauge = bindows.loadGaugeIntoDiv("js/bindows/gauges/g_clock.xml", "clockDiv");
		w.monitoring = this;
		w.monitoring.doWebSocketConnection( );
	},
	doWebSocketConnection: function( ) {
		// Each widget uses the same authentication mechanism, please refer
		// to the public widget ../../res/js/widgets/wAuth.js
		var lCallbacks = {
			OnOpen: function() {

			},
			OnClose: function() {
				w.monitoring.resetGauges();
			},
			OnLogon: function() {
				if (mWSC && mWSC.streaming) {
					mWSC.streaming.registerStream("timeStream");
				}
				// Registering to the monitoring stream
				var lRegisterToken = {
					ns: w.monitoring.NS,
					type: w.monitoring.TT_REGISTER,
					interest: w.monitoring.TT_INFO
				};
				// Sending the register token
				mWSC.sendToken(lRegisterToken);
			},
			OnLogoff: function() {
				mWSC.streaming.unregisterStream("timeStream");
				var lUnregisterToken = {
					ns: w.monitoring.NS,
					type: w.monitoring.TT_UNREGISTER,
					interest: w.monitoring.TT_INFO
				};
				// Sending the register token
				mWSC.sendToken(lUnregisterToken);
				w.monitoring.resetGauges();
			},
			OnWelcome: function(aToken) {
				if (aToken.username !== "anonymous") {
					this.OnLogon(aToken);
				}
			},
			OnMessage: function(aEvent, aToken) {
				if (w.monitoring.NS === aToken.ns &&
						w.monitoring.TT_INFO === aToken.type) {
					w.monitoring.updateGauge(aToken);
				}

				if (w.monitoring.NS_STREAMING === aToken.ns) {
					if (aToken.type === "event" && aToken.name === "stream" && aToken.streamID === "timeStream") {
						w.monitoring.updateTime(aToken);
					}
				}

				var lDate = "";
				if (aToken.date_val) {
					lDate = jws.tools.ISO2Date(aToken.date_val);
				}
				log("<font style='color:#888'>jWebSocket '" + aToken.type +
						"' token received, full message: '" + aEvent.data + "' " +
						lDate + "</font>");
			}
		};
		// this widget will be accessible from the global variable w.auth
		$("#demo_box").auth(lCallbacks);
	},
	// Dynamically update the gauge at runtime
	updateGauge: function(aToken) {
		//cpu
		var IValue = parseInt(aToken.consumeCPU);
		w.monitoring.mCPUGauge.needle.setValue(IValue);
		w.monitoring.mCPUGauge.label.setText(aToken.consumeCPU);

		//memory
		w.monitoring.mMemGauge.needle1.setValue(aToken.usedMemPercent);
		w.monitoring.mMemGauge.needle2.setValue(aToken.swapPercent);
		w.monitoring.mMemGauge.label2.setText(aToken.usedMemPercent.toFixed(1) + "%");
		w.monitoring.mMemGauge.label4.setText(aToken.swapPercent.toFixed(1) + "%");

		//hdd 
		var IUsed;
		if (aToken.totalHddSpace.substr(-3) !== aToken.usedHddSpace.substr(-3)) {
			IUsed = parseInt(aToken.usedHddSpace) / 1000;
		}
		else {
			IUsed = parseInt(aToken.usedHddSpace);
		}
		w.monitoring.mHDDGauge.label1.setText(aToken.totalHddSpace);
		w.monitoring.mHDDGauge.label2.setText(aToken.usedHddSpace);
		w.monitoring.mHDDGauge.needle.setValue(IUsed);
		w.monitoring.mHDDGauge.maxValue.setEndValue(parseInt(aToken.totalHddSpace));
	},
	updateTime: function(aToken) {
		if (aToken) {
			w.monitoring.mClockGauge.needleMinutes.setValue(aToken.minutes);
			w.monitoring.mClockGauge.needleSeconds.setValue(aToken.seconds);
			w.monitoring.mClockGauge.needleHours.setValue(aToken.hours);
		}
	},
	//Reset gauges when the server is disconnect
	resetGauges: function() {
		w.monitoring.mCPUGauge.needle.setValue("0");
		w.monitoring.mCPUGauge.label.setText("0");
		w.monitoring.mMemGauge.needle1.setValue(0);
		w.monitoring.mMemGauge.needle2.setValue(0);
		w.monitoring.mMemGauge.label2.setText("0");
		w.monitoring.mMemGauge.label4.setText("0");
		w.monitoring.mHDDGauge.label1.setText("0");
		w.monitoring.mHDDGauge.label2.setText("0");
		w.monitoring.mHDDGauge.needle.setValue(0);
		w.monitoring.mHDDGauge.maxValue.setEndValue(100);
	}
});