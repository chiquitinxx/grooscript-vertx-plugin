import org.grooscript.grails.plugin.ListenerDaemon
import org.grooscript.grails.plugin.VertxEventBus
import org.grooscript.GrooScript
import org.springframework.context.ApplicationContext
import org.vertx.groovy.core.Vertx

class GrooscriptVertxGrailsPlugin {
    // the plugin version
    def version = "0.2.5"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/controllers/**",
        "grails-app/views/**",
        "scripts/Message.groovy",
        "web-app/css/**",
        "web-app/js/Message.js"
    ]

    // TODO Fill in these fields
    def title = "Grooscript Vertx Plugin" // Headline display name of the plugin
    def author = "Jorge Franco Leza"
    def authorEmail = "grooscript@gmail.com"
    def description = '''\
Starts Grooscript conversion daemon to convert your groovy files to javascript.
Automatically reload pages while developing with Vert.x.
GrooScript info http://grooscript.org
Vert.x info http://vertx.io
More info about this plugin http://github.com/chiquitinxx/grooscript-vertx-plugin/
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grooscript-vertx"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "http://github.com/chiquitinxx/grooscript-vertx-plugin/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {

        //println '****************** doWithSpring'

        //println 'vertx ->'+application.config.vertx
        def port = application.config.vertx.eventBus.port
        //def host = application.config.vertx.eventBus.bridge.host

        if (port) {
            def host = application.config.vertx.eventBus.host
            if (!host) {
                host = 'localhost'
            }
            //println "\nVert.x Initialization Host:${host} Port:${port}"

            eventBus(VertxEventBus, port, host) { bean ->

            }
        }

    }

    def initGrooscriptDaemon(application,applicationContext) {

        def source = application.config.grooscript.source
        def destination = application.config.grooscript.destination
        def options = application.config.grooscript.options
        def doAfter = null

        //If there is eventbus, on file changes, we send reload
        if (applicationContext.eventBus) {
            doAfter = { list ->
                if (list.size()>0) {
                    //sleep(100)
                    applicationContext.grailsResourceProcessor.reloadAll()
                    applicationContext.eventBus.send(VertxEventBus.CHANNEL_CHANGES,[reload:true])
                }
            }

            if (application.config.savedFiles.listener &&
                    application.config.savedFiles.listener instanceof ArrayList) {
                ListenerDaemon listener = new ListenerDaemon()
                listener.sourceList = application.config.savedFiles.listener
                listener.doAfter = { list ->
                    if (list.size()>0) {
                        applicationContext.grailsResourceProcessor.reloadAll()
                        applicationContext.eventBus.send(VertxEventBus.CHANNEL_CHANGES,[reload:true])
                    }
                }
                listener.start()
            }
        }

        //By default
        if (!options) {
            options = [classpath:'src/groovy']
        }

        //Start the daemon if source and destination are ok
        if (source && destination) {
            GrooScript.startConversionDaemon(source,destination,options,doAfter)
        } else {
            println '\n[GrooScript-Vertx] GrooScript daemon not started.'
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def oldPort
    def oldHost
    def oldSource
    def oldDestination
    def oldSavedFilesListener

    def doWithApplicationContext = { applicationContext ->
        //println '****************** doWithApplicationContext'
        oldSource = application.config.grooscript?.source
        oldDestination = application.config.grooscript?.destination
        oldPort = application.config.vertx?.eventBus?.port
        oldHost = application.config.vertx?.eventBus?.host
        oldSavedFilesListener = application.config.savedFiles?.listener
        initGrooscriptDaemon(application,applicationContext)

    }

    def onChange = { event ->
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // The event is the same as for 'onChange'.

        if (event.plugin.title == "Grooscript Vertx Plugin") {
            //println '****************** onConfigChange'
            if (application.config.grooscript?.source != oldSource ||
                    application.config.grooscript?.destination != oldDestination ||
                    application.config.vertx?.eventBus?.port != oldPort ||
                    application.config.vertx?.eventBus?.host != oldHost ||
                    application.config.savedFiles?.listener != oldSavedFilesListener ) {
                println '*****************************************'
                println '* GrooScript or Vert.x changes detected *'
                println '*     - Must restart the server -       *'
                println '*****************************************'
            }
        }
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
        //println '****************** onShutdown'
        if (application.applicationContext.eventBus) {
            println 'Closing Vert.x ...'
            application.applicationContext.eventBus.close()
        }
        GrooScript.stopConversionDaemon()
    }
}
