package org.grooscript.grails

import org.grooscript.grails.plugin.GrooscriptVertxService

class RemoteDomainController {

    static final OK = 'OK'
    static final KO = 'KO'

    def grooscriptVertxService

    def doAction(ActionCommand command) {
        boolean validation = command.validate()
        boolean execution = (validation ? command.execute() : false)

        render(contentType:"text/json") {
            result = (validation && execution ? OK : KO)
            data = (command.domainAction == GrooscriptVertxService.LIST_ACTION ?
                    command.data.list : command.data)
            listErrors = grooscriptVertxService.getErrorsForClient(command, validation, execution)
            className = command.className
        }
    }
}
