import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

// 1. add the name of your phase to this variable with this event handler
eventAllTestsStart = {
    loadPhantomJsTestType()
}

eventPackagePluginsEnd = {
    loadPhantomJsTestType()
}

loadPhantomJsTestType = {
    phasesToRun << "phantomjs"
}

def testTypeName = "phantomjs"
def testDirectory = "phantomjs"
def testMode = new GrailsTestMode(autowire: true)
def customTestType = new JUnit4GrailsTestType(testTypeName, testDirectory, testMode)

// 3. Create a «phase name»Tests variable containing the test type(s)
phantomjsTests = [customTestType]

phantomjsTestPhasePreparation = {
    def phantomJsHome = config.phantomjs?.path
    if (phantomJsHome) {
        System.setProperty('JS_LIBRARIES_PATH','web-app/js')
        System.setProperty('PHANTOMJS_HOME', config.phantomjs.path)
    }
    functionalTestPhasePreparation()
}

phantomjsTestPhaseCleanUp = {
    functionalTestPhaseCleanUp()
}