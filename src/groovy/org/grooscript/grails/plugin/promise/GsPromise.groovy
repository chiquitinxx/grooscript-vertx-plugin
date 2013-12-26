package org.grooscript.grails.plugin.promise

/**
 * Created by jorge on 22/12/13.
 */
interface GsPromise {
    def then(Closure success, Closure fail)
}
