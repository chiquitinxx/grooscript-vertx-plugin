package org.grooscript.grails.plugin

class GrooScriptVertxTagLib {

    def static final VERTX_EVENTBUS_BEAN = 'eventBus'

    def reloadPageWithoutJsLibs = {

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {

            def eventBus = applicationContext.getBean(VERTX_EVENTBUS_BEAN)

            //out << r.require(module: 'vertx')
            //out << r.script() {
            out << r.script() {
                out << '''
                    var eb = new vertx.EventBus(\'''' + eventBus.getUrlEventBus() +'''\');

                    eb.onopen = function() {

                        console.log('Started.');

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
    }

    def reloadPage = {

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {

            def eventBus = applicationContext.getBean(VERTX_EVENTBUS_BEAN)
            out << r.require(module: 'vertx')
            out << r.script() {
                out << '''
                    var eb = new vertx.EventBus(\'''' + eventBus.getUrlEventBus() +'''\');

                    eb.onopen = function() {

                        console.log('Started.');

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
    }

}
