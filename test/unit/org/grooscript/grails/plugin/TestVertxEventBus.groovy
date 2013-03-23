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

    def VertxEventBus eventBus

    void setUp() {
        //println 'Init!'
        //eventBus = getEventBus()
        //eventBus.close()
    }

    void tearDown() {
        if (eventBus) {
            eventBus.close()
        }
    }

    def getEventBus(num = 0) {
        return new VertxEventBus(PORT+num,HOST)
    }

    void testInitialization () {

        try {
            eventBus = getEventBus()
        } catch (e) {
            fail 'Exception '+e.message
        }

        assert eventBus
        assert eventBus.getUrlEventBus() == "http://${HOST}:${PORT}/eventbus"
    }

    void testSendMessage() {
        eventBus = getEventBus()
        assert eventBus.send(CHANNEL,MESSAGE)
    }

    void testAddHandler() {
        eventBus = getEventBus()
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
