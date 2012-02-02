
$(document).ready(function(){
    $.jws.open();
    $.jws.bind('open', function(evt, aToken){        
        $('#board').ball();
        $('#scenario_body').stage(); 
        $('#board').player();
        $('#online').connected();
        $('#demo_box_header').user();
        $('#demo_box_scenario').menu();
        $('#demo_box_scenario').chat();
        $('#scenario_body').ranking();
    });
    $.jws.bind('close', function(evt, aToken){
        jAlert('connection ended', 'Ping Pong Game');
    }); 
   
    });

