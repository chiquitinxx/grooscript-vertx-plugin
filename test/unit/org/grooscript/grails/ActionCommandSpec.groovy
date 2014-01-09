package org.grooscript.grails

import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.grooscript.grails.plugin.GrooscriptVertxService
import spock.lang.Specification
import spock.lang.Unroll

import static org.grooscript.grails.plugin.GrooscriptVertxService.*
/**
 * User: jorgefrancoleza
 * Date: 15/10/13
 */
@TestMixin(ControllerUnitTestMixin)
class ActionCommandSpec extends Specification {

    private static final MAP = [1:1]
    private static final NAME_CLASS = 'name'
    private static final RESULT = true
    private static final FAKE_ACTION = 'fake action'

    ActionCommand cmd

    def setup() {
        cmd = mockCommandObject(ActionCommand)
        cmd.grooscriptVertxService = Mock(GrooscriptVertxService)
    }

    @Unroll
    def 'test validate'() {
        when:
        cmd.data = map
        cmd.className = className
        cmd.domainAction = domainAction
        def result = cmd.validate()

        then:
        _ * cmd.grooscriptVertxService.canDoActionWithDomainClass(className, domainAction) >> canAccess
        _ * cmd.grooscriptVertxService.existDomainClass(className) >> existClass
        result == expectedResult

        where:
        domainAction | map  | className | canAccess | existClass | expectedResult
        _            | null | _         | _         | _          | false
        null         | [:]  | _         | _         | _          | false
        _            | [:]  | null      | _         | _          | false
        null         | [:]  | ''        | _         | _          | false
        null         | [:]  | 'any'     | false     | false      | false
        ''           | [:]  | 'any'     | false     | false      | false
        FAKE_ACTION  | [:]  | 'any'     | false     | false      | false
        READ_ACTION  | [:]  | 'any'     | true      | true       | true
        READ_ACTION  | [:]  | 'any'     | true      | false      | false
        READ_ACTION  | [:]  | 'any'     | false     | true       | false
        LIST_ACTION  | null | 'any'     | true      | true       | true
    }

    @Unroll
    def 'test execute'() {
        when:
        cmd.data = MAP
        cmd.className = NAME_CLASS
        cmd.domainAction = domainAction
        def result = cmd.execute()

        then:
        1 * cmd.grooscriptVertxService."${domainAction}"(NAME_CLASS, cmd) >> RESULT
        result == RESULT

        where:
        domainAction << ALL_DOMAIN_ACTIONS
    }
}
