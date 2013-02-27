import org.grooscript.grails.plugin.ListenerDaemon
import org.grooscript.grails.plugin.VertxEventBus
import org.grooscript.GrooScript
import org.springframework.context.ApplicationContext
import org.vertx.groovy.core.Vertx

class GrooscriptVertxGrailsPlugin {
    // the plugin version
    def version = "0.2.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grooscript Vertx Plugin" // Headline display name of the plugin
    def author = "Jorge Franco Leza"
    def authorEmail = "jorge.franco.leza@gmail.com"
    def description = '''\
Starts conversion daemon to convert your groovy files to javascript.
Automatically reload pages while developing.
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
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)

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

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)

        initGrooscriptDaemon(application,applicationContext)
        //if (applicationContext.containsBean('eventBus')) {
        //  initGrooscriptDaemon(application,applicationContext.getBean('eventBus'))
        //} else {
        //    println '\n[GrooScript-Vertx] Vert.x eventbus not started.'
        //    initGrooscriptDaemon(application,null)
        //}

    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
        println 'File changed->'+event.source
        println 'Plugin->'+event.plugin
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
