var hostName = location.hostname; 
var portNumber = location.port;
var scheme = location.protocol;
var wsURL = createBaseURL(scheme, hostName, portNumber)+"/websocket";
var ws = null;
var wsConnected = false;
var connetionInterval = setTimeout('', 1000);

function createBaseURL(protocol, host, port){
    var url = "";
    if(protocol.indexOf("https") != -1)
    {
        if(typeof port == 'undefined' || port == '' || port == 443)
        {
            url = "wss://"+host; 
        }
        else
        {
            url = "wss://"+host+":"+port;
        }
    }
    else
    {
        if(typeof port == 'undefined' || port == '' || port == 80)
        {
            url = "ws://"+host; 
        }
        else
        {
            url = "ws://"+host+":"+port;
        }
    }
    return url;
}

function initWebSocket()
{
    if ("WebSocket" in window) 
    {
        connectWebSocket();
    } 
    else 
    {
        console.log("WebSocket is not supported by your browser!");
    }
}
function connectWebSocket()
{
    console.log("Connecting  to "+wsURL);
    wsConnected = false;
    ws = new WebSocket(wsURL);
        
    ws.onopen = function() {	   
        wsConnected = true;
        console.log("Connected");
    };
    
    ws.onmessage = function (evt) { 
        var received_msg = evt.data;
        var received = JSON.parse(received_msg);
        if(received.command == "broadcast-message")
        {
          for(var i in received.data)
          {
            showNotif(received.data[i].message);
          }
        }
        if(typeof handleIncommingMessage != 'undefined')
        {
            handleIncommingMessage(received_msg);
        }
    };
    
    ws.onclose = function() { 	   
        wsConnected = false;
        console.log("Connection is closed..."); 
        clearTimeout(connectWebSocket);
        connetionInterval = setTimeout(function(){
            connectWebSocket();
            if(wsConnected)
            {
                clearTimeout(connectWebSocket);
            }
        }, 2000);
    };
    ws.onError = function()
    {
        console.log("An error. occured.."); 
        wsConnected = false;
        clearTimeout(connectWebSocket);
        connetionInterval = setTimeout(function(){
            connectWebSocket();
            if(wsConnected)
            {
                clearTimeout(connectWebSocket);
            }
        }, 2000);
    }
}
$(document).ready(function(e){
    $('body').append('<div class="notification-container"></div>');
    $(document).on('click', '.notification-close a', function(e2){
        $(this).closest('.notification-item').remove();
    });
    initWebSocket();

});
function showNotif(message)
{
    var html = '<div class="notification-item">\r\n'+
        '<div class="notification-wrapper">\r\n'+
        '  <div class="notification-close">\r\n'+
        '    <a href="javascript:;">X</a>\r\n'+
        '  </div>\r\n'+
        '  <div class="notification-message">\r\n'+
        '    '+message+'\r\n'+
        '  </div>\r\n'+
        '</div>\r\n'+
        '</div>\r\n';
    var obj = $(html);  
    $('.notification-container').append(obj);
    setTimeout(function(){
        obj.remove()
    }, 6000);
}