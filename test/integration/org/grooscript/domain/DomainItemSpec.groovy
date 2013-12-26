package org.grooscript.domain

import grails.plugin.spock.IntegrationSpec
import org.grooscript.grails.util.GrooscriptGrails
import spock.lang.Specification

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
}
