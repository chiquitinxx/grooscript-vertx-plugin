modules = {
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