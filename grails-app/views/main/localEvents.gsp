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
        def myData = [domainAction: 'read', className: 'DomainItem', data: [id: message.id]]
        GrooscriptGrails.remoteDomainAction(myData, { data ->
            GrooscriptGrails.sendClientMessage('getSucceed', data)
        }, { error ->
            GrooscriptGrails.sendClientMessage('failRemote', error)
        })
    </grooscript:onEvent>
    <grooscript:onEvent name="getSucceed">
        $('#console').append('<p>Get item with id ' + message.id + '</p>')
    </grooscript:onEvent>
    <grooscript:onEvent name="getSucceed">
        println 'Get Success!'
        def item = message
        item.name = 'Pepe'
        def myData = [domainAction: 'update', className: 'DomainItem', data: item]
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
        def myData = [domainAction: 'list', className: 'DomainItem', data: null]
        GrooscriptGrails.remoteDomainAction(myData, { data ->
            GrooscriptGrails.sendClientMessage('listSucceed', data)
        }, { error ->
            GrooscriptGrails.sendClientMessage('failRemote', error)
        })
    </grooscript:onEvent>
    <grooscript:onEvent name="listSucceed">
        $('#console').append('<p>Listed item!</p>')
    </grooscript:onEvent>
    <grooscript:onEvent name="listSucceed">
        println 'List Success! ' + message
        message.each { item ->
             $('#console').append('<p>Id:' +item.id +' Name:' +item.name + '</p>')
        }
        def myData = [domainAction: 'delete', className: 'DomainItem', data: [id: message.first().id]]
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