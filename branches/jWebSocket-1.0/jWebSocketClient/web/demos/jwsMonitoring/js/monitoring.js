/**
 * This class allows the connection to the server and display the customer information
 * @author orlando
 */

$(document).ready(function(){ 
    $.jws.open("ws://10.208.7.30:8787/monitoringc11/");
    $.jws.bind("open", function(){
        $.jws.submit("monitoringPlugin.pcinfo", "register");
        console.log("Connection Opened");
    }); 
    
    $.jws.bind("close", function(){
        resetGauges();
        console.log("Connection Stoped");
    });
});

//To load the gauges in the divs        
var memGauge = bindows.loadGaugeIntoDiv("gauges/g_memoryRam_memorySwap.xml","memDiv");
var cpuGauge = bindows.loadGaugeIntoDiv("gauges/g_cpu.xml","cpuDiv");
var hddGauge = bindows.loadGaugeIntoDiv("gauges/g_hdd.xml","hddDiv");

// Dynamically update the gauge at runtime
function updateGauge() {
    $.jws.bind("monitoringPlugin.pcinfo:information", function(evt, aToken){
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
        if(aToken.totalHddSpace.substr(-3) != aToken.usedHddSpace.substr(-3))
		{
            IUsed = parseInt(aToken.usedHddSpace) / 1000;	
		}
		else IUsed = parseInt(aToken.usedHddSpace);
        hddGauge.label1.setText(aToken.totalHddSpace);
        hddGauge.label2.setText(aToken.usedHddSpace);
        hddGauge.needle.setValue(IUsed);
        hddGauge.maxValue.setEndValue(parseInt(aToken.totalHddSpace));
    });
}
updateGauge();

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
