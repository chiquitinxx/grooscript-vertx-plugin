import org.grooscript.grails.plugin.ListenerFileChangesDaemon
import org.grooscript.grails.plugin.VertxEventBus
import org.grooscript.GrooScript
import grails.util.Environment

import static org.grooscript.grails.util.Util.*

class GrooscriptVertxGrailsPlugin {
    // the plugin version
    def version = "0.3-SNAPSHOT"
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

    def title = "Grooscript Vertx Plugin"
    def author = "Jorge Franco Leza"
    def authorEmail = "grooscript@gmail.com"
    def description = '''\
Use your groovy code in your gsps thanks to GrooScript.
It converts the code to javascript and your groovy code will run in your browser.
Also use Vert.x to use events between server and gsps.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grooscript-vertx"

    def license = "APACHE"

    def organization = [ name: "Grails Community", url: "http://grails.org/" ]

    def developers = []

    def issueManagement = [ system: "GITHUB", url: "https://github.com/chiquitinxx/grooscript-vertx-plugin/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "http://github.com/chiquitinxx/grooscript-vertx-plugin/" ]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {

        def port = application.config.vertx?.eventBus?.port

        if (port) {
            def host = application.config.vertx?.eventBus?.host
            if (!host) {
                host = 'localhost'
            }

            eventBus(VertxEventBus, port, host,
                    application.config.vertx?.eventBus?.inboundPermitted?:[],
                    application.config.vertx?.eventBus?.outboundPermitted?:[],
                    application.config.vertx?.testing ? true : false)
        }
    }

    def initGrooScriptDaemon(application) {

        if (Environment.current == Environment.DEVELOPMENT) {

            def doAfter = { list ->
                if (list.size() > 0) {
                    sendReloadNotificationIfNeeded()
                }
            }

            launchFileChangesListeners(application, doAfter)

            def source = application.config.grooscript?.daemon?.source
            def destination = application.config.grooscript?.daemon?.destination
            def options = application.config.grooscript?.daemon?.options
            def doAfterOption = application.config.grooscript?.daemon?.doAfter

            //By default
            if (!options) {
                options = [classpath:'src/groovy']
            } else {
                if (!options.classpath) {
                    options.classpath = 'src/groovy'
                }
            }

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
            } else {
                consoleMessage "GrooScript daemon not started."
            }
        }
    }

    private sendReloadNotificationIfNeeded() {
        if (applicationContext.eventBus) {
            applicationContext.grailsResourceProcessor.reloadAll()
            applicationContext.eventBus.sendMessage(
                    VertxEventBus.CHANNEL_RELOAD,[reload:true])
        }
    }

    private launchFileChangesListeners(application, doAfter) {

        if (Environment.current == Environment.DEVELOPMENT) {
            launchConfigFileChangesListener(application, doAfter)
            //launchDomainFileChangesListener(application)
        }
    }

    private launchConfigFileChangesListener(application, doAfter) {

        def afterChanges = application.config.vertx?.listener?.afterChanges
        def listenerSource = application.config.vertx?.listener?.source

        if (listenerSource && listenerSource instanceof List) {
            ListenerFileChangesDaemon listener = new ListenerFileChangesDaemon()
            listener.sourceList = listenerSource
            listener.doAfter = { list ->
                if (afterChanges) {
                    afterChanges(list)
                }
                doAfter(list)
            }
            listener.start()
        }
    }

    private launchDomainFileChangesListener(application) {

        if (application.config.grooscript?.model) {
            def listModelFiles = application.config.grooscript?.model

            if (listModelFiles && listModelFiles instanceof List) {
                ListenerFileChangesDaemon listener = new ListenerFileChangesDaemon(notifyAllChanges: true)
                listener.sourceList = listModelFiles.collect { domainItem ->
                    "${DOMAIN_DIR}${SEP}${domainItem.name.replaceAll(/\./,SEP)}.groovy"
                }
                consoleMessage('Source: '+listener.sourceList)
                listener.doAfter = { list ->
                    if (list) {
                        list.each { String absolutePath ->
                            GrooScript.clearAllOptions()
                            GrooScript.setConversionProperty('customization', {
                                ast(org.grooscript.asts.DomainClass)
                            })
                            GrooScript.setOwnClassPath([GROOVY_DIR, DOMAIN_DIR])
                            try {
                                GrooScript.convert(absolutePath, DOMAIN_JS_DIR)
                                consoleMessage("Converted domain class: $absolutePath")
                            } catch (e) {
                                consoleError("Error converting $absolutePath: ${e.message}")
                            }
                        }
                        GrooScript.joinFiles(DOMAIN_JS_DIR, DOMAIN_CLASSES_JS_FILE)
                        if (list.size() != listModelFiles.size()) {
                            sendReloadNotificationIfNeeded()
                        }
                    }
                }
                listener.start()
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        initGrooScriptDaemon(application)
        if (application.config.vertx?.testing) {
            applicationContext.eventBus.onEvent('testing', { message ->
                println 'Message recieved body: ' + message.body
                message.reply([info: 'recieved'])
                applicationContext.eventBus.sendMessage(VertxEventBus.CHANNEL_RELOAD, [name: 'reloadChannel'])
                applicationContext.eventBus.sendMessage('testingIncoming', [name: 'testingIncomingChannel'])
            })
        }
    }

    /* It doesnt works property with .groovy files in src/groovy
    def onChange = { event ->
    }*/

    def onConfigChange = { event ->
        GrooScript.stopConversionDaemon()
        initGrooScriptDaemon(application)
    }

    def onShutdown = { event ->

        GrooScript.stopConversionDaemon()

        if (applicationContext.eventBus) {
            consoleMessage 'Closing Vert.x ...'
            applicationContext.eventBus.close()
        }
    }
}
