package org.grooscript.grails.plugin.domain

import org.grooscript.GrooScript
import spock.lang.Specification

/**
 * User: jorgefrancoleza
 * Date: 28/01/13
 */
class TestDomainClass extends Specification {

    private static final NAME = 'name'
    private static final OTHER_NAME = 'other_name'
    private static final VALUE = 'value'
    private static final FAKE_ID = -3464356

    private static final CLASS_NAME = 'org.grooscript.grails.plugin.domain.TestDomainClass$AstItem'

    @DomainClass class AstItem {
        String name
        Integer number

        static constraints = {
            name example:true
        }

        boolean equals(Object o) {
            o instanceof AstItem && this.name == o.name && this.number == o.number
        }
    }

    @DomainClass class AstItemWithBlankValidation {
        String name
        Integer number

        static constraints = {
            name blank:false
        }
    }

    def cleanup() {
        AstItem.lastId = 0
        AstItem.listItems = []
        GrooScript.clearAllOptions()
    }

    def 'test ast for domain classes'() {
        given:
        def item = new AstItem()

        expect:
        //println item.properties
        item.properties.containsKey('id')
        AstItem.listItems == []
        AstItem.lastId == 0
        AstItem.listColumns.size() == 2
        AstItem.listColumns.find{it.name==NAME}.name == NAME
        AstItem.listColumns.find{it.name==NAME}.type == 'java.lang.String'
        AstItem.listColumns.find{it.name==NAME}.constraints == [example:true]
        AstItem.listColumns.find{it.name=='number'}.name == 'number'
        AstItem.listColumns.find{it.name=='number'}.type == 'java.lang.Integer'
        AstItem.listColumns.find{it.name=='number'}.constraints == [:]
        item.metaClass.methods.find { it.name=='save'}
        item.metaClass.methods.find { it.name=='delete'}
        item.save()
    }

    def 'test get method'() {
        given:
        basicItem
        def item = AstItem.get(1)

        expect:
        item.id
        !AstItem.get(FAKE_ID)
    }

    def 'test get method obtains a cloned object'() {
        given:
        basicItem
        def item = AstItem.get(1)
        def item2 = AstItem.list()[0]

        when:
        item2.name = OTHER_NAME

        then:
        item.id == item2.id
        item != item2
    }

    def 'test create new item'() {
        given:
        AstItem.count() == 0
        def item = new AstItem()

        expect:
        !item.id
        AstItem.lastId == 0

        when:
        item."${NAME}" = VALUE
        def result = item.save()

        then:
        result == true
        item.id == 1
        AstItem.count() == 1
        AstItem.listItems.size() == 1
        AstItem.listItems[0]."${NAME}" == VALUE
        AstItem.lastId == 1
    }

    def 'test update an item'() {
        given:
        def item = basicItem

        expect:
        item.name == NAME

        when:
        item.name = VALUE
        item.save()

        then:
        AstItem.get(item.id).name == VALUE
        AstItem.list()[0] == item
    }

    def 'test delete an item'() {
        given:
        AstItem item = getBasicItem()

        expect:
        AstItem.count() == 1

        when:
        item.delete()

        then:
        AstItem.count() == 0
        !AstItem.list()
        !AstItem.listItems
    }

    def 'test change listener executed'() {
        given:
        def item = new AstItem()
        def value = 15
        item.changeListeners << { it -> println it; value = value * 2}

        when:
        item."$NAME" = VALUE
        item.save()

        then:
        value == 30
    }

    def 'test blank validation'() {
        given:
        AstItemWithBlankValidation.count() == 0
        AstItemWithBlankValidation item = new AstItemWithBlankValidation()

        expect:
        !item.clientValidations()
        !item.validate()

        and:
        item.hasErrors()
        item.errors == [name:'blank validation on value null']

        when:
        def result = item.save()

        then:
        !result
        AstItemWithBlankValidation.count() == 0
    }

    private getBasicItem() {
        def item = new AstItem(name: NAME)
        item.save()
        item
    }
}
