package org.grooscript.grails.util

import org.grooscript.asts.GsNative

/**
 * User: jorgefrancoleza
 * Date: 22/09/13
 */
class GrooscriptGrails {

    static remoteUrl
    static controllerRemoteDomain = 'remoteDomain'
    static actionRemoteDomain = 'doAction'

    @GsNative
    static sendClientMessage(String channel, message) {/*
        var sendMessage = message;
        if (!GrooscriptGrails.isGroovyObject(message)) {
            sendMessage = gs.toGroovy(message);
        }
        grooscriptEvents.sendMessage(channel, sendMessage);
    */}

    @GsNative
    static sendServerMessage(String channel, message) {/*
        var sendMessage = message;
        if (GrooscriptGrails.isGroovyObject(message)) {
            sendMessage = gs.toJavascript(message);
        }
        grooscriptEventBus.send(channel, sendMessage);
    */}

    @GsNative
    static doRemoteCall(String controller, String action, params, onSuccess, onFailure) {/*
        var url = GrooscriptGrails.remoteUrl;
        url = url + '/' + controller;
        if (domainAction != null) {
            url = url + '/' + domainAction;
        }
        $.ajax({
            type: "POST",
            data: (GrooscriptGrails.isGroovyObject(params) ? gs.toJavascript(params) : params),
            url: url
        }).done(function(newData) {
            onSuccess(newData);
        })
        .fail(function(error) {
            onFailure(error);
        });
    */}

    @GsNative
    static remoteDomainAction(params, onSuccess, onFailure) {/*
        var url = GrooscriptGrails.remoteUrl + '/' + GrooscriptGrails.controllerRemoteDomain +
            '/' + GrooscriptGrails.actionRemoteDomain;
        $.ajax({
            type: "POST",
            data: (GrooscriptGrails.isGroovyObject(params) ? gs.toJavascript(params) : params),
            url: url
        }).done(function(newData) {
            if (newData.result == 'OK') {
                onSuccess(newData);
            } else {
                onFailure(newData.listErrors);
            }
        })
        .fail(function(error) {
            onFailure(error);
        });
    */}

    @GsNative
    private static boolean isGroovyObject(objectItem) {/*
        return objectItem['clazz'] !== undefined;
    */}
}
