package org.grooscript.grails

class RemoteDomainController {

    static final OK = 'OK'
    static final KO = 'KO'

    def grooscriptVertxService

    def doAction(ActionCommand command) {
        boolean validation = command.validate()
        boolean execution = (validation ? command.execute() : false)

        render(contentType:"text/json") {
            result = (validation && execution ? OK : KO)
            data = command.data ?: 'ERROR'
            listErrors = grooscriptVertxService.getErrorsForClient(command, validation, execution)
            className = command.className
        }
    }
}
