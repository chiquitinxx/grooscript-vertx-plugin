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

    private static final SUCCESS_ACTION = { -> 'ok' }
    private static final FAILURE_ACTION = { -> 'ko' }
    private static final NAME = 'name'
    private static final DOMAIN_CLASS_NAME = 'RemoteItem'

    def 'initial properties of remote domain class'() {
        given:
        def remoteItem = remoteDomainClassInstance

        expect:
        remoteItem.name == null
        remoteItem.classNameWithoutPackage == DOMAIN_CLASS_NAME
        remoteItem.id == null
        remoteItem.version == 0
    }

    def 'create a remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.name = NAME
        remoteItem.save().then(SUCCESS_ACTION, FAILURE_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'create', className: DOMAIN_CLASS_NAME, data: properties],
                SUCCESS_ACTION, FAILURE_ACTION)
        1 * GrooscriptGrails.getRemoteDomainClassProperties(remoteItem) >> properties
        0 * _
    }

    def 'update a remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.id = 5
        remoteItem.save().then(SUCCESS_ACTION, FAILURE_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'update', className: DOMAIN_CLASS_NAME, data: properties],
                SUCCESS_ACTION, FAILURE_ACTION)
        1 * GrooscriptGrails.getRemoteDomainClassProperties(remoteItem) >> properties
        0 * _
    }

    def 'get remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.get(1).then(SUCCESS_ACTION, FAILURE_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'read', className: DOMAIN_CLASS_NAME, data: [id: 1]], SUCCESS_ACTION, FAILURE_ACTION)
        0 * _
    }

    def 'list remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.list().then(SUCCESS_ACTION, FAILURE_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'list', className: DOMAIN_CLASS_NAME, data: [:]], SUCCESS_ACTION, FAILURE_ACTION)
        0 * _
    }

    def 'delete remote domain class'() {
        given:
        GroovySpy(GrooscriptGrails, global: true)
        def remoteItem = remoteDomainClassInstance

        when:
        remoteItem.id = 2
        remoteItem.delete().then(SUCCESS_ACTION, FAILURE_ACTION)

        then:
        1 * GrooscriptGrails.remoteDomainAction(
                [domainAction: 'delete', className: DOMAIN_CLASS_NAME, data: [id: 2]], SUCCESS_ACTION, FAILURE_ACTION)
        0 * _
    }

    private getRemoteDomainClassInstance() {
        GroovyClassLoader invoker = new GroovyClassLoader()
        def clazz = invoker.parseClass('@org.grooscript.grails.plugin.remote.RemoteDomainClass ' +
                "class ${DOMAIN_CLASS_NAME} { String name }")
        clazz.newInstance()
    }

    private getProperties() {
        [id: null, name: NAME, version: 0]
    }
}
