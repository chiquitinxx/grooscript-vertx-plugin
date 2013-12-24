<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Testing local events</title>
    <r:require module="jquery"/>
    <r:layoutResources/>
</head>

<body>

    <div id="console"></div>
    <grooscript:onEvent name="failRemote">
        println 'Fail Remote!'
        println message
    </grooscript:onEvent>
    <grooscript:onEvent name="insertSucceed">
        $('#console').append('<p>Added a new Item!</p>')
    </grooscript:onEvent>
    <grooscript:onEvent name="insertSucceed">
        println 'Insert Success!'
        def myData = [domainAction: 'read', className: message.className, data: [id: message.data.id]]
        GrooscriptGrails.remoteDomainAction(myData, { data ->
            GrooscriptGrails.sendClientMessage('getSucceed', data)
        }, { error ->
            GrooscriptGrails.sendClientMessage('failRemote', error)
        })
    </grooscript:onEvent>
    <grooscript:onEvent name="getSucceed">
        $('#console').append('<p>Get item with id ' + message.data.id + '</p>')
    </grooscript:onEvent>
    <grooscript:onEvent name="getSucceed">
        println 'Get Success!'
        def item = message.data
        item.name = 'Pepe'
        def myData = [domainAction: 'update', className: message.className, data: item]
        GrooscriptGrails.remoteDomainAction(myData, { data ->
            GrooscriptGrails.sendClientMessage('updateSucceed', data)
        }, { error ->
            GrooscriptGrails.sendClientMessage('failRemote', error)
        })
    </grooscript:onEvent>
    <grooscript:onEvent name="updateSucceed">
        $('#console').append('<p>Update item!</p>')
    </grooscript:onEvent>
    <grooscript:onEvent name="updateSucceed">
        println 'Update Success!'
        def myData = [domainAction: 'delete', className: message.className, data: [id: message.data.id]]
        GrooscriptGrails.remoteDomainAction(myData, { data ->
            GrooscriptGrails.sendClientMessage('deleteSucceed', data)
        }, { error ->
            GrooscriptGrails.sendClientMessage('failRemote', error)
        })
    </grooscript:onEvent>
    <grooscript:onEvent name="deleteSucceed">
        $('#console').append('<p>Item deleted!</p>')
    </grooscript:onEvent>
    <grooscript:onEvent name="deleteSucceed">
        println 'Delete Success!'
    </grooscript:onEvent>
    <r:script>
        $(document).ready(function() {
            var myData = {domainAction: 'create', className: 'DomainItem', data: { name: 'Jorge'}};
            GrooscriptGrails.remoteDomainAction(myData, function (data) {
                //onSuccess
                console.log('Success: '+data);
                GrooscriptGrails.sendClientMessage('insertSucceed', data);
            }, function (data) {
                //onFail
                GrooscriptGrails.sendClientMessage('failRemote',data);
            });
        });
    </r:script>
    <r:layoutResources/>
</body>
</html>