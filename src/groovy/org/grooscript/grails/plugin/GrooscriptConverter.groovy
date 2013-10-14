package org.grooscript.grails.plugin

import grails.plugin.cache.Cacheable
import org.grooscript.GrooScript
import static org.grooscript.grails.util.Util.*

/**
 * User: jorgefrancoleza
 * Date: 22/09/13
 */
class GrooscriptConverter {

    boolean canConvertModel
    ListenerFileChangesDaemon modelChangesListener
    static final GROOVY_SOURCE_CODE = 'src/groovy'

    def stopListeners() {
        if (modelChangesListener) {
            modelChangesListener.stop()
        }
    }

    @Cacheable('conversions')
    String toJavascript(String groovyCode, options = null, cleanJsCode = true) {
        println '****************************** CONVERSION!'
        String jsCode = ''
        if (groovyCode) {
            GrooScript.clearAllOptions()
            try {
                options = addGroovySourceClassPathIfNeeded(options)
                options.each { key, value ->
                    GrooScript.setConversionProperty(key, value)
                }

                jsCode = GrooScript.convert(groovyCode)

                if (cleanJsCode) {
                    jsCode = cleanUpConvertedCode(jsCode)
                }
            } catch (e) {
                consoleError "Error converting to javascript: ${e.message}"
            }
            GrooScript.clearAllOptions()
        }
        jsCode
    }

    def addGroovySourceClassPathIfNeeded(options) {
        def conversionOptions = options ?: [:]
        if (!conversionOptions.classPath) {
            conversionOptions.classPath = []
        } else {
            if (conversionOptions.classPath instanceof String) {
                conversionOptions.classPath = [conversionOptions.classPath]
            }
        }
        if (!conversionOptions.classPath.contains(GROOVY_SOURCE_CODE)) {
            conversionOptions.classPath << GROOVY_SOURCE_CODE
        }
        conversionOptions
    }

    private cleanUpConvertedCode(String jsCode) {
        jsCode.replaceAll(/this\./,'')
    }
}
