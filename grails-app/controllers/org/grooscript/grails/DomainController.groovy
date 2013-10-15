package org.grooscript.grails

class DomainController {

    static final OK = 'OK'
    static final KO = 'KO'

    def insert(InsertCommand command) {
        def validation = command.validate()
        render(contentType:"text/json") {
            result = (validation ? OK : KO)
        }
    }
}
