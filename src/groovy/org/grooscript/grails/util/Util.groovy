package org.grooscript.grails.util

/**
 * User: jorgefrancoleza
 * Date: 13/09/13
 */
class Util {

    static final SEP = System.getProperty('file.separator')

    static final String GROOVY_DIR = "src${SEP}groovy"
    static final String DOMAIN_DIR = "grails-app${SEP}domain"
    static final String DOMAIN_JS_DIR = "web-app${SEP}js${SEP}domain"
    static final String DOMAIN_CLASSES_JS_FILE = "web-app${SEP}js${SEP}domainClasses.js"

    static final PLUGIN_MESSAGE = '[GrooScript Vertx Plugin]'

    static consoleMessage(message) {
        println "${PLUGIN_MESSAGE} $message"
    }

    static consoleError(message) {
        println "\u001B[91m${PLUGIN_MESSAGE} [ERROR] $message\u001B[0m"
    }
}
