package org.grooscript.grails.plugin

import spock.lang.Specification

/**
 * @author Jorge Franco
 * Date: 19/08/13
 */
class ListenerFileChangesDaemonSpec extends Specification {

    static final FOLDER_NAME = 'folder'
    static final FILE_NAME = 'file.txt'
    static final TEXT_BEFORE = 'text before'
    static final TEXT_AFTER = 'text after'
    static final TIME = 1000
    ListenerFileChangesDaemon listener

    def setup() {
        listener = new ListenerFileChangesDaemon()
    }

    def cleanup() {
        deleteFiles()
        listener?.stop()
    }

    void 'test listener file changes'() {
        given: 'a folder with a file'
        createFiles()
        def value = 0

        and: 'listener on that folder'
        listener.sourceList = [FOLDER_NAME]
        listener.doAfter = {
            value = 5
        }
        listener.start()

        when:
        changeFile()
        sleep(TIME)

        then:
        value == 5
    }

    private deleteFiles() {
        new File(FOLDER_NAME).deleteDir()
    }

    private createFiles() {
        new File(FOLDER_NAME).mkdirs()
        new File("${FOLDER_NAME}/${FILE_NAME}").text = TEXT_BEFORE
    }

    private changeFile() {
        new File("${FOLDER_NAME}/${FILE_NAME}").text = TEXT_AFTER
    }
}
