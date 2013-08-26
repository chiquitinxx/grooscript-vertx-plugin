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

<r:script>

var eb = new vertx.EventBus('http://localhost:8085/eventbus')

eb.onopen = function() {
    console.log('EventBus started');
}


</r:script>

<r:script>
    function addOne() {
        eb.registerHandler('one', function(message) {
            console.log('Got message on one: ' + JSON.stringify(message));
        });
        eb.registerHandler('reloadPage', function(message) {

            console.log('Got message on reloadPage: ' + JSON.stringify(message));
            if (message.reload == true) {
                window.location.reload();
            }

        });
    }
</r:script>

<button type="button" onclick="addOne();">One</button>

<r:layoutResources/>

<r:external uri='/js/Message.js'/>

</body>
</html>