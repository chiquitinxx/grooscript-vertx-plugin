package org.grooscript.domain

class DomainItem {

    String name
    Date date

    static constraints = {
        name nullable: false, maxSize: 12
        date nullable: true
    }

    static mapping = {}
}
