package org.grooscript.grails.test

/**
 * User: jorgefrancoleza
 * Date: 15/10/13
 */
class MixinUtils {

    static decodeJSON(String json) {
        new XmlSlurper().parseText(json)
    }
}
