<%--
  User: jorgefrancoleza
  Date: 24/02/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <r:require modules="vertx,grooscript,kimbo"/>
    <link rel="stylesheet" type="text/css" href="../css/style.css">
    <title>Title</title>
    <r:layoutResources/>
</head>
<body>

<p>Try Page.</p>

<div id="list"></div>

<label>Text:</label>
<input id="item" type="text" value=""/>
<button type="button" onclick="addToList($('#item').val());">Add</button>

<grooscript:code>
    def list = []

    def addToList(item) {
        list << item
        $('#list').html(getList())
    }

    def getList() {
        def out = '<ul>'
        list.each {
            out += '<li>'+it+'</li>'
        }
        out += '</ul>'
    }
</grooscript:code>

<grooscript:reloadPage/>

<r:script>
    function listenTesting() {
        grooscriptEventBus.registerHandler('testingIncoming', function(message) {
            console.log('Got message on testingIncoming: ' + JSON.stringify(message));
        });
    }
    function sendTestingMessage() {
        grooscriptEventBus.send('testing',{ message: 'hello'}, function(message) {
            console.log('Done send testing message.');
            console.log('Recieved:'+JSON.stringify(message));
        })
    }
</r:script>

<button type="button" onclick="listenTesting();">Listen Testing</button>
<button type="button" onclick="sendTestingMessage();">Send Testing Message</button>

<r:layoutResources/>

<r:external uri='/js/Message.js'/>

</body>
</html>