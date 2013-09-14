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
    static final TIME = ListenerFileChangesDaemon.REST_TIME
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
        def nameFile = ''
        def times = 0

        and: 'listener on that folder'
        listener.sourceList = [FOLDER_NAME]
        listener.notifyAllChanges = notifyAllChanges
        listener.doAfter = { list ->
            if (list) {
                times ++
                nameFile = list[0]
            }
        }

        when:
        listener.start()
        sleep(TIME * 2)
        changeFile()
        sleep(TIME * 2)

        then:
        nameFile == new File("${FOLDER_NAME}/${FILE_NAME}").absolutePath
        times == expectedTimes

        where:
        notifyAllChanges    |expectedTimes
        false               |1
        true                |2
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
