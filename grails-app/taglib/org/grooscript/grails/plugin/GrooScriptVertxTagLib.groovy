package org.grooscript.grails.plugin

import groovy.transform.TypeChecked
import org.grooscript.GrooScript
import static org.grooscript.grails.util.Util.*

class GrooScriptVertxTagLib {

    static final VERTX_EVENTBUS_BEAN = 'eventBus'
    static final EVENTBUS_JS_NAME = 'grooscriptEventBus'
    static final REQUEST_VERTX_STARTED = 'grooscriptVertxStarted'
    static final REQUEST_VERTX_NEXT_ONLOAD = 'grooscriptVertxNextOnLoad'
    static final VERTX_ONLOAD_FUNCTION_PREFIX = 'grooscriptVertxOnLoad'

    static namespace = 'grooscript'

    def grailsApplication

    def getNextVertxOnLoadEvent() {
        def value = request.getAttribute(REQUEST_VERTX_NEXT_ONLOAD)
        request.setAttribute(REQUEST_VERTX_NEXT_ONLOAD, (value ? value + 1 : 1) )
        value ?: 0
    }

    private putReloadPageJsCode() {
        r.script() {
            out << "\nfunction ${nextVertxOnLoadFunctionName}() {\n    ${EVENTBUS_JS_NAME}"+
'''.registerHandler(\''''+VertxEventBus.CHANNEL_RELOAD+'''\', function(message) {

        if (message.reload == true) {
            window.location.reload(true);
        }
        console.log('Got message on reloadPage: ' + JSON.stringify(message));
    });
};\n'''
        }
    }

    private getNextVertxOnLoadFunctionName() {
        "${VERTX_ONLOAD_FUNCTION_PREFIX}${nextVertxOnLoadEvent}"
    }

    def getVertxStarted() {
        def value = request.getAttribute(REQUEST_VERTX_STARTED)
        value != null
    }

    def setVertxStarted(value) {
        request.setAttribute(REQUEST_VERTX_STARTED, value)
    }

    def initVertx = {

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {

            def eventBus = applicationContext.getBean(VERTX_EVENTBUS_BEAN)

            if (!vertxStarted) {
                r.require(module: 'vertx')
                r.script() {
                    out << "\nvar ${EVENTBUS_JS_NAME} = new vertx.EventBus('${eventBus.getUrlEventBus()}');\n"
                    out << "grooscriptEventBus.onopen = function() {\n"
                    out << "    var stop = false, actualNumber = 0;\n"
                    out << "    while (!stop) {\n"
                    out << "        try {\n"
                    out << "            var onLoadFunction = eval('grooscriptVertxOnLoad'+ actualNumber++);\n"
                    out << "            if (onLoadFunction != undefined && typeof onLoadFunction === \"function\") {\n"
                    out << "                onLoadFunction();\n"
                    out << "            } else {\n"
                    out << "                stop = true;\n"
                    out << "            }\n"
                    out << "        } catch (e) { stop = true; }\n"
                    out << "    }\n"
                    out << "}\n"
                }
            }
            vertxStarted = true
        }
    }

    def reloadPage = {

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {
            initVertx()
            putReloadPageJsCode()
        }
    }

    def code = { attrs, body ->
        def script
        if (attrs.filePath) {
            try {
                script = new File(attrs.filePath).text
            } catch (e) {
                log.error "GrooScriptVertxTagLib.code error reading file('${attrs.filePath}'): ${e.message}", e
            }
        } else {
            script = body()
        }
        if (script) {
            try {
                def result = GrooScript.convert(script)
                r.script() {
                    out << cleanUpConvertedCode(result)
                }
            } catch (e) {
                log.error "GrooScriptVertxTagLib.code error converting: ${e.message}", e
            }
        }
    }

    private cleanUpConvertedCode(String jsCode) {
        jsCode.replaceAll(/this\./,'')
    }

    /**
     * grooscript: template
     *
     * filePath - optional - path to file to be used as template, from project dir
     * functionName - optional - name of the function that renders the template
     * itemSelector - optional - jQuery string selector where html generated will be placed
     * renderOnReady - optional defaults true - if template will be render onReady page event
     * listenEvents - optional - string list of events that render the page
     */
    def template = { attrs, body ->
        def script
        if (attrs.filePath) {
            try {
                script = new File(attrs.filePath).text
            } catch (e) {
                log.error "GrooScriptVertxTagLib.template error reading file('${attrs.filePath}'): ${e.message}", e
            }
        } else {
            script = body()
        }
        if (script) {
            def functionName = attrs.functionName ?: 'fTemplate'+new Date().time.toString()
            String result = GrooScript.convert("Builder.process { -> ${script}}").trim()

            r.require(module: 'grooscript')
            r.require(module: 'kimbo')
            r.require(module: 'grooscriptGrails')

            processTemplateEvents(attrs.listenEvents, functionName)

            if (!attrs.itemSelector) {
                out << "\n<div id='${functionName}'></div>\n"
            }

            r.script() {
                out << "\nfunction ${functionName}() {\n"
                out << "  var code = ${cleanUpConvertedCode(result)};\n"
                out << "  \$('" + (attrs.itemSelector ? attrs.itemSelector : "#${functionName}") + "').html(code.html);\n"
                out << '};\n'
                if (!attrs.renderOnReady) {
                    out << '$(document).ready(function() {\n'
                    out << "  ${functionName}();\n"
                    out << '});\n'
                }
            }
        }
    }

    private processTemplateEvents(listEvents, functionName) {
        if (listEvents) {
            r.require(module: 'clientEvents')
            r.script() {
                listEvents.each { nameEvent ->
                    out << "\ngrooscriptEvents.onEvent('${nameEvent}', ${functionName});\n"
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

    private getPathFromClassName(String className) {
        "${className.replaceAll(/\./,SEP)}.groovy"
    }

    private File getDomainFile(String domainClass) {
        def nameFile = "${DOMAIN_DIR}${SEP}${getPathFromClassName(domainClass)}"
        //println 'DOMAIN CLASS FILE: ' + nameFile
        new File(nameFile)
    }

    /**
     * grooscript:onEvent
     * name - name of the event
     */
    def onEvent = { attrs, body ->
        String name = attrs.name
        if (name) {
            r.require(module: 'clientEvents')

            r.script() {
                def script = body()
                def result = GrooScript.convert("{ message -> ${script}}").trim()
                result = removeLastSemicolon(result)

                out << "\ngrooscriptEvents.onEvent('${name}', ${cleanUpConvertedCode(result)});\n"
            }
        } else {
            consoleError 'GrooScriptVertxTagLib onEvent need define name property'
        }
    }

    /**
     * grooscript:onServerEvent
     * name - name of the event
     */
    def onServerEvent = { attrs, body ->

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {

            String name = attrs.name
            if (name) {
                initVertx()

                r.script() {
                    def script = body()
                    String result = GrooScript.convert("{ message -> ${script}}").trim()
                    result = removeLastSemicolon(result)

                    out << "\nfunction ${nextVertxOnLoadFunctionName}() {\n    ${EVENTBUS_JS_NAME}"+
                            ".registerHandler('${name}', ${cleanUpConvertedCode(result)})};\n"
                }
            } else {
                consoleError 'GrooScriptVertxTagLib onServerEvent need define name property'
            }
        }
    }

    private removeLastSemicolon(String code) {
        code.substring(0, code.lastIndexOf(';'))
    }
}
