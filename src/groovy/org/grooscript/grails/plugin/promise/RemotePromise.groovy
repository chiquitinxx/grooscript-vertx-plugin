package org.grooscript.grails.plugin.promise

import org.grooscript.grails.util.GrooscriptGrails

/**
 * Created by jorge on 24/12/13.
 */
class RemotePromise implements GsPromise {

    def domainAction
    def className
    def data
    def onSuccess
    def onFail

    def closure = { ->
        def remoteData = [domainAction: domainAction, className: className, data: data]
        GrooscriptGrails.remoteDomainAction(remoteData, onSuccess, onFail)
    }

    @Override
    def then(Closure success, Closure fail) {
        onSuccess = success
        onFail = fail
        closure.delegate = this
        closure()
    }
}
