import org.grooscript.grails.plugin.GrooscriptConverter
import org.grooscript.grails.plugin.ListenerFileChangesDaemon
import org.grooscript.GrooScript
import grails.util.Environment

import static org.grooscript.grails.util.Util.*

class GrooscriptVertxGrailsPlugin {
    // the plugin version
    def version = "0.4-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/controllers/**",
        "grails-app/domain/**",
        "grails-app/views/**",
        "src/Message.groovy",
        "src/groovy/MyTemplate.groovy",
        "web-app/css/**",
        "web-app/images/**",
        "web-app/js/Message.js",
        "web-app/js/domainClasses.js",
        "web-app/js/testWithNode.js",
        "web-app/js/domain/**"
    ]

    def dependsOn = [resources: "1.2.1 > *",
            cache: "1.1.1 > *"]

    def title = "Grooscript Vertx Plugin"
    def author = "Jorge Franco"
    def authorEmail = "grooscript@gmail.com"
    def description = '''\
Use your groovy code in your gsps thanks to GrooScript.
It converts the code to javascript and your groovy code will run in your browser.
Also use Vert.x to use events between server and gsps.
'''

    // URL to the plugin's documentation
    def documentation = "http://grooscript.org/pluginManual"

    def license = "APACHE"

    def organization = [ name: "Grails Community", url: "http://grails.org/" ]

    def developers = []

    def issueManagement = [ system: "GITHUB", url: "https://github.com/chiquitinxx/grooscript-vertx-plugin/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "http://github.com/chiquitinxx/grooscript-vertx-plugin/" ]

    def doWithWebDescriptor = { xml ->
    }

    static final GROOVY_VERSION_MODEL_REQUIRED = '2.1.0'
    static final JAVA_VERSION_VERTX_REQUIRED = '1.7'


    def doWithSpring = {

        def port = application.config.vertx?.eventBus?.port

        if (port) {

            def javaVersion = Class.forName('java.lang.String').package.implementationVersion
            if (javaVersion >= JAVA_VERSION_VERTX_REQUIRED) {
                def host = application.config.vertx?.eventBus?.host
                if (!host) {
                    host = 'localhost'
                }

                eventBus(org.grooscript.grails.plugin.VertxEventBus, port, host,
                        application.config.vertx?.eventBus?.inboundPermitted?:[],
                        application.config.vertx?.eventBus?.outboundPermitted?:[],
                        application.config.vertx?.testing ? true : false)
            } else {
                consoleWarning 'You need at least Java 1.7 to run Vert.x'
            }
        }

        def groovyVersion = Class.forName('groovy.lang.GString').package.implementationVersion

        grooscriptConverter(GrooscriptConverter) {
            canConvertModel = (groovyVersion >= GROOVY_VERSION_MODEL_REQUIRED)
        }
    }

    def initGrooScriptDaemon(application) {

        if (Environment.current == Environment.DEVELOPMENT) {

            def doAfter = { list ->
                if (list.size() > 0) {
                    sendReloadNotificationIfNeeded(application)
                }
            }

            launchFileChangesListeners(application, doAfter)

            def source = application.config.grooscript?.daemon?.source
            def destination = application.config.grooscript?.daemon?.destination
            def options = application.config.grooscript?.daemon?.options
            def doAfterOption = application.config.grooscript?.daemon?.doAfter

            //By default
            options = application.mainContext.grooscriptConverter.addGroovySourceClassPathIfNeeded(options)

            //Start the daemon if source and destination are ok
            if (source && destination) {
                GrooScript.clearAllOptions()
                def doAfterDaemon
                if (doAfterOption && doAfterOption instanceof Closure) {
                    doAfterDaemon = { list ->
                        doAfter(list)
                        doAfterDaemon(list)
                    }
                } else {
                    doAfterDaemon = doAfter
                }
                GrooScript.startConversionDaemon(source, destination, options, doAfterDaemon)
                GrooScript.clearAllOptions()
            } else {
                consoleMessage "GrooScript daemon not started."
            }
        }
    }

    private sendReloadNotificationIfNeeded(application) {
        if (application.mainContext.eventBus) {
            application.mainContext.grailsResourceProcessor.reloadAll()
            application.mainContext.eventBus.sendMessage(
                org.grooscript.grails.plugin.VertxEventBus.CHANNEL_RELOAD,[reload:true])
        }
    }

    private launchFileChangesListeners(application, doAfter) {

        if (Environment.current == Environment.DEVELOPMENT) {
            launchFileChangesListener(application, doAfter)
            if (application.mainContext.grooscriptConverter.canConvertModel) {
                //launchDomainFileChangesListener(application)
            } else {
                consoleWarning "You need at least Groovy ${GROOVY_VERSION_MODEL_REQUIRED} to work with the model."
            }
        }
    }

    private launchFileChangesListener(application, doAfter) {

        def afterChanges = application.config.vertx?.listener?.afterChanges
        def listenerSource = application.config.vertx?.listener?.source

        if (listenerSource && listenerSource instanceof List) {
            ListenerFileChangesDaemon listener = new ListenerFileChangesDaemon()
            listener.sourceList = listenerSource
            listener.nameListener = 'FILE_CHANGES'
            listener.doAfter = { list ->
                if (afterChanges) {
                    afterChanges(list)
                }
                doAfter(list)
            }
            listener.start()
            if (application.mainContext.eventBus) {
                application.mainContext.eventBus.fileChangesListener = listener
            }
        }
    }

    private launchDomainFileChangesListener(application) {

        if (application.config.grooscript?.model) {
            def listModelFiles = application.config.grooscript?.model
            //println '1-'+listModelFiles
            if (listModelFiles && listModelFiles instanceof List) {
                new File(DOMAIN_JS_DIR).mkdirs()
                ListenerFileChangesDaemon listener = new ListenerFileChangesDaemon(notifyAllChanges: true)
                listener.sourceList = listModelFiles.inject ([]) { listNames, domainItem ->
                    listNames << "${DOMAIN_DIR}${SEP}${domainItem.name.replaceAll(/\./,SEP)}.groovy"
                    listNames
                }
                consoleMessage('Listen domain classes: '+listener.sourceList)
                listener.nameListener = 'DOMAIN_CLASSES'
                listener.doAfter = { list ->
                    if (list) {
                        list.each { String absolutePath ->
                            GrooScript.clearAllOptions()
                            GrooScript.setConversionProperty('customization', {
                                ast(org.grooscript.asts.DomainClass)
                            })
                            GrooScript.setConversionProperty('classPath',[GROOVY_DIR, DOMAIN_DIR])
                            try {
                                GrooScript.convert(absolutePath, DOMAIN_JS_DIR)
                                consoleMessage("Converted domain class: $absolutePath")
                            } catch (e) {
                                consoleError("Error converting $absolutePath: ${e.message}")
                            }
                            GrooScript.clearAllOptions()
                        }
                        GrooScript.joinFiles(DOMAIN_JS_DIR, DOMAIN_CLASSES_JS_FILE)
                        if (list.size() != listModelFiles.size()) {
                            sendReloadNotificationIfNeeded()
                        }
                    }
                }
                listener.start()
                application.mainContext.grooscriptConverter.modelChangesListener = listener
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        initGrooScriptDaemon(application)
        if (applicationContext.eventBus && application.config.vertx?.testing) {
            applicationContext.eventBus.onEvent('testing', { message ->
                if (message) {
                    consoleMessage 'Testing message recieved body: ' + message.body
                    message.reply([info: 'recieved'])
                    applicationContext.eventBus.sendMessage(
                            org.grooscript.grails.plugin.VertxEventBus.CHANNEL_RELOAD, [name: 'reloadChannel'])
                    applicationContext.eventBus.sendMessage('testingIncoming', [name: 'testingIncomingChannel'])
                } else {
                    consoleMessage 'Empty message received.'
                }
            })
        }
    }

    /* It doesnt works property with .groovy files in src/groovy
    def onChange = { event ->
    }*/


    def onConfigChange = { event ->
        GrooScript.stopConversionDaemon()
        applicationContext.grooscriptConverter.stopListeners()
        if (applicationContext.eventBus) {
            applicationContext.eventBus.stopListeners()
        }
        initGrooScriptDaemon(application)
    }

    def onShutdown = { event ->

        GrooScript.stopConversionDaemon()
        application.grooscriptConverter.stopListeners()

        if (applicationContext.eventBus) {
            consoleMessage 'Closing Vert.x ...'
            applicationContext.eventBus.close()
        }
    }
}
