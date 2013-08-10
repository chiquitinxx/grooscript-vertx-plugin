package org.grooscript.grails.plugin

import groovyx.gpars.dataflow.DataflowVariable
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.EventBus
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.sockjs.SockJSServer

import static groovyx.gpars.dataflow.Dataflow.task

/**
 * User: jorgefrancoleza
 * Date: 24/02/13
 */
class VertxEventBus implements EventHandler {

    static final String CHANNEL_CHANGES = 'changeFiles'
    static final String CHANNEL_RELOAD = 'reloadPage'
    static final String EVENTBUS_NAME = 'eventbus'

    static Vertx vertx
    EventBus eventBus
    HttpServer httpServer
    SockJSServer sockServer
    Map listeners = [:]
    def host
    def port

    VertxEventBus (int port,String host) {

        if (!vertx) {
            try {
                vertx = Vertx.newVertx(port,host)
            } catch (e) {
                println "VertxEventBus error creating Vertx: ${e.message}"
            }
        } else {
            println 'Vertx already started. Reusing it.'
        }

        httpServer = vertx.createHttpServer()

        def config = ["prefix": '/' + EVENTBUS_NAME ]
        def inboundPermitted = []
        def outboundPermitted = []
        //outboundPermitted << ["address": CHANNEL_RELOAD]

        sockServer = vertx.createSockJSServer(httpServer).bridge(config, inboundPermitted, outboundPermitted)

        httpServer.listen(port)

        eventBus = vertx.eventBus
        this.host = host
        this.port = port
        println "Vertx event bus bridge listening ${getUrlEventBus()}"
        //listenFileChanges()
    }

    def getUrlEventBus() {
        return "http://${host}:${port}/${EVENTBUS_NAME}"
    }

    /*
    def listenFileChanges() {
        addChannelHandler(CHANNEL_CHANGES,{ msg ->
            eventBus.publish(CHANNEL_RELOAD,[reload:true])
        })
    }*/
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
            println "VertxEventBus.sendMessage channel:${channel} data:${data} error:${e.message}"
        }
    }

    /*
    def addChannelHandler(String channel,Closure handler) {
        eventBus.registerHandler(channel,handler)
    }
    */

    void close() {
        println 'Closing Vertx EventBus...'
        final DataflowVariable closed = new DataflowVariable()
        httpServer.close({
            task {
                closed << true
            }
        })
        closed.val == true
        println 'Closed Vertx EventBus.'
    }
}
