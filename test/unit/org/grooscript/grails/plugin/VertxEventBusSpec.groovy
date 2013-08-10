package org.grooscript.grails.plugin

import groovy.util.logging.Log
import spock.lang.Specification

/**
 * User: jorgefrancoleza
 * Date: 24/02/13
 */
class VertxEventBusSpec extends Specification {

    static final PORT = 8085
    static final HOST = 'localhost'
    static final CHANNEL = 'channel'
    static final MESSAGE = [data:'data']

    def VertxEventBus eventBus

    void setup() {
        eventBus = getEventBus()
    }

    void cleanup() {
        if (eventBus) {
            eventBus.close()
        }
    }

    def getEventBus() {
        def vertx = new VertxEventBus(PORT,HOST)
        vertx
    }

    void 'test initialization'() {
        expect:
        eventBus.getUrlEventBus() == "http://${HOST}:${PORT}/eventbus"
    }

    void 'test send message'() {
        expect:
        eventBus.sendMessage(CHANNEL,MESSAGE)
    }

    void 'test listen event'() {
        given:
        def times = 0
        eventBus.onEvent(CHANNEL,{ msg -> println "Recieved: ${msg.body}";times++})
        eventBus.onEvent(CHANNEL,{ msg -> times++})

        expect:
        times == 0
        eventBus.sendMessage(CHANNEL,MESSAGE)

        when:
        sleep(200)

        then:
        times == 2
    }
}
