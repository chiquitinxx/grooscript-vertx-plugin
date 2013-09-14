modules = {
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
        resource url:'/js/Builder.js'
    }
    clientEvents {
        dependsOn 'grooscript'
        resource url:'/js/ClientEventHandler.js'
    }
}