package org.grooscript.grails.plugin.promise

import org.grooscript.grails.plugin.GrooscriptVertxService
import org.grooscript.grails.util.GrooscriptGrails
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by jorge on 21/12/13.
 */
class RemotePromiseSpec extends Specification {

    private static final SUCCESS_ACTION = { -> 'ok' }
    private static final FAILURE_ACTION = { -> 'ko' }

    @Unroll
    def 'remote promise test'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def data = [:]
        def className = 'ClassName'
        def promise = new RemotePromise(domainAction: domainAction, className: className, data: data)

        when:
        promise.then(SUCCESS_ACTION, FAILURE_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction([domainAction: domainAction, className: className, data: data],
                SUCCESS_ACTION, FAILURE_ACTION)

        where:
        domainAction << GrooscriptVertxService.ALL_DOMAIN_ACTIONS
    }
}
