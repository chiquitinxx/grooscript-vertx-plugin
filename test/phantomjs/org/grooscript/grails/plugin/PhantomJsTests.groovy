package org.grooscript.grails.plugin

import org.grooscript.asts.PhantomJsTest
import org.grooscript.grails.plugin.test.PhantomJsCase

/**
 * @author Jorge Franco
 * Date: 20/08/13
 */
class PhantomJsTests extends PhantomJsCase {

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

    @PhantomJsTest(url='http://localhost:8080/grooscript-vertx/main/templating')
    void testTemplateOptions() {
        assert $("#template1").html() != '','1->' +$ ("#template1").html() + '<'
        assert $("#template2").html() != '','2->' +$ ("#template2").html() + '<'
        assert $("#template3").html() == '','3->' +$ ("#template3").html() + '<'
    } //TODO review fails in assert dont stop test

    @PhantomJsTest(url = 'http://localhost:8080/grooscript-vertx/main/vertxEvents', waitSeconds = 2)
    void testWaitSeconds() {
        assert $('#points').html() == '.',"points Html after is ${$('#points').html()}"
    }

    void testInMainController() {
        phantomJs(controller: 'main', code: '''
            println 'New cool test!'
            assert false
        ''') //TODO review fails in assert dont stop test
        assert true
    }

    @PhantomJsTest(url = 'http://localhost:8080/grooscript-vertx/main/localEvents', waitSeconds = 3)
    void testLocalEventsWithRemoteDomainClass() {
        assert $('#console').html() == '<p>Added a new Item!</p><p>Get item with id 1</p>' +
                '<p>Update item!</p><p>Item deleted!</p>',"console Html is >${$('#console').html()}<"
    }
}
