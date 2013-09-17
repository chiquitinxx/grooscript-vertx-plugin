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

    def resourceTaglib

    void setup() {
        resourceTaglib = Mock(ResourceTagLib)
        GrooScriptVertxTagLib.metaClass.r = resourceTaglib
    }

    void cleanup() {
        try {
            applicationContext.getBean(GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN)?.close()
        } catch (e) {

        }
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

    static final FILE_PATH = 'src/groovy/org/grooscript/grails/util/Builder.groovy'

    void 'test code taglib with a file'() {
        given:
        GroovySpy(GrooScript, global: true)

        when:
        applyTemplate("<grooscript:code filePath='${FILE_PATH}'/>")

        then:
        1 * resourceTaglib.script(_)
        1 * GrooScript.convert(new File(FILE_PATH).text)
    }

    void 'test init vertx variable'() {
        given:
        initVertx()

        when:
        applyTemplate("<grooscript:initVertx/>")

        then:
        1 * resourceTaglib.require([module: 'vertx'])
        1 * resourceTaglib.script(_)
    }

    void 'test reload page'() {
        given:
        initVertx()

        when:
        applyTemplate("<grooscript:reloadPage/>")

        then:
        1 * resourceTaglib.require(_)
        2 * resourceTaglib.script(_)
        0 * _
    }

    void 'test template'() {
        given:
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

    void 'very basic test template options'() {
        when:
        def result = applyTemplate("<grooscript:template functionName='jarJar'" +
                " itemSelector='#anyId' renderOnReady=\"${true}\">assert true</grooscript:template>")

        then:
        1 * resourceTaglib.script(_)
        !result
    }

    static final FILE_PATH_TEMPLATE = 'src/groovy/MyTemplate.groovy'

    void 'test template with a file'() {
        given:
        GroovySpy(GrooScript, global: true)

        when:
        def result = applyTemplate("<grooscript:template filePath='${FILE_PATH_TEMPLATE}'/>")

        then:
        1 * resourceTaglib.script(_)
        1 * GrooScript.convert("Builder.process { -> ${new File(FILE_PATH_TEMPLATE).text}}")
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

    private initVertx() {
        defineBeans {
            "${GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN}"(VertxEventBus,'localhost',8989)
        }
    }

    void 'test onEvent'() {
        when:
        applyTemplate("<grooscript:onEvent name='nameEvent'>assert true</grooscript:onEvent>")

        then:
        1 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'clientEvents'])
        0 * _
    }

    void 'test onServerEvent'() {
        given:
        initVertx()

        when:
        applyTemplate("<grooscript:onServerEvent name='nameEvent'>assert true</grooscript:onServerEvent>")

        then:
        2 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'vertx'])
        0 * _
    }
}
