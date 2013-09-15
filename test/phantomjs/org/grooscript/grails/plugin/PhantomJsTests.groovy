package org.grooscript.grails.plugin

import org.grooscript.asts.PhantomJsTest

/**
 * @author Jorge Franco
 * Date: 20/08/13
 */
class PhantomJsTests extends GroovyTestCase {

    void setUp() {
    }

    @PhantomJsTest(url='http://localhost:8080/grooscript-vertx/main/test')
    void testDefaultController() {
        def title = $("title")
        assert title[0].text=='Title Test',"Title is ${title[0].text}"
        def p = $("p")
        assert p.size() == 1, 'p correct size is ' + p.size()
        assert p[0].textContent == 'Test p', "p tag is correct ${p[0].textContent}"
        assert $("li").size() == 5, 'li correct size is ' + $("li").size()
    }

    @PhantomJsTest(url='http://localhost:8080/grooscript-vertx/main/test')
    void testAddButtonAction() {
        assert $("p").size() == 1, 'Correct size is ' + $("p").size()
        assert $("h3").text() == 'Number of times: 0', 'Correct text is >' + $("h3").text() +'<'

        grooscriptEvents.sendMessage('redraw',null);

        assert $("p").size() == 2, 'Correct size is ' + $("p").size()
        assert $("h3").text() == 'Number of times: 1', 'Correct text is >' + $("h3").text() +'<'
    }
}
