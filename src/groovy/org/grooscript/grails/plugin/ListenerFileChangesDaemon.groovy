package org.grooscript.grails.plugin

import groovyx.gpars.GParsPool
import groovyx.gpars.agent.Agent
import static groovyx.gpars.dataflow.Dataflow.task
import static org.grooscript.grails.util.Util.*

/**
 * User: jorgefrancoleza
 * Date: 27/02/13
 */
class ListenerFileChangesDaemon {

    def static final REST_TIME = 500

    def sourceList
    def doAfter = null
    Map dates = [:]
    boolean continueTask = false
    def actualTask
    def notifyAllChanges = false
    def nameListener

    /**
     * Start the daemon
     * @return
     */
    def start() {
        if (sourceList) {
            continueTask = true
            actualTask = task {
                while (continueTask) {
                    def list = work()
                    if (doAfter && doAfter instanceof Closure) {
                        doAfter(list)
                    }
                    sleep(REST_TIME)
                }
            }.then( {
                consoleMessage 'Listener File Changes Terminated. ' + (nameListener ? nameListener : '')
            }, { e ->
                consoleError 'Listener ended by uncontrolled error: ' + e.message
            })
            consoleMessage 'Listener File Changes Started. ' + (nameListener ? nameListener : '')
        } else {
            consoleError 'Listener File Changes needs sourceList to run.'
        }
    }

    def stop() {
        if (actualTask) {
            continueTask = false
            actualTask.join()
            actualTask = null
        }
    }

    private work() {

        Agent agent = new Agent([])

        //Check all files and all files in dirs
        GParsPool.withPool {
            sourceList.eachParallel { name ->
                def file = new File(name)
                if (file && (file.isDirectory() || file.isFile())) {
                    if (file.isDirectory()) {
                        file.eachFile { File item ->
                            if (item.isFile()) {
                                checkFile item, agent
                            }
                        }
                    } else {
                        checkFile file, agent
                    }
                } else {
                    def message = "Listener File Changes error in file/folder ${name}"
                    consoleError message
                    throw new Exception(message)
                }
            }
        }
        //Return list of converted files
        agent.val
    }

    //Check if lastModified of file changed
    private checkFile = { File file, Agent agent ->
        def change
        //Only add if change, 1st time will be ignored
        def add = false
        if (dates."${file.absolutePath}") {
            change = !(dates."${file.absolutePath}" == file.lastModified())
            add = true
        } else {
            change = true
        }
        //println "${file.absolutePath} -O- ${file.lastModified()} Change: ${change}"
        if (change) {
            dates."${file.absolutePath}" = file.lastModified()
            if (add || notifyAllChanges) {
                agent << { it.add file.absolutePath }
            }
        }
    }
}
