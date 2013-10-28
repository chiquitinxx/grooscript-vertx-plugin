<%--
  User: jorgefrancoleza
  Date: 17/09/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Vert.x events</title>
    <r:require module="jquery"/>
    <r:layoutResources/>
</head>
<body>
    <div id='points'></div>
    <grooscript:onVertxStarted>
        console.log 'Started!'
    </grooscript:onVertxStarted>
    <grooscript:onServerEvent name="testingIncoming">
        $('#points').text($('#points').text() + '.')
    </grooscript:onServerEvent>
    <grooscript:onServerEvent name="testingIncoming">
        console.log 'Message testingIncoming!'
        console.log 'Message recieved: ' + message
    </grooscript:onServerEvent>
    <grooscript:onServerEvent name="reloadPage">
        console.log 'Message reloadPage!'
        console.log 'Message recieved: ' + message
    </grooscript:onServerEvent>
    <grooscript:onVertxStarted>
        console.log 'Going to send message'
        grooscriptEventBus.send('testing',[ message: 'hello'])
    </grooscript:onVertxStarted>
    <r:layoutResources/>
</body>
</html>