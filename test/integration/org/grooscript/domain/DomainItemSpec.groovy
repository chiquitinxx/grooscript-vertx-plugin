package org.grooscript.domain

import grails.plugin.spock.IntegrationSpec
import org.grooscript.grails.plugin.GrooscriptConverter
import org.grooscript.grails.util.GrooscriptGrails
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by jorge on 26/12/13.
 */
class DomainItemSpec extends IntegrationSpec {

    def 'properties of domain class'() {
        given:
        def domainItem = new DomainItem()
        def properties = GrooscriptGrails.getRemoteDomainClassProperties(domainItem)

        expect:
        properties == [date:null, id:null, name:null, number:null, version:null]
    }

    @Unroll
    def 'get path of a valid domain class'() {
        given:
        def grooscriptConverter = new GrooscriptConverter()

        expect:
        'grails-app/domain/org/grooscript/domain/DomainItem.groovy' ==
                grooscriptConverter.getDomainFilePath(domainClassName)

        where:
        domainClassName << ['DomainItem', 'org.grooscript.domain.DomainItem']
    }

    @Unroll
    def 'get path of an invalid domain class'() {
        given:
        def grooscriptConverter = new GrooscriptConverter()

        expect:
        null == grooscriptConverter.getDomainFilePath(domainClassName)

        where:
        domainClassName << ['Item', 'org.DomainItem']
    }
}
