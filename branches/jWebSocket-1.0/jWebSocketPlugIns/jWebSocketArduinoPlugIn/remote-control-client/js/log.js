var eLog = null;

function log(aString ) {  
  eLog.prepend($("<label>"+aString+"</label>" + "<br>").fadeIn(400) ); 
}

function clearLog() {  
eLog.find('label').each(function(i, element){    
 $(element).fadeOut(400, function() { $(this).remove() });    
});
eLog.html('');
  
}
