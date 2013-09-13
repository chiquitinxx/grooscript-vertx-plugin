package org.grooscript.grails.util

import spock.lang.Specification

/**
 * User: jorgefrancoleza
 * Date: 13/09/13
 */
class BuilderSpec extends Specification {

    def 'works basic stuff'() {
        given:
        def code = {
            html {
                head {
                    title {
                        t 'Title'
                    }
                }
                body {
                    p 'Hello World!'
                }
            }
        }

        when:
        def result = Builder.process code

        then:
        result.html == '<html><head><title>Title</title></head><body><p>Hello World!</p></body></html>'
    }
}
