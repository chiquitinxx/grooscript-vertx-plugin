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

    def phantomJs(params) {
        if (params.controller && params.code) {
            def url
            if (!grailsLinkGenerator.getContextPath()) {
                url = grailsLinkGenerator.getServerBaseURL() + '/grooscript-vertx/' + params.controller +
                        (params.domainAction ? "/${params.domainAction}" : '')
            } else {
                url = grailsLinkGenerator.link(params)
            }
            PhantomJsTestImpl.doPhantomJsTest(url, convertCodeToJavascript(params.code), FUNCTION_NAME)
        } else {
            assert false, 'Need define "controller" and "code" params at least'
        }
    }

    private convertCodeToJavascript(code) {
        def converter = new GsConverter()
        converter.classPath = 'src/main'
        converter.toJs("def ${FUNCTION_NAME} = { -> \n" + code + "\n}\n")
    }
}
