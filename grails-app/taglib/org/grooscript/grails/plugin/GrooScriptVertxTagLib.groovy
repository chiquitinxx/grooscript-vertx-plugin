package org.grooscript.grails.plugin

import org.grooscript.GrooScript

class GrooScriptVertxTagLib {

    static final VERTX_EVENTBUS_BEAN = 'eventBus'

    static namespace = 'grooscript'

    static final SEP = System.getProperty('file.separator')

    private putJsCode(out) {

        def eventBus = applicationContext.getBean(VERTX_EVENTBUS_BEAN)

        out << r.script() {
            out << '''
                    var eventBus = new vertx.EventBus(\'''' + eventBus.getUrlEventBus() +'''\');

                    eventBus.onopen = function() {

                        //console.log('Started.');
                        eventBus.registerHandler('reloadPage', function(message) {

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

    def template = { attrs, body ->
        def script = body()
        def functionName = 'fTemplate'+new Date().time.toString()
        def result = GrooScript.convert("{ -> ${script}}")
        //println 'Result->'+result
        r.require(module: 'kimbo')
        r.require(module: 'grooscript')

        out << "<div id='${functionName}'/>"

        r.script() {
            out << "function ${functionName}() {"
            out << "  \$('${functionName}').html(Builder.process(${result}).html);"
            out << '};'
            out << '$(document).ready(function() {'
            out << "  ${functionName}();"
            out << '});'
        }
        //println 'Out->'+out.toString()
    }

    def model = { attrs ->
        if (!attrs.domainClass) {
            log.error "GrooScriptVertxTagLib.model: have to define domainClass property"
        } else {
            def domainClass
            if (attrs.domainClass instanceof String) {
                try {
                    domainClass = Class.forName(attrs.domainClass)
                } catch (e) {
                    log.error "GrooScriptVertxTagLib.model: domainClass must exists"
                    e.printStackTrace()
                }
            } else {
                domainClass = attrs.domainClass
            }
            if (domainClass) {
                prepareDomainClassJsFile(domainClass)

            }
        }
    }

    private prepareDomainClassJsFile(Class domainClass) {
        try {
            GrooScript.clearAllOptions()
            GrooScript.setConversionProperty('customization', {
                ast(org.grooscript.asts.DomainClass)
                //ast(TypeChecked)
            })
            GrooScript.setOwnClassPath(['src/groovy'])
            File domainFile = getDomainFile(domainClass)
            println 'SOURCE *********************\n'+domainFile.text
            if (domainFile) {
                try {
                    GrooScript.convert(domainFile.path, domainJsDir)
                } catch (e) {
                    println 'GrooScriptVertxTagLib Error converting ' + e.message
                }
            } else {
                println 'GrooScriptVertxTagLib Error finding domain class ' + domainClass.name
            }
        } catch (e) {
            println 'GrooScriptVertxTagLib Error creating domain class js file ' + e.message
        }
    }

    private File getDomainFile(Class domainClass) {
        def nameFile = "grails-app${SEP}domain${SEP}${domainClass.name.replaceAll(/\./,SEP)}.groovy"
        println 'DOMAIN CLASS FILE: ' + nameFile
        new File(nameFile)
    }

    private String getDomainJsDir() {
        "web-app${SEP}js${SEP}domain"
    }
}
