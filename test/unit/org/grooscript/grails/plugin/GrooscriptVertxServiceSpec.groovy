package org.grooscript.grails.plugin

import grails.test.mixin.TestFor
import org.grooscript.grails.plugin.GrooscriptVertxService
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(GrooscriptVertxService)
class GrooscriptVertxServiceSpec extends Specification {

	def setup() {
	}

	def cleanup() {
	}

	void "test something"() {
        expect:
        service
	}
}
