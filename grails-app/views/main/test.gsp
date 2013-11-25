<%--
  User: jorge
  Date: 23/08/13
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Title Test</title>
  <r:require module="jquery"/>
  <r:layoutResources/>
</head>
<body>

    <p>Test p</p>
    <grooscript:template filePath="src/groovy/MyTemplate.groovy"/>

    <button type="button" id="addButton" onclick="grooscriptEvents.sendMessage('redraw',null);">Add</button>

    <grooscript:template listenEvents="['redraw']">
        def number = numberTimes
        numberTimes = numberTimes + 1
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

    <grooscript:code>
        def sing = { name ->
            console.log 'Singing...' + name
        }

        def doSomething = { mapOfClosures ->
            mapOfClosures.each { key, value ->
                value(key)
            }
        }

        $(document).ready doSomething([groovy: sing, grails: sing, grooscript: sing])
    </grooscript:code>

    <r:layoutResources/>

</body>
</html>