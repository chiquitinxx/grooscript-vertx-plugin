package org.grooscript.grails

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(DomainController)
class DomainControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void 'test insert'() {
        given:
        def command = Mock(InsertCommand)

        when:
        controller.insert(command)

        then:
        1 * command.validate() >> resultCommand
        response.json.result == expectedResult

        where:
        resultCommand | expectedResult
        true          | controller.OK
        false         | controller.KO
    }
}