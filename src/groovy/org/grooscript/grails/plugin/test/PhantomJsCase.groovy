package org.grooscript.grails.plugin.test

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.grooscript.convert.GsConverter
import org.grooscript.asts.PhantomJsTestImpl
import org.springframework.beans.factory.annotation.Autowired
import static org.grooscript.grails.util.Util.*

/**
 * User: jorgefrancoleza
 * Date: 13/10/13
 */
class PhantomJsCase extends GroovyTestCase {

    private static final FUNCTION_NAME = 'gsTestFunction'

    @Autowired
    LinkGenerator grailsLinkGenerator

    //TODO not working atm, need fix in grooscript
    def phantomJs(params) {
        if (params.controller && params.code) {
            def url = grailsLinkGenerator.link(params)
            def converter = new GsConverter()
            converter.classPath = 'src/main'
            def testCode = converter.toJs("def ${FUNCTION_NAME} = { -> \n" + params.code + "\n}\n")
            if (!grailsLinkGenerator.getContextPath()) {
                url += '/grooscript-vertx'
            }
            println 'url->'+url
            println 'basr->'+grailsLinkGenerator.getServerBaseURL()
            println 'context->'+grailsLinkGenerator.getContextPath()
            println 'testCode->'+testCode
            PhantomJsTestImpl.doPhantomJsTest(url, testCode, FUNCTION_NAME)
        } else {
            consoleError 'Need define "controller" and "code" params at least'
        }
    }
}
