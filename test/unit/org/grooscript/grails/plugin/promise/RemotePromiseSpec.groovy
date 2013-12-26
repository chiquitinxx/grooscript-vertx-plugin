package org.grooscript.grails.plugin.promise

import org.grooscript.grails.util.GrooscriptGrails
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by jorge on 21/12/13.
 */
class RemotePromiseSpec extends Specification {

    @Unroll
    def 'remote promise test'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def data = [:]
        def className = 'ClassName'
        def domainAction = 'create'
        def promise = new RemotePromise(domainAction: domainAction, className: className, data: data)
        def onSuccess = { -> result = 'ok'}
        def onFailure = { -> result = 'ko'}

        when:
        promise.then(onSuccess, onFailure)

        then:
        1 * GrooscriptGrails.remoteDomainAction([domainAction: domainAction, className: className, data: data],
                onSuccess, onFailure)
    }
}
