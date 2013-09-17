<%--
  User: jorge
  Date: 16/09/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Testing template</title>
  <r:layoutResources/>
</head>
<body>

    <div id='template2'></div>
    <div id='template3'></div>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy" functionName="template1"/>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy" itemSelector="#template2"/>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy" renderOnReady="false" itemSelector="#template3"/>

    <r:layoutResources/>
</body>
</html>