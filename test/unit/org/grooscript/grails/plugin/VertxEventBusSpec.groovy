package org.grooscript.grails.plugin

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

    VertxEventBus eventBus

    void setup() {
        eventBus = obtainNewEventBus()
    }

    void cleanup() {
        if (eventBus) {
            eventBus.close()
        }
    }

    def obtainNewEventBus() {
        new VertxEventBus(PORT, HOST)
    }

    void 'test initialization'() {
        expect:
        eventBus.getUrlEventBus() == "http://${HOST}:${PORT}/eventbus"
    }

    void 'test send message'() {
        expect:
        eventBus.sendMessage(CHANNEL, MESSAGE)
        sleep(200)
    }

    void 'test listen event'() {
        given:
        def times = 0
        eventBus.onEvent(CHANNEL, { msg -> println "Recieved: ${msg.body}"; times++})
        eventBus.onEvent(CHANNEL, { msg -> times++})

        expect:
        times == 0

        when:
        eventBus.sendMessage(CHANNEL, MESSAGE)
        sleep(200)

        then:
        times == 2
    }
}
