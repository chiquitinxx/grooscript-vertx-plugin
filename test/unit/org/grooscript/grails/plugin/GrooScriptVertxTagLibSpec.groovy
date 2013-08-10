package org.grooscript.grails.plugin

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugin.resource.ResourceTagLib
import org.grooscript.GrooScript
import spock.lang.Specification

/**
 * @author Jorge Franco
 * Date: 19/06/13
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(GrooScriptVertxTagLib)
class GrooScriptVertxTagLibSpec extends Specification {

    void setup() {
    }

    static final CODE = 'code example'

    void 'test code taglib'() {
        given: 'mock taglib and grooscript'
        def resourceTaglib = Mock(ResourceTagLib)
        GrooScriptVertxTagLib.metaClass.r = resourceTaglib
        GroovySpy(GrooScript, global: true)

        when: 'applying grooscript code taglib'
        applyTemplate("<grooscript:code>${CODE}</grooscript:code>")

        then: 'call convert and script'
        1 * resourceTaglib.script(_)
        1 * GrooScript.convert(CODE)
        1 * GrooScript.getConverter()
        0 * _
    }

    void 'test reload page'() {
        given: 'mock taglib'
        def resourceTaglib = Mock(ResourceTagLib)
        GrooScriptVertxTagLib.metaClass.r = resourceTaglib

        and: 'add vertx if necesary'
        if (hasVertx) {
            //BEWARE beans stay for next tests
            defineBeans {
                "${GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN}"(VertxEventBus,'localhost',8989)
            }
        }

        when: 'applying grooscript reloadPage taglib'
        def result = applyTemplate("<grooscript:reloadPage${withoutJsLib?'WithoutJsLibs':''}/>")

        then: 'call require if hasVertex'
        (hasVertx && !withoutJsLib?1:0) * resourceTaglib.require(_) >> CODE
        (hasVertx?1:0) * resourceTaglib.script(_)
        0 * _
        expectedResult == result

        where:
        withoutJsLib|hasVertx    |expectedResult
        true        |false       |''
        false       |false       |''
        false       |true        |CODE
        true        |true        |''
    }
}
