
$(document).ready(function() { 
    init();    
  
    $('#ledblue').click( function(){
        sendCommand(49)
    });       
    $('#ledred').click( function(){
        sendCommand(50)
    });       
    $('#ledgreen').click( function(){
        sendCommand(51)
    });       
    $('#ledyellow').click( function(){
        sendCommand(52)
    });       
});

function keyCommand(key){
    switch(key){
        case 37:
            sendCommand(37)
            break;
             
        case 38:
            sendCommand(38)
            break;
             
        case 39:
            sendCommand(39)
            break;
             
        case 40:
            sendCommand(40)
            break;       
    }    
}

function changeledsStatus(blue, red, green, yellow){
    if(blue){
        $('#ledblue').removeClass('off').addClass('on');
    }
    else{
        $('#ledblue').removeClass('on').addClass('off');
    }
    if(red){
        $('#ledred').removeClass('off').addClass('on');
    }
    else{
        $('#ledred').removeClass('on').addClass('off');
    }
    if(green){
        $('#ledgreen').removeClass('off').addClass('on');
    }
    else{
        $('#ledgreen').removeClass('on').addClass('off');
    }
    if(yellow){
        $('#ledyellow').removeClass('off').addClass('on');
    }
    else{
        $('#ledyellow').removeClass('on').addClass('off');
    }
}