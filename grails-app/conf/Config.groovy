log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error 'org.vertx'
    error 'org.grails.plugin.grooscript-vertx'

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

vertx {
    eventBus {
        port = 8086
        //host = 'localhost'
    }
    listener {
        source = ['web-app/css/style.css']
        afterChanges = { list ->
            if (list) {
                println 'Detected changes: ' + list
            }
        }
    }
    testing = true
}

grooscript {
    daemon {
        source = ['src/Message.groovy']
        destination = 'web-app/js'
    }
}

grails.resources.modules = {
    kimbo {
        resource url:'/js/kimbo.min.js'
    }
    sockjs {
        resource url:'/js/sockjs.js'
    }
    vertx {
        dependsOn 'sockjs'
        resource url:'/js/vertxbus.js'
    }
    grooscript {
        resource url:'/js/grooscript.js'
    }
    domainClasses {
        dependsOn 'grooscript'
        resource url:'/js/domainClasses.js'
    }
    grooscriptGrails {
        dependsOn 'grooscript'
        resource url:'/js/Builder.js'
        resource url:'/js/GrooscriptGrails.js'
    }
    clientEvents {
        dependsOn 'grooscriptGrails'
        resource url:'/js/ClientEventHandler.js'
    }
}

grooscript.model = [ [name: 'org.grooscript.domain.DomainItem']]

phantomjs.path = '/Applications/phantomjs'
savedFiles.listener = ['web-app/css']
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
