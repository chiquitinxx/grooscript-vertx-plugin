package org.grooscript.grails.util

/**
 * User: jorgefrancoleza
 * Date: 13/09/13
 */
class Util {

    static definePhantomJsConfig(String phantomJsPath) {
        System.setProperty('JS_LIBRARIES_PATH','web-app/js')
        if (phantomJsPath) {
            System.setProperty('PHANTOMJS_HOME', phantomJsPath)
        }
    }
}
