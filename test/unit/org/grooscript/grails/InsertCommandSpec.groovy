package org.grooscript.grails

import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.grooscript.grails.plugin.GrooscriptVertxService
import spock.lang.Specification

/**
 * User: jorgefrancoleza
 * Date: 15/10/13
 */
@TestMixin(ControllerUnitTestMixin)
class InsertCommandSpec extends Specification {

    InsertCommand cmd

    def setup() {
        cmd = mockCommandObject(InsertCommand)
        cmd.grooscriptVertxService = Mock(GrooscriptVertxService)
    }

    def 'test validate'() {
        when:
        cmd.data = map
        cmd.className = className

        then:
        _ * cmd.grooscriptVertxService.canAccessDomainClass(className) >> canAccess
        _ * cmd.grooscriptVertxService.existDomainClass(className) >> existClass
        cmd.validate() == expectedResult

        where:
        map  | className | canAccess | existClass | expectedResult
        null | null      | _         | _          | false
        [:]  | ''        | _         | _          | false
        [:]  | 'any'     | false     | false      | false
    }
}
