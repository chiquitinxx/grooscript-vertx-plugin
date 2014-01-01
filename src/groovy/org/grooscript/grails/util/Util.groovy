package org.grooscript.grails.util

/**
 * User: jorgefrancoleza
 * Date: 13/09/13
 */
class Util {

    static final SEP = System.getProperty('file.separator')

    static final String DOMAIN_NAME = 'domain'
    static final String REMOTE_NAME = 'remote'
    static final String GROOVY_DIR = "src${SEP}groovy"
    static final String DOMAIN_DIR = "grails-app${SEP}${DOMAIN_NAME}"
    static final String DOMAIN_JS_DIR = "web-app${SEP}js${SEP}${DOMAIN_NAME}"
    static final String REMOTE_JS_DIR = "web-app${SEP}js${SEP}${REMOTE_NAME}"

    static final PLUGIN_MESSAGE = '[GrooScript Vertx Plugin]'

    static consoleMessage(message) {
        println "${PLUGIN_MESSAGE} [INFO] $message"
    }

    static consoleError(message) {
        println "\u001B[91m${PLUGIN_MESSAGE} [ERROR] $message\u001B[0m"
    }

    static consoleWarning(message) {
        println "\u001B[93m${PLUGIN_MESSAGE} [WARNING] $message\u001B[0m"
    }
}
