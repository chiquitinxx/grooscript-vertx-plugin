grooscript-vertx-plugin
=======================

Grails plugin to help develop with GrooScript and automatically reloads the page with vert.x

Install the plugin.

Add dependencies in BuildConfig.groovy

    dependencies {
        runtime 'org.vert-x:vertx-lang-groovy:1.3.1.final'
        runtime 'org.grooscript:grooscript:0.2.2'
    }

Set the vert.x port in Config.groovy (optional)

    vertx {
        eventBus {
            port = 8085
            //Host is optional, by default is localhost
            //host = 'localhost'
        }
    }

Set Groovy files to watch to be converted with the daemon. (optional)

    grooscript {
        source = ['src/groovy/Me.groovy']
        destination = 'web-app/js'
    }

Set directories / files to watch and send reload signal to browsers. (need vertx)

    savedFiles.listener = ['web-app/css']


If you are using grooscript conversion, that converted .js files, need grooscript.js

You can do this in your Config.groovy:

    grails.resources.modules = {
        myModule {
            dependsOn('grooscript')
            resource url:'/js/MyFile.js'
            ..
        }
        otherModule { ....
    }

And then use in your gsp:

    <r:require module="myModule"/>