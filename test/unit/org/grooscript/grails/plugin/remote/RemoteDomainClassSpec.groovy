package org.grooscript.grails.plugin.remote

import org.grooscript.grails.plugin.promise.GsPromise
import org.grooscript.grails.plugin.promise.RemotePromise
import org.grooscript.grails.util.GrooscriptGrails
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Created by jorge on 22/12/13.
 */
class RemoteDomainClassSpec extends Specification {

    private static final ON_ACTION = { -> }
    private static final NAME = 'name'

    def 'initial properties of remote domain class'() {
        given:
        def remoteItem = remoteDomainClassInstance

        expect:
        remoteItem.name == null
        remoteItem.classNameWithoutPackage == 'RemoteItem'
        remoteItem.id == null
        remoteItem.version == 0
    }

    def 'create a remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.name = NAME
        remoteItem.save().then(ON_ACTION, ON_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'create', className: 'RemoteItem', data: [id: null, name: NAME, version: 0]],
                ON_ACTION, ON_ACTION)
    }

    def 'get remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.get(1).then(ON_ACTION, ON_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'read', className: 'RemoteItem', data: [id: 1]], ON_ACTION, ON_ACTION)
    }

    private getRemoteDomainClassInstance() {
        GroovyClassLoader invoker = new GroovyClassLoader()
        def clazz = invoker.parseClass('@org.grooscript.grails.plugin.remote.RemoteDomainClass ' +
                'class RemoteItem { String name }')
        clazz.newInstance()
    }
}
