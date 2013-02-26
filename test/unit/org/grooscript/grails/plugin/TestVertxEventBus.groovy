package org.grooscript.grails.plugin

/**
 * User: jorgefrancoleza
 * Date: 24/02/13
 */
class TestVertxEventBus extends GroovyTestCase {

    static final PORT = 8085
    static final HOST = 'localhost'
    static final CHANNEL = 'channel'
    static final MESSAGE = [data:'data']

    def getEventBus() {
        return new VertxEventBus(PORT,HOST)
    }

    void testInitialization () {

        def eventBus
        try {
            eventBus = new VertxEventBus(PORT,HOST)
        } catch (e) {
            fail 'Exception '+e.message
        }

        assert eventBus
        assert eventBus.getUrlEventBus() == "http://${HOST}:${PORT}/eventbus"
    }

    void testSendMessage() {
        VertxEventBus eventBus = getEventBus()
        assert eventBus.send(CHANNEL,MESSAGE)
    }

    void testAddHandler() {
        VertxEventBus eventBus = getEventBus()
        def times = 0
        eventBus.addChannelHandler(CHANNEL,{ msg -> times++})//; msg.reply ([ok:true]) })
        /*assert eventBus.send(CHANNEL,MESSAGE, {
            assert times == 1
        })*/
        assert eventBus.send(CHANNEL,MESSAGE)
        sleep(200)
        assert times == 1
    }
}
