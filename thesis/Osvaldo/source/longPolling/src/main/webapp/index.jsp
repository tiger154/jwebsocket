<%-- 
    Document   : index
    Created on : Sep 29, 2011, 5:01:06 PM
    Author     : osvaldo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript" src="jWebSocket.js"></script>
        <script type="text/javascript" src="websocketXHR.js"></script>
        <script type="text/javascript" src="app.js"></script>
</head>
    
    
    <body>
        <h1>jWebSocket long polling demo</h1>
        
        <input type="button" value="Start"
               id="btnStart"></input>
        
        <input type="button" value="close connection"
	id="btnClosing"></input>
        <input type="button" value="clear"
	id="btnClear"></input>
		message repeat
		<select id="repeatMessages">
			<option value="1">1</option>
			<option value="2">2</option>
			<option value="5">5</option>
			<option value="10">10</option>
		</select>
        <br/>
        <div id="log" style="font:12px sans-serif;width: 900px; height: 300px; background-color: #e3e3e3; overflow:scroll;"></div>
        
        
       <input type="text" id="textToSend"></input>
       <input type="button" id="btnSend" value="send" ></input>
       
            
        </div>
    </body>
</html>
