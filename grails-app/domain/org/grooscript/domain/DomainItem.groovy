package org.grooscript.domain

class DomainItem {

    String name
    Date date
    Integer number

    static constraints = {
        name nullable: false, maxSize: 12
        date nullable: true
        number nullable: true
    }

    static mapping = {}
}
