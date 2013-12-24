package org.grooscript.grails

import org.grooscript.grails.plugin.GrooscriptVertxService

import static org.grooscript.grails.util.Util.*

/**
 * User: jorgefrancoleza
 * Date: 15/10/13
 */
@grails.validation.Validateable
class ActionCommand {

    transient grooscriptVertxService

    String domainAction
    String className
    Map data
    String doingActionError

    static constraints = {
        domainAction nullable: false, blank: false, validator: { value ->
            value in GrooscriptVertxService.ALL_DOMAIN_ACTIONS
        }
        className nullable: false, blank: false, validator: { value, o ->
            o.grooscriptVertxService.existDomainClass(value) &&
                    o.grooscriptVertxService.canDoActionWithDomainClass(value, o.domainAction)
        }
        data nullable: false
        doingActionError nullable: true
    }

    def execute() {
        def result
        try {
            result = grooscriptVertxService."${domainAction}"(className, this)
        } catch (e) {
            result = false
            consoleError e.message
        }
        result
    }
}
