package presenter

import org.grooscript.asts.GsNative

/**
 * User: jorgefrancoleza
 * Date: 27/02/13
 */

class Message {

    @GsNative
    def salute(who) {/*
        console.log('Hello '+who+'!');
    */}
}

new Message().salute 'Jorge'

