<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Testing model</title>
    <r:require module="jquery"/>
    <r:require module="domain"/>
    <r:layoutResources/>
</head>

<body>
    <div id="info"></div>
    <!--grooscript:model domainClass="org.grooscript.domain.DomainItem"/-->

    <r:script>
        $(document).ready(function() {
            var domainItem = DomainItem();
            domainItem.name = 'grooscript';
            if (domainItem.save()) {
                $('#info').html('saved Ok, number of domain items: ' + DomainItem.count());
            } else {
                $('#info').html('Error!');
            }
        });
    </r:script>

    <r:layoutResources/>
</body>
</html>