<%--
  User: jorge
  Date: 16/09/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Testing template</title>
    <r:require module="jquery"/>
    <r:layoutResources/>
</head>
<body>

    <div id='template2'></div>
    <div id='template3'></div>
    <div id='template4'></div>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy" functionName="template1"/>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy" itemSelector="#template2"/>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy" renderOnReady="false" itemSelector="#template3"/>

    <grooscript:template functionName="hola" itemSelector="#template4" renderOnReady="false">
        p 'Hola from:' + data.from + ' to:' + data.to
    </grooscript:template>

    <r:script>
        $(document).ready(function() {
            var map = gs.toGroovy({from: 'Me', to: 'You'});
            hola(map);
        });
    </r:script>

    <r:layoutResources/>
</body>
</html>