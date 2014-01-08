package org.grooscript.grails.plugin

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugin.resource.ResourceTagLib
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Jorge Franco
 * Date: 19/06/13
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(GrooScriptVertxTagLib)
class GrooScriptVertxTagLibSpec extends Specification {

    def resourceTaglib
    def grooscriptConverter

    void setup() {
        resourceTaglib = Mock(ResourceTagLib)
        grooscriptConverter = Mock(GrooscriptConverter)
        GrooScriptVertxTagLib.metaClass.r = resourceTaglib
        GrooScriptVertxTagLib.metaClass.grooscriptConverter = grooscriptConverter
    }

    void cleanup() {
        try {
            applicationContext.getBean(GrooScriptVertxTagLib.VERTX_EVENTBUS_BEAN)?.close()
        } catch (e) {

        }
    }

    static final CODE = 'code example'

    void 'test code taglib'() {
        when: 'applying grooscript code taglib'
        applyTemplate("<grooscript:code>${CODE}</grooscript:code>")

        then: 'call convert and script'
        1 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'grooscript'])
        1 * grooscriptConverter.toJavascript(_)
    }

    static final FILE_PATH = 'src/groovy/org/grooscript/grails/util/Builder.groovy'

    void 'test code taglib with a file'() {
        when:
        applyTemplate("<grooscript:code filePath='${FILE_PATH}'/>")

        then:
        1 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'grooscript'])
        1 * grooscriptConverter.toJavascript(new File(FILE_PATH).text)
    }

    void 'test code taglib with a file and body'() {
        when:
        applyTemplate("<grooscript:code filePath='${FILE_PATH}'>${CODE}</grooscript:code>")

        then:
        1 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'grooscript'])
        1 * grooscriptConverter.toJavascript(new File(FILE_PATH).text+'\n'+CODE)
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
        1 * resourceTaglib.require([module: 'vertx'])
        2 * resourceTaglib.script(_)
        0 * _
    }

    void 'test template'() {
        when:
        def result = applyTemplate("<grooscript:template>assert true</grooscript:template>")

        then:
        2 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'grooscript'])
        1 * resourceTaglib.require([module: 'grooscriptGrails'])
        1 * grooscriptConverter.toJavascript('Builder.process { data = [:] -> assert true}') >> ''
        0 * _
        result.startsWith "\n<div id='fTemplate"
    }

    void 'very basic test template options'() {
        when:
        def result = applyTemplate("<grooscript:template functionName='jarJar'" +
                " itemSelector='#anyId' renderOnReady=\"${true}\">assert true</grooscript:template>")

        then:
        2 * resourceTaglib.script(_)
        1 * grooscriptConverter.toJavascript(_) >> ''
        2 * resourceTaglib.require(_)
        0 * _
        !result
    }

    static final FILE_PATH_TEMPLATE = 'src/groovy/MyTemplate.groovy'

    void 'test template with a file'() {
        when:
        def result = applyTemplate("<grooscript:template filePath='${FILE_PATH_TEMPLATE}'/>")

        then:
        2 * resourceTaglib.script(_)
        1 * grooscriptConverter.toJavascript("Builder.process { data = [:] -> ${new File(FILE_PATH_TEMPLATE).text}}") >> ''
        2 * resourceTaglib.require(_)
        0 * _
        result.startsWith "\n<div id='fTemplate"
    }

    void 'test template with a reload event'() {
        when:
        applyTemplate('<grooscript:template listenEvents="$events">h3 \'Hello!\'</grooscript:template>',
                [events: ['redraw']])

        then:
        3 * resourceTaglib.script(_)
        2 * resourceTaglib.require(_)
        1 * grooscriptConverter.toJavascript(_) >> ''
        1 * resourceTaglib.require([module: 'clientEvents'])
        0 * _
    }

    static final FAKE_NAME = 'FAKE'
    static final DOMAIN_CLASS_NAME = 'correctDomainClass'
    static final DOMAIN_CLASS_NAME_WITH_PACKAGE = 'org.grooscript.correctDomainClass'

    @Unroll
    void 'test model with domain class'() {
        given:
        GrooScriptVertxTagLib.metaClass.existDomainClass = { String name ->
            name != FAKE_NAME
        }

        when:
        applyTemplate("<grooscript:model domainClass='${domainClassName}'/>")

        then:
        numberTimes * resourceTaglib.require([module: 'domain'])
        numberTimes * grooscriptConverter.convertDomainClass(domainClassName)
        0 * _

        where:
        domainClassName                | numberTimes
        FAKE_NAME                      | 0
        DOMAIN_CLASS_NAME              | 1
        DOMAIN_CLASS_NAME_WITH_PACKAGE | 1
    }

    @Unroll
    void 'test remote model with domain class'() {
        given:
        GrooScriptVertxTagLib.metaClass.existDomainClass = { String name ->
            name != FAKE_NAME
        }

        when:
        applyTemplate("<grooscript:remoteModel domainClass='${domainClassName}'/>")

        then:
        numberTimes * resourceTaglib.require([module: 'grooscriptGrails'])
        numberTimes * resourceTaglib.require([module: 'remoteDomain'])
        numberTimes * resourceTaglib.script(_)
        numberTimes * grooscriptConverter.convertDomainClass(domainClassName, true)
        0 * _

        where:
        domainClassName                | numberTimes
        FAKE_NAME                      | 0
        DOMAIN_CLASS_NAME              | 1
        DOMAIN_CLASS_NAME_WITH_PACKAGE | 1
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
        2 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'clientEvents'])
        1 * resourceTaglib.require([module: 'grooscriptGrails'])
        0 * _
    }

    void 'test onServerEvent'() {
        given:
        initVertx()

        when:
        applyTemplate("<grooscript:onServerEvent name='nameEvent'>assert true</grooscript:onServerEvent>")

        then:
        3 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'grooscriptGrails'])
        1 * resourceTaglib.require([module: 'vertx'])
        0 * _
    }

    void 'test onVertxStarted'() {
        given:
        initVertx()

        when:
        applyTemplate("<grooscript:onVertxStarted>assert true</grooscript:onVertxStarted>")

        then:
        3 * resourceTaglib.script(_)
        1 * resourceTaglib.require([module: 'grooscriptGrails'])
        1 * resourceTaglib.require([module: 'vertx'])
        0 * _
    }
}
