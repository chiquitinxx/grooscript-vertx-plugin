package org.grooscript.grails.util

import org.grooscript.asts.GsNative

/**
 * User: jorgefrancoleza
 * Date: 22/09/13
 */
class GrooscriptGrails {

    @GsNative
    static sendClientMessage(String channel, message) {/*
        var sendMessage = message;
        if (message['gSclass'] == undefined) {
            message = GrooscriptGrails.toGroovy(message);
        }
        grooscriptEvents.sendMessage(channel, message);
    */}

    @GsNative
    static sendServerMessage(String channel, message) {/*
        var sendMessage = message;
        if (message['gSclass'] !== undefined) {
            message = GrooscriptGrails.toJavascript(message);
        }
        grooscriptEventBus.send(channel, message);
    */}

    @GsNative
    static toJavascript(message) {/*
        var result;
        if (message!=null && message!=undefined && typeof(message) !== "function") {
            if (message instanceof Array) {
                result = [];
                var i;
                for (i = 0; i < message.length; i++) {
                    result[result.length] = GrooscriptGrails.toJavascript(message[i]);
                }
            } else {
                if (message instanceof Object) {
                    result = {};
                    for (ob in message) {
                        if (!isgSmapProperty(ob)) {
                            result[ob] = GrooscriptGrails.toJavascript(message[ob]);
                        }
                    }
                } else {
                    result = message;
                }
            }
        }
        return result;
    */}

    @GsNative
    static toGroovy(message) {/*
        var result;
        if (message!=null && message!=undefined && typeof(message) !== "function") {
            if (message instanceof Array) {
                result = gSlist([]);
                var i;
                for (i = 0; i < message.length; i++) {
                    result.add(GrooscriptGrails.toGroovy(message[i]));
                }
            } else {
                if (message instanceof Object) {
                    result = gSmap();
                    for (ob in message) {
                        result.add(ob, GrooscriptGrails.toGroovy(message[ob]));
                    }
                } else {
                    result = message;
                }
            }
        }
        return result;
    */}
}
