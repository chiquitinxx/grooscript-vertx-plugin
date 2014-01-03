@Grab('org.grooscript:grooscript:0.4.2')

import org.grooscript.GrooScript
import org.grooscript.grails.plugin.domain.DomainClass
import org.grooscript.grails.plugin.remote.RemoteDomainClass

GrooScript.clearAllOptions()

GrooScript.convert('src/groovy/org/grooscript/grails/util/Builder.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/plugin/ClientEventHandler.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/util/GrooscriptGrails.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/plugin/promise/RemotePromise.groovy', 'web-app/js')

new File('web-app/js/ClientEventHandler.js').text += '\nvar grooscriptEvents = ClientEventHandler();\n'

//Convert domain class
GrooScript.setConversionProperty('customization', {
    ast(RemoteDomainClass)
})
GrooScript.convert('grails-app/domain/org/grooscript/domain/DomainItem.groovy', 'web-app/js/remote')

GrooScript.setConversionProperty('customization', {
    ast(DomainClass)
})
GrooScript.convert('grails-app/domain/org/grooscript/domain/DomainItem.groovy', 'web-app/js/domain')
