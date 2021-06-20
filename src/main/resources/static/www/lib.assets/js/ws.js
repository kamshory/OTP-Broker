var hostName = location.hostname; 
var portNumber = location.port;
var scheme = location.protocol;
var wsURL = createBaseURL(scheme, hostName, portNumber)+"/websocket/manager";
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
        var receivedRaw = evt.data;
        var receivedJSON = JSON.parse(receivedRaw);
        if(receivedJSON.command == "broadcast-message")
        {
          for(var i in receivedJSON.data)
          {
            showNotif(receivedJSON.data[i].message);
          }
        }
        if(receivedJSON.command == "server-info")
        {
            updateServerInfo(receivedJSON);
            updateUSBColor(receivedJSON);
        }
        if(typeof handleIncommingMessage != 'undefined')
        {
            handleIncommingMessage(receivedRaw);
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

function createUSB(color)
{
    var html = 

    '    <svg\r\n'+
    '      xmlns:svg="http://www.w3.org/2000/svg"\r\n'+
    '      xmlns="http://www.w3.org/2000/svg"\r\n'+
    '      version="1.0"\r\n'+
    '      width="47.524799"\r\n'+
    '      height="22.8092"\r\n'+
    '      viewBox="0 0 475.248 228.092"\r\n'+
    '      id="Layer_1"\r\n'+
    '      xml:space="preserve"><defs\r\n'+
    '      id="defs1337" />\r\n'+
    '    <path\r\n'+
    '      d="M 462.836,114.054 L 412.799,85.158 L 412.799,105.771 L 157.046,105.771 L 206.844,53.159 C 211.082,49.762 216.627,47.379 222.331,47.247 C 245.406,47.247 259.109,47.241 264.153,47.231 C 267.572,56.972 276.756,64.003 287.674,64.003 C 301.486,64.003 312.695,52.795 312.695,38.978 C 312.695,25.155 301.487,13.951 287.674,13.951 C 276.756,13.951 267.572,20.978 264.153,30.711 L 222.821,30.704 C 211.619,30.704 199.881,36.85 192.41,44.055 C 192.614,43.841 192.826,43.613 192.398,44.059 C 192.24,44.237 139.564,99.873 139.564,99.873 C 135.335,103.265 129.793,105.633 124.093,105.769 L 95.161,105.769 C 91.326,86.656 74.448,72.256 54.202,72.256 C 31.119,72.256 12.408,90.967 12.408,114.043 C 12.408,137.126 31.119,155.838 54.202,155.838 C 74.452,155.838 91.33,141.426 95.165,122.297 L 123.59,122.297 C 123.663,122.297 123.736,122.301 123.81,122.297 L 186.681,122.297 C 192.37,122.442 197.905,124.813 202.13,128.209 C 202.13,128.209 254.794,183.841 254.957,184.021 C 255.379,184.468 255.169,184.235 254.961,184.025 C 262.432,191.229 274.175,197.371 285.379,197.371 L 325.211,197.362 L 325.211,214.139 L 375.261,214.139 L 375.261,164.094 L 325.211,164.094 L 325.211,180.849 C 325.211,180.849 314.72,180.83 284.891,180.83 C 279.186,180.699 273.635,178.319 269.399,174.922 L 219.59,122.3 L 412.799,122.3 L 412.799,142.946 L 462.836,114.054 z "\r\n'+
    '      id="path1334" stoke="'+color+'" fill="'+color+'" />\r\n'+
    '    </svg>';
    return html;
}

function createUSBSymbol(color)
{
    $('.usb-device-symbol').html('<a href="modem.html">'+createUSB(color)+'</a>');
}

function setUSBColor(color)
{
    $('.usb-device-symbol svg path').attr('fill', color);
    $('.usb-device-symbol svg path').attr('stroke', color);
}

function setModemConnected(value)
{
    var key = 'otp_modem_connected';
    window.localStorage.setItem(key, (value)?'1':'0');
}
function getModemConnected()
{
    var key = 'otp_modem_connected';
    var value = window.localStorage.getItem(key) || '';
    return value == '1';
}
function setWSConnected(value)
{
    var key = 'otp_ws_connected';
    window.localStorage.setItem(key, (value)?'1':'0');
    console.log('otp_ws_connected', value)
}
function getWSConnected()
{
    var key = 'otp_ws_connected';
    var value = window.localStorage.getItem(key) || '';
    return value == '1';
}
function setAMQPConnected(value)
{
    var key = 'otp_amqp_connected';
    window.localStorage.setItem(key, (value)?'1':'0');
}
function getAMQPConnected()
{
    var key = 'otp_amqp_connected';
    var value = window.localStorage.getItem(key) || '';
    return value == '1';
}
function setWSEnable(value)
{
    var key = 'otp_ws_enable';
    window.localStorage.setItem(key, (value)?'1':'0');
    console.log('otp_ws_enable', value)
}
function getWSEnable()
{
    var key = 'otp_ws_enable';
    var value = window.localStorage.getItem(key) || '';
    return value == '1';
}
function setAMQPEnable(value)
{
    var key = 'otp_amqp_enable';
    window.localStorage.setItem(key, (value)?'1':'0');
}
function getAMQPEnable()
{
    var key = 'otp_amqp_enable';
    var value = window.localStorage.getItem(key) || '';
    return value == '1';
}
function setHTTPEnable(value)
{
    var key = 'otp_http_enable';
    window.localStorage.setItem(key, (value)?'1':'0');
}
function getHTTPEnable()
{
    var key = 'otp_http_enable';
    var value = window.localStorage.getItem(key) || '';
    return value == '1';
}
function updateUSBColor(receivedJSON)
{
    var data = receivedJSON.data;
    for(var i in data)
    {
        var item = data[i];
        if(item.name == 'modem_connected')
        {
            createUSBSymbol(item.value?'#328C54':'#D83A56');
        }
    }
}

function updateServerInfo(receivedJSON)
{
    var data = receivedJSON.data;
    for(var i in data)
    {
        var item = data[i];
        if(item.name == 'http_enable')
        {
            setHTTPEnable(item.value);
        }
        if(item.name == 'modem_connected')
        {
            setModemConnected(item.value);
        }
        if(item.name == 'ws_enable')
        {
            setWSEnable(item.value);
        }
        if(item.name == 'ws_connected')
        {
            setWSConnected(item.value);
        }
        if(item.name == 'amqp_enable')
        {
            setAMQPEnable(item.value);
        }
        if(item.name == 'amqp_connected')
        {
            setAMQPConnected(item.value);
        }
        if(item.name == 'feeder')
        {
            setFeeder(item.feeder);
        }
    }
    
}

function setFeeder(feeder)
{
    var key = 'otp_feeder';
    var value = (typeof feeder == 'object')?(JSON.stringify(feeder)):'{}';
    window.localStorage.setItem(key, value);

}
function getFeeder()
{
    var key = 'otp_feeder';
    var raw = window.localStorage.getItem(key) || '{}';
    var value = {};
    try
    {
        value = JSON.parse(raw);
    }
    catch(e)
    {
        value = {};
    }
    return value;
}
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

function updateDashboard()
    {
    var isModemConnected = getModemConnected();
    var isWSEmable = getWSEnable();
    var isWSConnected = getWSConnected();
    var isAMQPEnable = getAMQPEnable();
    var isAMQPConnected = getAMQPConnected();
    var isHTTPEmable = getHTTPEnable();

    $('.service-modem').removeClass('connected');
    $('.service-modem').removeClass('disconnected');
    $('.service-modem').addClass('enable');
    $('.service-modem').addClass(isModemConnected?'connected':'disconnected');

    $('.service-http').removeClass('connected');
    $('.service-http').removeClass('disconnected');
    $('.service-http').removeClass('enable');
    $('.service-http').removeClass('disable');
    $('.service-http').addClass(isHTTPEmable?'enable':'disable');
    $('.service-http').addClass(isHTTPEmable?'connected':'disconnected');

    $('.service-ws').removeClass('enable');
    $('.service-ws').removeClass('disable');
    $('.service-ws').removeClass('connected');
    $('.service-ws').removeClass('disconnected');
    $('.service-ws').addClass(isWSEmable?'enable':'disable');
    $('.service-ws').addClass(isWSConnected?'connected':'disconnected');

    $('.service-amqp').removeClass('enable');
    $('.service-amqp').removeClass('disable');
    $('.service-amqp').removeClass('connected');
    $('.service-amqp').removeClass('disconnected');
    $('.service-amqp').addClass(isAMQPEnable?'enable':'disable');
    $('.service-amqp').addClass(isAMQPConnected?'connected':'disconnected');
}

$(document).ready(function(e){
    createUSBSymbol();
    $('body').append('<div class="notification-container"></div>');
    $(document).on('click', '.notification-close a', function(e2){
        $(this).closest('.notification-item').remove();
    });
    initWebSocket();

});