package org.grooscript.grails.plugin

import org.grooscript.GrooScript

class GrooScriptVertxTagLib {

    def static final VERTX_EVENTBUS_BEAN = 'eventBus'

    static namespace = "grooscript"

    def private putJsCode(out) {

        def eventBus = applicationContext.getBean(VERTX_EVENTBUS_BEAN)

        out << r.script() {
            out << '''
                    var eb = new vertx.EventBus(\'''' + eventBus.getUrlEventBus() +'''\');

                    eb.onopen = function() {

                        //console.log('Started.');
                        eb.registerHandler('reloadPage', function(message) {

                            if (message.reload == true) {
                                window.location.reload(true);
                            }

                            //console.log('Got message on reloadPage: ' + JSON.stringify(message));
                        });
                    }
            '''
        }
    }

    def reloadPageWithoutJsLibs = {

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {
            putJsCode(out)
        }
    }

    def reloadPage = {

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {
            out << r.require(module: 'vertx')
            putJsCode(out)
        }
    }

    def code = { attrs, body ->
        def script = body()
        try {
            def result = GrooScript.convert(script)
            r.script() {
                out << result
            }
        } catch (e) {
            log.error "GrooScriptVertxTagLib.code: ${e.message}", e
        }
    }

}
