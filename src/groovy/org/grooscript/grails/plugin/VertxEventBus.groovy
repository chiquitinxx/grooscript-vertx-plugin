package org.grooscript.grails.plugin

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.EventBus
import org.vertx.groovy.core.http.HttpServer

/**
 * User: jorgefrancoleza
 * Date: 24/02/13
 */
class VertxEventBus {

    static final String CHANNEL_CHANGES = 'changeFiles'
    static final String CHANNEL_RELOAD = 'reloadPage'

    Vertx vertx
    def EventBus eventBus
    def host
    def port

    VertxEventBus (int port,String host) {

        vertx = Vertx.newVertx(port,host)
        HttpServer httpServer = vertx.createHttpServer()

        def config = ["prefix": "/eventbus"]
        def inboundPermitted = []
        def outboundPermitted = []
        outboundPermitted << ["address": CHANNEL_RELOAD]

        vertx.createSockJSServer(httpServer).bridge(config, inboundPermitted, outboundPermitted)

        httpServer.listen(port)

        eventBus = vertx.eventBus
        this.host = host
        this.port = port
        println "\n[GrooScript-Vertx] Vertx event bus bridge listening ${getUrlEventBus()}"
        listenFileChanges()
    }

    def getUrlEventBus() {
        return "http://${host}:${port}/eventbus"
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
            println 'Fail Send!'
            result = false
        }

        return result
    }

    def send(String channel,message,Closure onResponse) {
        boolean result = true
        try {
            eventBus.send(channel,message,onResponse)
        } catch (e) {
            println 'Fail Send with response!'
            result = false
        }

        return result
    }

    def addChannelHandler(String channel,Closure handler) {
        eventBus.registerHandler(channel,handler)
    }
}
