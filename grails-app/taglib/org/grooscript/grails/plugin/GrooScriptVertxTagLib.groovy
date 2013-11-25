package org.grooscript.grails.plugin

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
    def grooscriptConverter

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

    /**
     * grooscript:code
     * filePath - optional - path to the file to be converted from project dir
     */
    def code = { attrs, body ->
        def script
        if (attrs.filePath) {
            try {
                script = new File(attrs.filePath).text
                if (body()) {
                    script += '\n' + body()
                }
            } catch (e) {
                log.error "GrooScriptVertxTagLib.code error reading file('${attrs.filePath}'): ${e.message}", e
            }
        } else {
            script = body()
        }
        if (script) {
            r.require(module: 'grooscript')
            def jsCode = grooscriptConverter.toJavascript(script.toString())
            r.script() {
                out << jsCode
            }
        }
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
            String jsCode = grooscriptConverter.toJavascript("Builder.process { -> ${script}}").trim()

            r.require(module: 'grooscript')
            r.require(module: 'grooscriptGrails')

            processTemplateEvents(attrs.listenEvents, functionName)

            if (!attrs.itemSelector) {
                out << "\n<div id='${functionName}'></div>\n"
            }

            r.script() {
                out << "\nfunction ${functionName}() {\n"
                out << "  var code = ${jsCode};\n"
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

    /**
     * grooscript:model
     * domainClass - REQUIRED name of the model class
     */
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

    /**
     * Do a manual domainClasses generation
     * @param domainClass
     */
    def prepareDomainClassJsFile(String domainClass) {
        try {
            GrooScript.clearAllOptions()
            GrooScript.setConversionProperty('customization', {
                ast(org.grooscript.asts.DomainClass)
            })
            GrooScript.setConversionProperty('classPath',['src/groovy'])
            File domainFile = getDomainFile(domainClass)
            if (domainFile) {
                try {
                    GrooScript.convert(domainFile.path, DOMAIN_JS_DIR)
                } catch (e) {
                    consoleError 'GrooScriptVertxTagLib Error converting ' + e.message
                }
            } else {
                consoleError 'GrooScriptVertxTagLib Error finding domain class ' + domainClass.name
            }
            GrooScript.clearAllOptions()
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
                def jsCode = grooscriptConverter.toJavascript("{ message -> ${script}}").trim()
                jsCode = removeLastSemicolon(jsCode)

                out << "\ngrooscriptEvents.onEvent('${name}', ${jsCode});\n"
            }
        } else {
            consoleError 'GrooScriptVertxTagLib onEvent need define name property'
        }
    }

    /**
     * grooscript:onServerEvent When a vertx message arrives
     * name - name of the event
     */
    def onServerEvent = { attrs, body ->

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {

            String name = attrs.name
            if (name) {
                initVertx()
                r.require(module: 'grooscriptGrails')

                r.script() {
                    def script = body()
                    String jsCode = grooscriptConverter.toJavascript("{ message -> message = GrooscriptGrails.toGroovy(message);${script}}").trim()
                    jsCode = removeLastSemicolon(jsCode)

                    out << "\nfunction ${nextVertxOnLoadFunctionName}() {\n    ${EVENTBUS_JS_NAME}"+
                            ".registerHandler('${name}', ${jsCode})};\n"
                }
            } else {
                consoleError 'GrooScriptVertxTagLib onServerEvent need define name property'
            }
        }
    }

    /**
     * grooscript:onVertxStarted execute when Vertx started
     */
    def onVertxStarted = { attrs, body ->

        if (applicationContext.containsBean(VERTX_EVENTBUS_BEAN)) {

            initVertx()
            r.require(module: 'grooscriptGrails')

            r.script() {
                def script = body()
                String jsCode = grooscriptConverter.toJavascript("{ -> ${script}}").trim()
                jsCode = removeLastSemicolon(jsCode)
                jsCode = jsCode.replaceFirst(/function\(it\)/,"function ${nextVertxOnLoadFunctionName}()")

                out << "\n${jsCode};\n"
            }

        }
    }

    private removeLastSemicolon(String code) {
        if (code.lastIndexOf(';') >= 0) {
            return code.substring(0, code.lastIndexOf(';'))
        } else {
            return code
        }
    }
}
