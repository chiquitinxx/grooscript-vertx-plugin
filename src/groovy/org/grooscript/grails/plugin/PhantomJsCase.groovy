package org.grooscript.grails.plugin

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.annotation.Autowired

/**
 * User: jorgefrancoleza
 * Date: 13/10/13
 */
class PhantomJsCase extends GroovyTestCase {

    @Autowired
    LinkGenerator linkGenerator

    private String getUrlFromController(params) {
        String url = null
        if (params.controller) {
            url = linkGenerator.link(params)
        }
        return url
    }
}
