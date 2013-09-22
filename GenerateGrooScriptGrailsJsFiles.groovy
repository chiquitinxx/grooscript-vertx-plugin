@Grab('org.grooscript:grooscript:0.3.1')

import org.grooscript.GrooScript

GrooScript.convert('src/groovy/org/grooscript/grails/util/Builder.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/plugin/ClientEventHandler.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/util/GrooscriptGrails.groovy', 'web-app/js')

new File('web-app/js/ClientEventHandler.js').text += '\nvar grooscriptEvents = ClientEventHandler();\n'

GrooScript.setConversionProperty('customization', {
   ast(org.grooscript.asts.DomainClass)
    //ast(TypeChecked)
})

new File('web-app/js/domain').mkdir()

GrooScript.convert('grails-app/domain/org/grooscript/domain', 'web-app/js/domain')
GrooScript.joinFiles('web-app/js/domain', 'web-app/js/domainClasses.js')

new File('web-app/js/domain').deleteDir()