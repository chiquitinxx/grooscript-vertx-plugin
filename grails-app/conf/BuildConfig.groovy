grails.project.work.dir = 'target'
grails.project.source.level = 1.7

grails.project.dependency.resolution = {

    inherits("global")
    log "warn"

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        compile 'org.vert-x:vertx-lang-groovy:1.3.1.final'
        compile 'org.grooscript:grooscript:0.2.4'
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3",
              ":resources:1.2.RC2") {
            export = false
        }
    }
}
