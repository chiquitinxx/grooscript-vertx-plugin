package org.grooscript.grails

/**
 * User: jorgefrancoleza
 * Date: 15/10/13
 */
@grails.validation.Validateable
class InsertCommand {

    transient grooscriptVertxService

    String className
    Map data

    static constraints = {
        className nullable: false, blank: false, validator: { value, o ->
            o.grooscriptVertxService.existDomainClass(value) && o.grooscriptVertxService.canAccessDomainClass(value)
        }
        data nullable: false
    }
}
