package org.grooscript.grails.plugin

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugin.resource.ResourceTagLib
import org.grooscript.GrooScript
import org.grooscript.grails.util.Builder
import spock.lang.Specification

import static org.grooscript.grails.util.Util.getDOMAIN_JS_EXTERNAL
import static org.grooscript.grails.util.Util.getSEP

/**
 * @author Jorge Franco
 * Date: 19/06/13
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(GrooScriptVertxTagLib)
class GrooScriptVertxTagLibSpec extends Specification {

    def resourceTaglib

    void setup() {
        resourceTaglib = Mock(ResourceTagLib)
        GrooScriptVertxTagLib.metaClass.r = resourceTaglib
    }

    static final CODE = 'code example'

    void 'test code taglib'() {
        given: 'mock grooscript'
        GroovySpy(GrooScript, global: true)

        when: 'applying grooscript code taglib'
        applyTemplate("<grooscript:code>${CODE}</grooscript:code>")

        then: 'call convert and script'
        1 * resourceTaglib.script(_)
        1 * GrooScript.convert(CODE)
    }

    void 'test reload page'() {

        given: 'add vertx if necesary'
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

        and:
        if (hasVertx) {
            applicationContext.getBean(GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN)?.close()
        }

        where:
        withoutJsLib | hasVertx | expectedResult
        true         | false    | ''
        false        | false    | ''
        false        | true     | CODE
        true         | true     | ''
    }

    void 'test template'() {
        given: 'mock grooscript'
        GroovySpy(GrooScript, global: true)

        when:
        def result = applyTemplate("<grooscript:template>assert true</grooscript:template>")

        then:
        1 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'kimbo'])
        1 * resourceTaglib.require([module: 'grooscript'])
        1 * resourceTaglib.require([module: 'grooscriptGrails'])
        1 * GrooScript.convert('Builder.process { -> assert true}')
        result.startsWith "\n<div id='fTemplate"
    }

    void 'test template with a reload event'() {
        when:
        applyTemplate('<grooscript:template listenEvents="$events">h3 \'Hello!\'</grooscript:template>',
                [events: ['redraw']])

        then:
        2 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'clientEvents'])
    }

    static final FAKE_NAME = 'FAKE'
    static final DOMAIN_CLASS_NAME = 'correctDomainClass'

    void 'test model with domain class'() {
        given:
        GrooScriptVertxTagLib.metaClass.existDomainClass = { String name ->
            name != FAKE_NAME
        }

        when:
        applyTemplate("<grooscript:model domainClass='${domainClassName}'/>")

        then:
        numberTimes * resourceTaglib.require([module: 'domainClasses'])

        where:
        domainClassName   | numberTimes
        FAKE_NAME         | 0
        DOMAIN_CLASS_NAME | 1
    }
}
