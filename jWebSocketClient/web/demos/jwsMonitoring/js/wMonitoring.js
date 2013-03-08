//	****************************************************************************
//	jWebSocket Hello World (uses jWebSocket Client and Server)
//	(C) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
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
//	****************************************************************************

/*
 * @author Orlando Miranda Gómez, vbarzana
 */
$.widget("jws.monitoring", {
	_init: function( ) {
		this.NS = jws.NS_BASE + ".plugins.monitoring";
		this.mMemGauge = bindows.loadGaugeIntoDiv("gauges/g_memoryRam_memorySwap.xml", "memDiv");
		this.mCPUGauge = bindows.loadGaugeIntoDiv("gauges/g_cpu.xml", "cpuDiv");
		this.mHDDGauge = bindows.loadGaugeIntoDiv("gauges/g_hdd.xml", "hddDiv");

		w.monitoring = this;
		w.monitoring.doWebSocketConnection( );
	},
	doWebSocketConnection: function( ) {
		// Each widget uses the same authentication mechanism, please refer
		// to the public widget ../../res/js/widgets/wAuth.js
		var lCallbacks = {
			OnOpen: function() {
				// Registering to the monitoring stream
				var lRegisterToken = {
					ns: w.monitoring.NS,
					type: "register",
					interest: "computerInfo"
				};
				// Sending the register token
				mWSC.sendToken(lRegisterToken);
			},
			OnClose: function() {
				w.monitoring.resetGauges();
			},
			OnMessage: function(aEvent, aToken) {
				if (w.monitoring.NS === aToken.ns && "computerInfo" === aToken.type) {
					w.monitoring.updateGauge(aToken);
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
		if (aToken.totalHddSpace.substr(-3) != aToken.usedHddSpace.substr(-3)) {
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