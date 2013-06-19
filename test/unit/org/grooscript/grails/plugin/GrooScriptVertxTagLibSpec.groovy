package org.grooscript.grails.plugin

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grooscript.GrooScript
import spock.lang.Specification

/**
 * @author Jorge Franco
 * Date: 19/06/13
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(GrooScriptVertxTagLib)
class GrooScriptVertxTagLibSpec extends Specification {

    static final URL = 'url'

    void setup() {
        defineBeans {
            "${GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN}"(VertxEventBus)
        }
        GrooScriptVertxTagLib.metaClass.r = {
            def result = []
            result.script = { Closure cl ->
                cl()
            }

            return result
        }
    }

    static final CODE = 'code'
    static final RESULT = 'result'

    void 'test code'() {
        given:
        GroovySpy(GrooScript, global: true)

        expect:
        applicationContext.containsBean(GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN)

        when:
        def result = applyTemplate("<grooscript:code>${CODE}</grooscript:code>")

        then:
        1 * GrooScript.convert(CODE) >> RESULT
        result == RESULT
    }
}
