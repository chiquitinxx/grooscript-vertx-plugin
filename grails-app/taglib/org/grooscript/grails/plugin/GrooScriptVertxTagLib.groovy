package org.grooscript.grails.plugin

import groovy.transform.TypeChecked
import org.grooscript.GrooScript
import static org.grooscript.grails.util.Util.*

class GrooScriptVertxTagLib {

    static final VERTX_EVENTBUS_BEAN = 'eventBus'

    static namespace = 'grooscript'

    def grailsApplication

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
        //println 'script->'+script+'<-'
        String result = GrooScript.convert("Builder.process { -> ${script}}").trim()
        //println 'Result->'+result+'<-'

        r.require(module: 'grooscript')
        r.require(module: 'kimbo')
        r.require(module: 'grooscriptGrails')

        processTemplateEvents(attrs.reloadOn, functionName)

        out << "\n<div id='${functionName}'></div>\n"

        r.script() {
            out << "\nfunction ${functionName}() {\n"
            out << "  var code = ${result};\n"
            out << "  \$('#${functionName}').html(code.html);\n"
            out << '};\n'
            out << '$(document).ready(function() {\n'
            out << "  ${functionName}();\n"
            out << '});\n'
        }
        //println 'Out->'+out.toString()
    }

    private processTemplateEvents(listEvents, functionName) {
        if (listEvents) {
            r.require(module: 'clientEvents')
            r.script() {
                listEvents.each { nameEvent ->
                    out << "\ngrooscriptEvents.onEvent('${nameEvent}', ${functionName})\n"
                }
            }
        }
    }

    def model = { attrs ->
        if (!attrs.domainClass || !(attrs.domainClass instanceof String)) {
            log.error "GrooScriptVertxTagLib.model: have to define domainClass property as a String"
        } else {
            if (existDomainClass(attrs.domainClass)) {
                r.require(module: 'domainClasses')
            } else {
                log.error "Not exist domain class ${attrs.domainClass}"
            }
        }
    }

    private existDomainClass(String nameClass) {
        grailsApplication.domainClasses.find { it.fullName == nameClass }
    }

    private prepareDomainClassJsFile(String domainClass) {
        try {
            GrooScript.clearAllOptions()
            GrooScript.setConversionProperty('customization', {
                ast(org.grooscript.asts.DomainClass)
                //ast(TypeChecked)
            })
            GrooScript.setOwnClassPath(['src/groovy'])
            File domainFile = getDomainFile(domainClass)
            //println 'SOURCE *********************\n'+domainFile.text
            if (domainFile) {
                try {
                    GrooScript.convert(domainFile.path, DOMAIN_JS_DIR)
                } catch (e) {
                    consoleError 'GrooScriptVertxTagLib Error converting ' + e.message
                }
            } else {
                consoleError 'GrooScriptVertxTagLib Error finding domain class ' + domainClass.name
            }
        } catch (e) {
            consoleError 'GrooScriptVertxTagLib Error creating domain class js file ' + e.message
        }
    }

    private File getDomainFile(String domainClass) {
        def nameFile = "${DOMAIN_DIR}${SEP}${domainClass.replaceAll(/\./,SEP)}.groovy"
        //println 'DOMAIN CLASS FILE: ' + nameFile
        new File(nameFile)
    }
}
