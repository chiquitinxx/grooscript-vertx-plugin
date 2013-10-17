package org.grooscript.grails

import grails.test.mixin.TestFor
import org.grooscript.grails.plugin.GrooscriptVertxService
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(DomainController)
class DomainControllerSpec extends Specification {

    private static final DATA = ['hello':'hello']
    private static final ERRORS = ['error 1']

    def setup() {
        controller.grooscriptVertxService = Mock(GrooscriptVertxService)
    }

    def cleanup() {
    }

    @Unroll
    void 'test action #action with command #commandClass'() {
        given:
        def command = Mock(ActionCommand)
        command.data >> DATA

        when:
        controller.doAction(command)

        then:
        1 * controller.grooscriptVertxService.getErrorsForClient(command, resultValidate, resultExecute) >> ERRORS
        1 * command.validate() >> resultValidate
        _ * command.execute() >> resultExecute
        response.json.result == expectedResult
        response.json.data == DATA
        response.json.listErrors == ERRORS

        where:
        resultValidate | resultExecute | expectedResult
        true           | false         | controller.KO
        false          | _             | controller.KO
        true           | true          | controller.OK
    }
}