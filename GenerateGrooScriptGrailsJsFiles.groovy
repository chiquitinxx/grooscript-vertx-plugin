@Grab('org.grooscript:grooscript:0.4.1')

import org.grooscript.GrooScript

GrooScript.convert('src/groovy/org/grooscript/grails/util/Builder.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/plugin/ClientEventHandler.groovy', 'web-app/js')
GrooScript.convert('src/groovy/org/grooscript/grails/util/GrooscriptGrails.groovy', 'web-app/js')

new File('web-app/js/ClientEventHandler.js').text += '\nvar grooscriptEvents = ClientEventHandler();\n'