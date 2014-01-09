package org.grooscript.grails.plugin

import org.grooscript.GrooScript
import org.grooscript.grails.util.Util
import spock.lang.Specification

/**
 * User: jorgefrancoleza
 * Date: 01/01/14
 */
class GrooscriptConverterSpec extends Specification {

    private static final CODE = 'def a = 5; b.go()'
    private static final FILE_PATH = 'path'
    def grooscriptConverter = new GrooscriptConverter()

    def setup() {
        GroovySpy(GrooScript, global: true)
        GroovySpy(Util, global: true)
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
        1 * Util.getDOMAIN_JS_DIR()
        1 * Util.getJS_DIR()
        1 * Util.getSEP()
        1 * Util.getDOMAIN_NAME()
        1 * GrooScript.convert(FILE_PATH, 'web-app/js/domain') >> true
        1 * GrooScript.joinFiles('web-app/js/domain', 'web-app/js/domain.js')
        0 * _

        where:
        domainclassName << ['DomainItem']
    }
}
