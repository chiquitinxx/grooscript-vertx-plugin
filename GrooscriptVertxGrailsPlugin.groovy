import org.grooscript.grails.plugin.VertxEventBus
import org.grooscript.GrooScript

class GrooscriptVertxGrailsPlugin {
    // the plugin version
    def version = "0.3-SNAPSHOT"
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

    def title = "Grooscript Vertx Plugin"
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

            eventBus(VertxEventBus, port, host) { bean ->
            }
        }
    }

    def initGrooscriptDaemon(application,applicationContext) {

        def source = application.config.grooscript?.source
        def destination = application.config.grooscript?.destination
        def options = application.config.grooscript?.options
        def doAfter = null

        //If there is eventbus, on file changes, we send reload
        if (applicationContext.eventBus) {

            //Only 1 listener can be up
            def afterChanges = application.config.vertx?.listener?.afterChanges
            def listenerSource = application.config.vertx?.listener?.source

            doAfter = { list ->
                if (list.size()>0) {
                    applicationContext.grailsResourceProcessor.reloadAll()
                    applicationContext.eventBus.send(VertxEventBus.CHANNEL_CHANGES,[reload:true])
                }
            }

            if (listenerSource && listenerSource instanceof List) {
                ListenerDaemon listener = new ListenerDaemon()
                listener.sourceList = listenerSource
                listener.doAfter = { list ->
                    if (afterChanges) {
                        afterChanges(list)
                    }
                    doAfter(list)
                }

                //Set the listener in eventbus
                applicationContext.eventBus.startListener(listener)
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
            println "\n${VertxEventBus.CONSOLE_MESSAGE} GrooScript daemon not started."
        }
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
        initGrooscriptDaemon(application,applicationContext)
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
        GrooScript.stopConversionDaemon()
        initGrooscriptDaemon(application,)
    }

    def onShutdown = { event ->

        GrooScript.stopConversionDaemon()

        if (applicationContext.eventBus) {
            log.info 'Closing Vert.x ...'
            applicationContext.eventBus.close()
        }

    }
}
