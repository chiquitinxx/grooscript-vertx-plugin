package org.grooscript.grails.plugin

import org.springframework.transaction.annotation.Transactional

class GrooscriptVertxService {

    @Transactional(readOnly = true)
    boolean existDomainClass(String nameClass) {
        false
    }

    @Transactional(readOnly = true)
    boolean canAccessDomainClass(String nameClass) {
        false
    }
}
