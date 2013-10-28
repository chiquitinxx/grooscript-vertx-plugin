<%--
  User: jorgefrancoleza
  Date: 24/02/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <r:require modules="vertx, jquery"/>
    <r:external uri="/css/style.css"/>
    <title>Title</title>
    <r:layoutResources/>
</head>
<body>

<p>Try Page.</p>

<div id="list"></div>

<label>Text:</label>
<input id="item" type="text" value=""/>
<button type="button" onclick="addToList($('#item').val());">Add</button>

<div id="gotIncoming"></div>

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

<grooscript:onServerEvent name='testingIncoming'>
    console.log 'Got message in testingIncoming to json!' + JSON.stringify(message)
    console.log 'Got message in testingIncoming coming!' + message.toString()
    console.log 'Got js!' + GrooscriptGrails.toJavascript(message)
    $('#gotIncoming').text($('#gotIncoming').text() + '.')
</grooscript:onServerEvent>
<grooscript:onServerEvent name='testingIncoming'>
    console.log 'One more!'
</grooscript:onServerEvent>

<r:script>
    function sendTestingMessage() {
        /*grooscriptEventBus.send('testing',{ message: 'hello'}, function(message) {
            console.log('Done send testing message.');
            console.log('Recieved:'+JSON.stringify(message));
        })*/
        grooscriptEventBus.send('testing',{ message: 'hello'});
        /*setTimeout(
                function()
                {
                    alert('hola!');
                }, 2000);*/
    }
</r:script>

<grooscript:onVertxStarted>
    GrooscriptGrails.sendServerMessage('testing',[hola: 'hola', one: 1, list: [1,2], map: [three : 3, four: 'four']])
</grooscript:onVertxStarted>

<button type="button" onclick="sendTestingMessage();">Send Testing Message</button>

<r:layoutResources/>

<r:external uri='/js/Message.js'/>

</body>
</html>