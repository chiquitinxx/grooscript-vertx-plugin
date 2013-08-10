<%--
  User: jorgefrancoleza
  Date: 24/02/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <script src="../js/sockjs.js"></script>
    <script src='../js/vertxbus.js'></script>
    <script src='../js/grooscript.js'></script>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.js"></script>
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

<script>

var eb = new vertx.EventBus('http://localhost:8085/eventbus')

eb.onopen = function() {

    console.log('EventBus started');
    eb.registerHandler('reloadPage', function(message) {

        console.log('Got message on reloadPage: ' + JSON.stringify(message));
        if (message.reload == true) {
            window.location.reload();
        }

    });

    //Lets get the list of items
    //eb.send('model', {action: 'list', model:'model.Movie'}, function(message) {

}
</script>

<script src='../js/Message.js'></script>

<r:layoutResources/>
</body>
</html>