import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grooscript.grails.plugin.GrooscriptConverter
import org.grooscript.grails.plugin.ListenerFileChangesDaemon
import grails.util.Environment

import static org.grooscript.grails.util.Util.*

class GrooscriptVertxGrailsPlugin {
    // the plugin version
    def version = "0.4.5-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/controllers/grooscript/vertx/**",
        "grails-app/domain/**",
        "grails-app/views/**",
        "src/Message.groovy",
        "src/groovy/MyTemplate.groovy",
        "src/docs/**",
        "web-app/css/**",
        "web-app/images/**",
        "web-app/js/Message.js",
        "web-app/js/application.js",
        "web-app/js/domainClasses.js",
        "web-app/js/testWithNode.js",
        "web-app/js/domain.js",
        "web-app/js/remoteDomain.js",
        "web-app/js/remoteDomain/**",
        "web-app/js/domain/**"
    ]

    def dependsOn = [resources: "1.2.1 > *",
            cache: "1.1.1 > *", jquery: "1.10 > *"]

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
        //Convert all domain files to javascript
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
                        application.config.vertx?.eventBus?.inboundPermitted ?: [],
                        application.config.vertx?.eventBus?.outboundPermitted ?: [],
                        application.config.vertx?.testing ? true : false)
            } else {
                if (application.config.vertx) {
                    consoleWarning 'You need at least Java 1.7 to run Vert.x'
                }
            }
        }

        def groovyVersion = Class.forName('groovy.lang.GString').package.implementationVersion

        grooscriptConverter(GrooscriptConverter) {
            canConvertModel = (groovyVersion >= GROOVY_VERSION_MODEL_REQUIRED)
        }
    }

    def initGrooscriptDaemon(GrailsApplication application) {

        if (Environment.current == Environment.DEVELOPMENT) {

            def doAfterDefault = { listFiles ->
                if (listFiles.size() > 0) {
                    sendReloadNotificationIfNeeded(application)
                }
            }

            launchFileChangesListeners(application, doAfterDefault)

            def source = application.config.grooscript?.daemon?.source
            def destination = application.config.grooscript?.daemon?.destination

            //Start the daemon if source and destination are ok
            if (source && destination && application.mainContext.grooscriptConverter) {
                consoleMessage 'Starting Grooscript daemon ...'
                application.mainContext.grooscriptConverter.startDaemon()
            } else {
                consoleMessage 'Grooscript daemon not started.'
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
            if (!application.mainContext.grooscriptConverter.canConvertModel) {
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

    def doWithApplicationContext = { applicationContext ->
        initGrooscriptDaemon(application)
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
        applicationContext.grooscriptConverter.stopDaemon()
        if (applicationContext.eventBus) {
            applicationContext.eventBus.stopListeners()
        }
        //initGrooscriptDaemon(application)
    }

    def onShutdown = { event ->

        applicationContext.grooscriptConverter.stopDaemon()

        if (applicationContext.eventBus) {
            consoleMessage 'Closing Vert.x ...'
            applicationContext.eventBus.stopListeners()
            applicationContext.eventBus.close()
        }
    }
}
