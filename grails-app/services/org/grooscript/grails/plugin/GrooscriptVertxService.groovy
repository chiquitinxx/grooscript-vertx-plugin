package org.grooscript.grails.plugin

import org.grooscript.grails.ActionCommand
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class GrooscriptVertxService {

    def grailsApplication

    static final CREATE_ACTION = 'create'
    static final READ_ACTION = 'read'
    static final UPDATE_ACTION = 'update'
    static final DELETE_ACTION = 'delete'
    static final ALL_DOMAIN_ACTIONS = [CREATE_ACTION, READ_ACTION, UPDATE_ACTION, DELETE_ACTION]

    private static final NOT_ALLOWED_PROPERTIES = ['id', 'version']

    boolean existDomainClass(String nameClass) {
        nameClass && existShortDomainClassName(nameClass)
    }

    boolean canDoActionWithDomainClass(String nameClass, action) {
        boolean result = false
        if (nameClass) {
            Map mapOfClassName = getMapOfClassName(nameClass)
            if (mapOfClassName) {
                def evaluation = mapOfClassName[action]
                if (evaluation != null) {
                    if (evaluation instanceof Closure) {
                        evaluation.delegate = this
                        result = evaluation()
                    } else {
                        result = evaluation as boolean
                    }
                }
            }
        }
        result
    }

    private getMapOfClassName(nameClass) {
        grailsApplication.config.grooscript?.model?.find { it -> it.name == nameClass}
    }

    private boolean existShortDomainClassName(String nameClass) {
        getDomainClass(nameClass) as boolean
    }

    List getErrorsForClient(command, boolean validationOk, boolean executeOk) {
        def result = []
        if (!validationOk) {
            result.add 'Error validation in '+command.domainAction+' '+command.className
            result.addAll command.errors
        } else {
            if (!executeOk) {
                result.add "Error doing domainAction ${command.domainAction}: ${command.doingActionError}"
            }
        }
        result
    }

    private getDomainClass(String nameClass) {
        grailsApplication.domainClasses.find {
            it.shortName == nameClass
        }
    }

    class Actions {
        def domainClassName
        def domainClass
        def actionCommand
        def result = false

        def passParametersToItem(item) {
            actionCommand.data.findAll { key, value -> !(key in NOT_ALLOWED_PROPERTIES) }.
                each { key, value ->
                    item."${key}" = value
                }
        }
        def passItemToMap(item, map) {
            map.id = item.id
        }
        def validateAndSave(item) {
            if (!item.validate()) {
                actionCommand.doingActionError =
                    "Error validating ${domainClassName} ${actionCommand.data.toString()}"
            } else {
                item.save(failOnError: true)
                result = true
            }
        }
        def getItemById() {
            domainClass?.referenceInstance.get(actionCommand?.data?.id as Long)
        }
        def deleteItem(item) {
            try {
                item.delete()
                result = true
            } catch (e) {
                actionCommand.doingActionError =
                    "Error deleting ${domainClassName} id:${item.id}"
            }
        }
    }

    private validateAction(String domainClassName, ActionCommand actionCommand, Closure closure) {
        def result = false
        if (domainClassName && actionCommand && closure) {
            def domainClass = getDomainClass(domainClassName)
            if (domainClass) {
                def actions = new Actions(domainClassName: domainClassName,
                        domainClass:domainClass, actionCommand: actionCommand)
                try {
                    closure.delegate = actions
                    closure()
                    result = actions.result
                } catch (e) {
                    result = false
                    actionCommand.doingActionError = "Exception: ${e.message}"
                }
            } else {
                actionCommand.doingActionError = "Don't find domain class ${domainClassName}"
            }
        } else {
            actionCommand.doingActionError = "Bad parameters"
        }
        result
    }

    @Transactional
    boolean create(String domainClassName, ActionCommand actionCommand) {
        validateAction(domainClassName, actionCommand) {
            def item = domainClass.newInstance()
            if (item) {
                passParametersToItem(item)
                validateAndSave(item)
                passItemToMap(item, actionCommand.data)
            }
        }
    }

    def read(String domainClassName, ActionCommand actionCommand) {
        validateAction(domainClassName, actionCommand) {
            result = getItemById()
        }
    }

    @Transactional
    boolean update(String domainClassName, ActionCommand actionCommand) {
        validateAction(domainClassName, actionCommand) {
            def item = getItemById()
            if (item) {
                passParametersToItem(item)
                validateAndSave(item)
            } else {
                actionCommand.doingActionError =
                    "Updating don't find ${domainClassName} with id:${actionCommand?.data?.id}"
            }
        }
    }

    @Transactional
    boolean delete(String domainClassName, ActionCommand actionCommand) {
        validateAction(domainClassName, actionCommand) {
            def item = getItemById()
            if (item) {
                deleteItem(item)
            } else {
                actionCommand.doingActionError =
                        "Deleting don't find ${domainClassName} with id:${actionCommand?.data?.id}"
            }
        }
    }
}
