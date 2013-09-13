grails.project.work.dir = 'target'
grails.project.source.level = 1.6

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
        compile 'org.grooscript:grooscript:0.3.1'
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        test "cglib:cglib:3.0"
    }

    plugins {
        runtime ":resources:1.2.RC2"

        build(":tomcat:$grailsVersion",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }

        test ":resources:1.2.RC2"

        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
    }
}
