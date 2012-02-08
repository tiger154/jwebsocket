/**
 * This class allows the connection to the server and display the customer information
 * @author orlandon
 */

//To load the gauges in the divs
memGauge = bindows.loadGaugeIntoDiv("gauges/g_memoryRam_memorySwap.xml","memDiv");
cpuGauge = bindows.loadGaugeIntoDiv("gauges/g_cpu.xml","cpuDiv");
hddGauge = bindows.loadGaugeIntoDiv("gauges/g_hdd.xml","hddDiv");

// Dynamically update the gauge at runtime
function updateGauge() {
	$.jws.bind("monitoringPlugin.pcinfo:computerInfo", function(evt, aToken){
		//cpu
		var IValue = parseInt(aToken.consumeCPU);
		cpuGauge.needle.setValue(IValue);
		cpuGauge.label.setText(aToken.consumeCPU);
                    
		//memory
		memGauge.needle1.setValue(aToken.usedMemPercent);
		memGauge.needle2.setValue(aToken.swapPercent);
		memGauge.label2.setText(aToken.usedMemPercent.toFixed(1) + "%");
		memGauge.label4.setText(aToken.swapPercent.toFixed(1) + "%" );
                    
		//hdd 
		var IUsed;
		if(aToken.totalHddSpace.substr(-3) != aToken.usedHddSpace.substr(-3)) {
			IUsed = parseInt(aToken.usedHddSpace) / 1000;	
		}
		else {
			IUsed = parseInt(aToken.usedHddSpace);
		}
		hddGauge.label1.setText(aToken.totalHddSpace);
		hddGauge.label2.setText(aToken.usedHddSpace);
		hddGauge.needle.setValue(IUsed);
		hddGauge.maxValue.setEndValue(parseInt(aToken.totalHddSpace));
	});
}

//Reset gauges when the server is disconnect
function resetGauges(){
	cpuGauge.needle.setValue("0");
	cpuGauge.label.setText("0");
	memGauge.needle1.setValue(0);
	memGauge.needle2.setValue(0);
	memGauge.label2.setText("0");
	memGauge.label4.setText("0"); 
	hddGauge.label1.setText("0");
	hddGauge.label2.setText("0");
	hddGauge.needle.setValue(0);
	hddGauge.maxValue.setEndValue(100);
}