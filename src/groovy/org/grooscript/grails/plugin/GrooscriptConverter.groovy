package org.grooscript.grails.plugin

import grails.plugin.cache.Cacheable
import org.grooscript.GrooScript
import org.grooscript.daemon.ConversionDaemon
import org.grooscript.grails.plugin.domain.DomainClass
import org.grooscript.grails.plugin.remote.RemoteDomainClass

import static org.grooscript.grails.util.Util.*
import static grails.util.Holders.getGrailsApplication

/**
 * User: jorgefrancoleza
 * Date: 22/09/13
 */
class GrooscriptConverter {

    boolean canConvertModel
    static final GROOVY_SOURCE_CODE = 'src/groovy'
    ConversionDaemon conversionDaemon

    @Cacheable('conversions')
    String toJavascript(String groovyCode, options = null) {
        String jsCode = ''
        if (groovyCode) {
            GrooScript.clearAllOptions()
            try {
                options = addGroovySourceClassPathIfNeeded(options)
                options.each { key, value ->
                    GrooScript.setConversionProperty(key, value)
                }

                jsCode = GrooScript.convert(groovyCode)

            } catch (e) {
                consoleError "Error converting to javascript: ${e.message}"
            }
            GrooScript.clearAllOptions()
        }
        jsCode
    }

    def convertDomainClass(String domainClassName, boolean remote = false) {
        if (canConvertModel) {
            convertDomainClassFile(domainClassName, remote)
        } else {
            consoleWarning "Can't convert model classes. Need at least Groovy 2.1.0"
        }
    }

    String getDomainFilePath(String domainClass) {
        def nameFilePath
        def result = grailsApplication.domainClasses.find { it.fullName == domainClass || it.name == domainClass }
        if (result) {
            nameFilePath = "${DOMAIN_DIR}${SEP}${getPathFromClassName(result.clazz.canonicalName)}"
        }
        nameFilePath
    }

    void startDaemon() {

        if (conversionDaemon) {
            stopDaemon()
        }

        def config = grailsApplication.config

        def source = config.grooscript?.daemon?.source
        def destination = config.grooscript?.daemon?.destination

        //By default
        def options = config.grooscript?.daemon?.options
        options = addGroovySourceClassPathIfNeeded(options)

        conversionDaemon = GrooScript.startConversionDaemon(source, destination, options, closureToRunAfterDaemonConversion)
    }

    void stopDaemon() {
        if (conversionDaemon) {
            conversionDaemon.stop()
        }
    }

    private Closure getClosureToRunAfterDaemonConversion() {

        //Send reload notifications if needed
        def doAfterDefault = { listFiles ->
            if (listFiles.size() > 0) {
                sendReloadNotificationIfNeeded(grailsApplication)
            }
        }

        //Config option to do
        def doAfterConfig = grailsApplication.config.grooscript?.daemon?.doAfter

        //Full action to do after some change
        Closure doAfterDaemon
        if (doAfterConfig && doAfterConfig instanceof Closure) {
            doAfterDaemon = { listFilesList ->
                doAfterDefault(listFilesList)
                doAfterConfig(listFilesList)
            }
        } else {
            doAfterDaemon = doAfterDefault
        }
        doAfterDaemon
    }

    private convertDomainClassFile(String domainClassName, boolean remote) {
        try {
            String domainFilePath = getDomainFilePath(domainClassName)
            if (domainFilePath) {
                try {
                    GrooScript.clearAllOptions()
                    if (remote) {
                        GrooScript.setConversionProperty('customization', {
                            ast(RemoteDomainClass)
                        })
                    } else {
                        GrooScript.setConversionProperty('customization', {
                            ast(DomainClass)
                        })
                    }
                    GrooScript.setConversionProperty('classPath', [GROOVY_SOURCE_CODE])
                    def targetDirectory = remote ? REMOTE_JS_DIR : DOMAIN_JS_DIR
                    GrooScript.convert(domainFilePath, targetDirectory)
                    GrooScript.joinFiles(targetDirectory, JS_DIR + SEP + (remote ? REMOTE_NAME : DOMAIN_NAME) + '.js')
                } catch (e) {
                    consoleError 'GrooscriptConverter Error converting ' + e.message
                }
            } else {
                //TODO delete this
                consoleWarning 'Domain file not found ' + domainClassName
            }
        } catch (e) {
            consoleError 'GrooscriptConverter Error creating domain class js file ' + e.message
        }
        GrooScript.clearAllOptions()
    }

    private addGroovySourceClassPathIfNeeded(options) {
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

    private getPathFromClassName(String className) {
        "${className.replaceAll(/\./,SEP)}.groovy"
    }

    private sendReloadNotificationIfNeeded(application) {
        if (grailsApplication.mainContext.eventBus) {
            grailsApplication.mainContext.grailsResourceProcessor.reloadAll()
            grailsApplication.mainContext.eventBus.sendMessage(
                    org.grooscript.grails.plugin.VertxEventBus.CHANNEL_RELOAD, [reload:true])
        }
    }
}
