package org.grooscript.grails.plugin

import grails.plugin.spock.IntegrationSpec
import org.grooscript.domain.DomainItem
import spock.lang.Ignore

/**
 * User: jorgefrancoleza
 * Date: 12/09/13
 */
class GrooScriptTagLibIntegrationSpec extends IntegrationSpec {

    @Ignore
    def 'test use model'() {
        given:
        def taglib = new GrooScriptVertxTagLib()

        when:
        def result = taglib.model(domainClass: DomainItem)

        then:
        result.toString() == 'pepe'
    }
}
