package org.grooscript.grails.plugin

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.grooscript.domain.DomainItem
import org.grooscript.grails.ActionCommand
import org.springframework.validation.Errors
import spock.lang.Specification

import static org.grooscript.grails.plugin.GrooscriptVertxService.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(GrooscriptVertxService)
@Mock(DomainItem)
class GrooscriptVertxServiceSpec extends Specification {

    static final FAKE_NAME = 'fake domain class'
    static final GOOD_NAME = 'DomainItem'
    static final UPDATED_NAME = 'UpdaItem'
    static final CAN_CREATE = true
    static final CAN_READ = false
    static final CAN_UPDATE = { -> true}
    static final CAN_DELETE = { -> false}

    def setup() {
        service.metaClass.existShortDomainClassName = { String name ->
            name == GOOD_NAME
        }
        grailsApplication.config = [grooscript:[model:[
                [name: GOOD_NAME, create: CAN_CREATE, read: CAN_READ, update: CAN_UPDATE, delete: CAN_DELETE]
        ]]]
	}

	def cleanup() {
	}

	def 'exist domain class'() {
        when:
        def result = service.existDomainClass(name)

        then:
        result == expectedResult

        where:
        name      | expectedResult
        null      | false
        ''        | false
        FAKE_NAME | false
        GOOD_NAME | true
	}

    def 'can access domain class'() {
        when:
        def result = service.canDoActionWithDomainClass(name, action)

        then:
        result == expectedResult

        where:
        name      | expectedResult | action
        null      | false          | CREATE_ACTION
        FAKE_NAME | false          | CREATE_ACTION
        GOOD_NAME | CAN_CREATE     | CREATE_ACTION
        GOOD_NAME | CAN_READ       | READ_ACTION
        GOOD_NAME | CAN_UPDATE()   | UPDATE_ACTION
        GOOD_NAME | CAN_DELETE()   | DELETE_ACTION
    }

    def 'error for client'() {
        given:
        def command = Mock(ActionCommand)
        command.errors >> Mock(Errors)

        when:
        def result = service.getErrorsForClient(command, validationOk, executeOk)

        then:
        result.size() == expectedSize

        where:
        validationOk | executeOk | expectedSize
        true         | true      | 0
        false        | true      | 1
        true         | false     | 1
        false        | false     | 2
    }


    static final VALID_DOMAIN_ITEM = [name: GOOD_NAME]
    static final VALID_DOMAIN_ITEM_UPDATE = [name: UPDATED_NAME, date: new Date()]
    static final INVALID_DOMAIN_ITEM = [name: FAKE_NAME]
    static final ERROR_DOMAIN_ITEM = [name: GOOD_NAME, date: FAKE_NAME]

    def setupCommandWithDomain(action, data) {
        def command = Mock(ActionCommand)
        command.className >> DomainItem.class.simpleName
        command.action >> action
        command.data >> data
        command.metaClass.setDoingActionError = { String msg ->
            println '******* '+msg
        }
        command
    }

    def 'success create a domain class'() {
        given:
        def command = setupCommandWithDomain(CREATE_ACTION, VALID_DOMAIN_ITEM)

        expect:
        DomainItem.count == 0

        when:
        def result = service.create(command.className, command)

        then:
        result

        and:
        DomainItem.count == 1
        DomainItem.list()[0].id
        DomainItem.list()[0].name == VALID_DOMAIN_ITEM.name
    }

    def 'error create a domain class'() {
        given:
        def command = setupCommandWithDomain(CREATE_ACTION, data)

        when:
        def result = service.create(DomainItem.class.simpleName, command)

        then:
        !result

        where:
        data << [INVALID_DOMAIN_ITEM, ERROR_DOMAIN_ITEM]
    }

    /*
    def 'success update a domain class'() {
        given:
        def command = setupCommandWithDomain(UPDATE_ACTION, VALID_DOMAIN_ITEM)
        new DomainItem(name: GOOD_NAME).save(failOnError: true, flush: true)

        expect:
        DomainItem.get(1).name == GOOD_NAME

        when:
        command.metaClass.getData = { -> VALID_DOMAIN_ITEM_UPDATE + [id: 1] }
        def result = service.update(command.className, command)

        then:
        result

        and:
        DomainItem.count == 1
        DomainItem.list()[0].id == old(DomainItem.list()[0].id)
        DomainItem.list()[0].name == VALID_DOMAIN_ITEM_UPDATE.name
    } */
}
