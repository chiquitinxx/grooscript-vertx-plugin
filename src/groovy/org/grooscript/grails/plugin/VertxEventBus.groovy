package org.grooscript.grails.plugin

import groovyx.gpars.dataflow.DataflowVariable
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.EventBus
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.sockjs.SockJSServer

import static groovyx.gpars.dataflow.Dataflow.task
import static org.grooscript.grails.util.Util.*

/**
 * User: jorgefrancoleza
 * Date: 24/02/13
 */
class VertxEventBus implements EventHandler {

    static final String CHANNEL_RELOAD = 'reloadPage'
    static final String EVENTBUS_NAME = 'eventbus'

    static Vertx vertx
    EventBus eventBus
    HttpServer httpServer
    SockJSServer sockServer
    Map listeners = [:]
    def host
    def port
    ListenerFileChangesDaemon fileChangesListener

    def stopListeners() {
        if (fileChangesListener) {
            fileChangesListener.stop()
        }
    }

    VertxEventBus (int port, String host, inboundPermitted = [], outboundPermitted = [], testing = false) {

        if (!vertx) {
            try {
                vertx = Vertx.newVertx(port, host)
            } catch (e) {
                consoleError "VertxEventBus error creating Vertx: ${e.message}"
            }
        } else {
            consoleMessage 'Vertx already started. Reusing it.'
        }

        httpServer = vertx.createHttpServer()

        def config = ["prefix": '/' + EVENTBUS_NAME ]

        addChannelToList(CHANNEL_RELOAD, outboundPermitted)
        if (testing) {
            addChannelToList('testing', inboundPermitted)
            addChannelToList('testingIncoming', outboundPermitted)
        }

        sockServer = vertx.createSockJSServer(httpServer).bridge(config, inboundPermitted, outboundPermitted)

        httpServer.listen(port)

        eventBus = vertx.eventBus
        this.host = host
        this.port = port
        consoleMessage "Vertx event bus bridge listening ${urlEventBus}"
    }

    private addChannelToList(String channelName, List list) {
        if (!list.find { it."address" == channelName}) {
            list << ["address": channelName]
        }
    }

    def getUrlEventBus() {
        return "http://${host}:${port}/${EVENTBUS_NAME}"
    }

    def reactChannel(String name, data) {
        if (listeners[name]) {
            listeners[name].each {
                it(data)
            }
        }
    }

    void onEvent(String channel, Closure action) {
        if (listeners[channel]) {
            listeners[channel] << action
        } else {
            eventBus.registerHandler(channel, this.&reactChannel.curry(channel))
            listeners[channel] = [action]
        }
    }

    void sendMessage(String channel,Map data) {
        try {
            eventBus.publish(channel, data)
        } catch (e) {
            consoleError "VertxEventBus.sendMessage channel:${channel} data:${data} error:${e.message}"
        }
    }

    void close() {
        consoleMessage 'Closing Vertx EventBus...'
        final DataflowVariable closed = new DataflowVariable()
        listeners = [:]
        httpServer.close({
            task {
                closed << true
            }
        })
        closed.val == true
        stopListeners()
        consoleMessage 'Closed Vertx EventBus.'
    }
}
