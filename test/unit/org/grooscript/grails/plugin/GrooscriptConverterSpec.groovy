package org.grooscript.grails.plugin

import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grooscript.GrooScript
import org.grooscript.daemon.ConversionDaemon
import spock.lang.Specification
import spock.lang.Unroll

/**
 * User: jorgefrancoleza
 * Date: 01/01/14
 */
class GrooscriptConverterSpec extends Specification {

    private static final CODE = 'def a = 5; b.go()'
    private static final FILE_PATH = 'path'
    def grooscriptConverter = new GrooscriptConverter()
    def grailsApplication = Mock(GrailsApplication)

    def setup() {
        GroovySpy(GrooScript, global: true)
        GroovySpy(Holders, global: true)
    }

    def 'convert to javascript'() {
        given:
        def code = CODE

        when:
        def result = grooscriptConverter.toJavascript(code, null)

        then:
        2 * GrooScript.clearAllOptions()
        1 * GrooScript.setConversionProperty('classPath', ['src/groovy'])
        1 * GrooScript.convert(CODE)
        1 * GrooScript.getNewConverter()
        0 * _
        result == 'var a = 5;\ngs.mc(b,"go",gs.list([]));\n'
    }

    def 'convert domain class'() {
        given:
        grooscriptConverter.canConvertModel = true
        grooscriptConverter.metaClass.getDomainFilePath = { String domainClass ->
            domainClass == domainclassName ? FILE_PATH : null
        }

        when:
        grooscriptConverter.convertDomainClass(domainclassName)

        then:
        2 * GrooScript.clearAllOptions()
        1 * GrooScript.setConversionProperty('customization', _)
        1 * GrooScript.setConversionProperty('classPath', ['src/groovy'])
        1 * GrooScript.convert(FILE_PATH, 'web-app/js/domain') >> true
        1 * GrooScript.joinFiles('web-app/js/domain', 'web-app/js/domain.js')
        0 * _

        where:
        domainclassName << ['DomainItem']
    }

    @Unroll
    def 'test get domain file path'() {
        given:
        def domainClass = 'myDomain'
        grailsApplication.metaClass.domainClasses = [[fullName: fullName, name: name, clazz: [canonicalName: 'canonical']]]

        when:
        def result = grooscriptConverter.getDomainFilePath(domainClass)

        then:
        1 * Holders.getGrailsApplication() >> grailsApplication
        0 * _
        result == expectedResult

        where:
        fullName   | name       | expectedResult
        'myDomain' | _          | 'grails-app/domain/canonical.groovy'
        _          | 'myDomain' | 'grails-app/domain/canonical.groovy'
        ''         | ''         | null
    }

    def 'test start daemon'() {
        given:
        def conversionOptions = [:]
        def doAfter = { -> 6 }
        def daemon = new ConversionDaemon()
        grooscriptConverter.metaClass.getClosureToRunAfterDaemonConversion = doAfter

        when:
        grooscriptConverter.startDaemon()

        then:
        1 * GrooScript.startConversionDaemon('source', 'destination',
                ['classPath':['src/groovy']], 6) >> daemon
        1 * Holders.getGrailsApplication() >> grailsApplication
        1 * grailsApplication.getConfig() >> [grooscript:[daemon:
              [source: 'source', destination: 'destination', doAfter: doAfter, options: conversionOptions]]]
        0 * _
        grooscriptConverter.conversionDaemon == daemon
    }
}
