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
    <link rel="stylesheet" type="text/css" href="../css/style.css">

    <title>Title</title>
</head>
<body>

<p>Try Page.</p>

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

</body>
</html>