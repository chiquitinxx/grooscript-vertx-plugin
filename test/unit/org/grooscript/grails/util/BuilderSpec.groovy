package org.grooscript.grails.util

import org.grooscript.grails.plugin.domain.DomainClass
import spock.lang.Specification

/**
 * User: jorgefrancoleza
 * Date: 13/09/13
 */
class BuilderSpec extends Specification {

    static final TEXT = 'text'

    void 'process with the builder'() {
        given:
        def result = Builder.process {
            body {
                p TEXT
            }
        }

        expect:
        result.html == "<body><p>${TEXT}</p></body>"
    }

    void 'works with tag options and t function'() {
        given:
        def result = Builder.process {
            body {
                p(class:'salute') {
                    t 'hello'
                }
            }
        }

        expect:
        result.html == "<body><p class='salute'>hello</p></body>"
    }

    class MyDomainClass {
        static list() {
            [[name:'myDomainClass']]
        }
    }

    void 'works with a model class'() {
        given:
        def result = Builder.process {
            body {
                ul {
                    MyDomainClass.list().each {
                        li 'item: '+ it.name
                    }
                }
            }
        }

        expect:
        result.html == "<body><ul><li>item: myDomainClass</li></ul></body>"
    }

    @DomainClass
    class MyClass {
        String name
    }

    void 'works with a domain class'() {
        given:

        def result = Builder.process {

            def NAME = 'George'
            def myClass = new MyClass(name: NAME)
            myClass.save()

            body {
                ul {
                    MyClass.list().each { myItem ->
                        li(class: 'myClass') {
                            t 'item: '+ myItem.name
                        }
                    }
                }
            }
        }

        expect:
        result.html == "<body><ul><li class='myClass'>item: George</li></ul></body>"
    }

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
