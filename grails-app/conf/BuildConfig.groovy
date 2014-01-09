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
        compile 'org.codehaus.jsr166-mirror:jsr166y:1.7.0'
        compile 'org.codehaus.gpars:gpars:1.0.0'
        compile ('org.grooscript:grooscript:0.4.2') {
            exclude 'groovy'
        }
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        //test "cglib:cglib:3.0"
    }

    plugins {
        runtime ":resources:1.2.1"
        //runtime ":hibernate:$grailsVersion"

        build(//":tomcat:$grailsVersion",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }

        //test ":resources:1.2.1"
        compile ":cache:1.1.1"
        runtime ":jquery:1.10.2.2"

        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
    }
}
