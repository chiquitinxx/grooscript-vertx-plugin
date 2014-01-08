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
    grooscriptGrails {
        dependsOn 'grooscript'
        resource url:'/js/Builder.js'
        resource url:'/js/GrooscriptGrails.js'
        resource url:'/js/RemotePromise.js'
    }
    domain {
        dependsOn 'grooscript'
        resource url:'/js/domain.js'
    }
    remoteDomain {
        dependsOn 'grooscriptGrails'
        resource url:'/js/remoteDomain.js'
    }
    clientEvents {
        dependsOn 'grooscriptGrails'
        resource url:'/js/ClientEventHandler.js'
    }
}