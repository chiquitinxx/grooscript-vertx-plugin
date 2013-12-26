package org.grooscript.grails.plugin

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.grooscript.domain.DomainItem
import org.grooscript.grails.ActionCommand
import org.springframework.validation.Errors
import spock.lang.Specification
import spock.lang.Unroll

import static org.grooscript.grails.plugin.GrooscriptVertxService.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(GrooscriptVertxService)
@Mock([DomainItem])
class GrooscriptVertxServiceSpec extends Specification {

    static final FAKE_NAME = 'fake domain class'
    static final GOOD_NAME = 'DomainItem'
    static final UPDATED_NAME = 'UpdaItem'
    static final CAN_CREATE = true
    static final CAN_READ = false
    static final CAN_LIST = false
    static final CAN_UPDATE = { -> true}
    static final CAN_DELETE = { -> false}

    static final VALID_DOMAIN_ITEM = [name: GOOD_NAME]
    static final VALID_DOMAIN_ITEM_UPDATE = [name: UPDATED_NAME, date: new Date(), id:1]
    static final INVALID_DOMAIN_ITEM = [name: FAKE_NAME]
    static final ERROR_DOMAIN_ITEM = [name: GOOD_NAME, date: FAKE_NAME]

    def setup() {
        service.metaClass.existShortDomainClassName = { String name ->
            name == GOOD_NAME
        }
        grailsApplication.config = [grooscript:[model:[
                [name: GOOD_NAME, create: CAN_CREATE, read: CAN_READ,
                        list: CAN_LIST, update: CAN_UPDATE, delete: CAN_DELETE]
        ]]]
	}

	def cleanup() {
	}

    @Unroll
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

    @Unroll
    def 'can access domain class'() {
        when:
        def result = service.canDoActionWithDomainClass(name, domainAction)

        then:
        result == expectedResult

        where:
        name      | expectedResult | domainAction
        null      | false          | CREATE_ACTION
        FAKE_NAME | false          | CREATE_ACTION
        GOOD_NAME | CAN_CREATE     | CREATE_ACTION
        GOOD_NAME | CAN_READ       | READ_ACTION
        GOOD_NAME | CAN_LIST       | LIST_ACTION
        GOOD_NAME | CAN_UPDATE()   | UPDATE_ACTION
        GOOD_NAME | CAN_DELETE()   | DELETE_ACTION
    }

    @Unroll
    def 'error from client'() {
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
        false        | true      | 2
        true         | false     | 1
        false        | false     | 2
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
        DomainItem.list().first().id
        DomainItem.list().first().name == VALID_DOMAIN_ITEM.name
    }

    @Unroll
    def 'error create and updating a domain class'() {
        given:
        def command = setupCommandWithDomain(CREATE_ACTION, data)

        when:
        def result = service."${domainAction}"(DomainItem.class.simpleName, command)

        then:
        !result

        where:
        domainAction  | data
        CREATE_ACTION | INVALID_DOMAIN_ITEM
        CREATE_ACTION | ERROR_DOMAIN_ITEM
    }

    def 'success update a domain class'() {
        given:
        insertDomainClass()

        when:
        def command = setupCommandWithDomain(UPDATE_ACTION, VALID_DOMAIN_ITEM_UPDATE)
        def result = service.update(command.className, command)

        then:
        result
        DomainItem.list().first().name == UPDATED_NAME
    }

    def 'success delete a domain class'() {
        given:
        insertDomainClass()

        when:
        def command = setupCommandWithDomain(DELETE_ACTION, VALID_DOMAIN_ITEM_UPDATE)
        def result = service.delete(command.className, command)

        then:
        result
        DomainItem.count == 0
    }

    def 'success read a domain class'() {
        given:
        insertDomainClass()

        when:
        def command = setupCommandWithDomain(READ_ACTION, VALID_DOMAIN_ITEM_UPDATE)
        def result = service.read(command.className, command)

        then:
        result == DomainItem.list().first()
    }

    def 'success list a domain class'() {
        given:
        insertDomainClass()

        when:
        def command = setupCommandWithDomain(LIST_ACTION, VALID_DOMAIN_ITEM_UPDATE)
        def result = service.list(command.className, command)

        then:
        result == DomainItem.list()
    }

    def setupCommandWithDomain(action, data) {
        def command = Mock(ActionCommand)
        command.className >> DomainItem.class.simpleName
        command.domainAction >> action
        command.data >> data
        command.metaClass.setDoingActionError = { String msg ->
            println '******* '+msg
        }
        //WTF have to create this mock :/ get not working
        GrooscriptVertxService.Actions.metaClass.getItemById = {
            DomainItem.list() ? DomainItem.list().first() : null
        }
        command
    }

    private insertDomainClass() {
        def command = setupCommandWithDomain(CREATE_ACTION, VALID_DOMAIN_ITEM)
        service.create(command.className, command)
    }
}
