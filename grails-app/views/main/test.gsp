<%--
  User: jorge
  Date: 23/08/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Title Test</title>
  <r:layoutResources/>
</head>
<body>

    <p>Test p</p>
    <grooscript:template>
        ul {
            li 'One'
            li 'Two'
            li 'Three'
        }
    </grooscript:template>

    <button type="button" id="addButton" onclick="grooscriptEvents.sendMessage('redraw',null);">Add</button>

    <grooscript:template listenEvents="['redraw']">
        def number = numberTimes++
        h3 'Number of times: ' + number
        number.times { num ->
            p ' '+num
        }
    </grooscript:template>

    <grooscript:model domainClass='org.grooscript.domain.DomainItem'/>

    <r:script>
        var a = DomainItem();
        var numberTimes = 0;
    </r:script>

    <r:layoutResources/>

</body>
</html>