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
class VertxEventBus {

    static final String CHANNEL_CHANGES = 'changeFiles'
    static final String CHANNEL_RELOAD = 'reloadPage'
    static final String CONSOLE_MESSAGE = '[GrooScript-Vertx]'
    static final String EVENTBUS_NAME = 'eventbus'

    static Vertx vertx
    def EventBus eventBus
    HttpServer httpServer
    SockJSServer sockServer
    def Stack stack
    def host
    def port

    VertxEventBus (int port,String host) {

        stack = new Stack()

        if (!vertx) {
            try {
                vertx = Vertx.newVertx(port,host)
            } catch (e) {
                println '!-'+e.message
            }
        } else {
            println "\n${CONSOLE_MESSAGE} Vertx already started. Reusing it."
        }

        httpServer = vertx.createHttpServer()

        def config = ["prefix": '/' + EVENTBUS_NAME ]
        def inboundPermitted = []
        def outboundPermitted = []
        outboundPermitted << ["address": CHANNEL_RELOAD]

        sockServer = vertx.createSockJSServer(httpServer).bridge(config, inboundPermitted, outboundPermitted)

        httpServer.listen(port)

        eventBus = vertx.eventBus
        this.host = host
        this.port = port
        println "\n${CONSOLE_MESSAGE} Vertx event bus bridge listening ${getUrlEventBus()}"
        listenFileChanges()
    }

    def getUrlEventBus() {
        return "http://${host}:${port}/${EVENTBUS_NAME}"
    }


    def listenFileChanges() {
        addChannelHandler(CHANNEL_CHANGES,{ msg ->
            eventBus.publish(CHANNEL_RELOAD,[reload:true])
        })
    }

    def send(String channel,message) {
        boolean result = true
        try {
            eventBus.send(channel,message)
        } catch (e) {
            //println 'Fail Send!'
            result = false
        }

        return result
    }

    def send(String channel,message,Closure onResponse) {
        boolean result = true
        try {
            eventBus.send(channel,message,onResponse)
        } catch (e) {
            //println 'Fail Send with response!'
            result = false
        }

        return result
    }

    def addChannelHandler(String channel,Closure handler) {
        eventBus.registerHandler(channel,handler)
        stack << channel
    }

    def close() {

        stopListener()

        final DataflowVariable closed = new DataflowVariable()
        httpServer.close({
            task {
                closed << true
            }
        })
        closed.val == true

        while (!stack.empty()) {
            eventBus.unregisterSimpleHandler stack.pop()
        }

    }

    ListenerDaemon actualListener

    def stopListener() {
        if (actualListener) {
            actualListener.stop()
        }
    }

    def startListener(ListenerDaemon listener) {
        if (actualListener) {
            stopListener()
        }
        actualListener = listener
        actualListener.start()
    }
}
