package org.grooscript.grails.plugin

import groovyx.gpars.agent.Agent

import static groovyx.gpars.GParsPool.withPool

/**
 * User: jorgefrancoleza
 * Date: 27/02/13
 */
class ListenerDaemon {

    def sourceList
    def doAfter = null

    def dates = [:]

    /**
     * Start the daemon
     * @return
     */
    def start() {
        if (sourceList) {
            Thread.start {
                while (true) {
                    def list = work()
                    sleep(500)
                    if (doAfter) {
                        doAfter(list)
                    }
                }
                println('Listener Terminated.')
            }
            println('Listener Started.')
        } else {
            println('Listener need sourceList to run.')
        }
    }

    def work() {

        def Agent agent = new Agent([])

        //Check if lastModified of file changed
        def checkFile = { File file ->
            def change
            //Only add if change, 1st time will be ignored
            def add = false
            if (dates."${file.absolutePath}") {
                change = !(dates."${file.absolutePath}"==file.lastModified())
                add = true
            } else {
                change = true
            }
            if (change) {
                if (add) {
                    agent << { it.add file.absolutePath }
                }
                dates."${file.absolutePath}" = file.lastModified()
            }
        }

        //Check all files and all files in dirs
        withPool {
            sourceList.eachParallel { name ->
                def file = new File(name)
                if (file && (file.isDirectory() || file.isFile())) {
                    if (file.isDirectory()) {

                        file.eachFile { File item ->
                            if (item.isFile()) {
                                checkFile(item)
                            }
                        }

                    } else {
                        checkFile(file)
                    }
                } else {
                    def message = 'Listener error in file/folder '+name
                    println message
                    throw new Exception(message)
                }
            }
        }

        //List of converted files
        return agent.val
    }
}
