<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Testing remote model</title>
    <r:layoutResources/>
</head>

<body>
    <div id="info"></div>
    <grooscript:remoteModel domainClass="org.grooscript.domain.DomainItem"/>

    <r:script>
        $(document).ready(function() {
            var domainItem = DomainItem();
            domainItem.name = 'grooscript';
            domainItem.save().then (function(data) {
                $('#info').html('saved Ok!'+gs.toGroovy(data.data));
            }, function(data) {
                $('#info').html('Error! '+data);
            })
        });
    </r:script>

    <r:layoutResources/>
</body>
</html>