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
            result.addAll command.errors
        }
        if (!executeOk) {
            result.add "Error doing action ${command.action}: ${command.doingActionError}"
        }
        result
    }

    private getDomainClass(String nameClass) {
        grailsApplication.domainClasses.find { it.shortName == nameClass }
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
        def validateAndSave(item) {
            if (!item.validate()) {
                actionCommand.doingActionError =
                    "Error validating ${domainClassName} ${actionCommand.data.toString()}"
            } else {
                item.save(failOnError: true)
                result = true
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
            }
        }
    }

    @Transactional
    boolean read(String domainClassName, ActionCommand actionCommand) {

    }

    static final NOT_ALLOWED_PROPERTIES = ['id', 'version']

    @Transactional
    boolean update(String domainClassName, ActionCommand actionCommand) {
        validateAction(domainClassName, actionCommand) {
            def item = domainClass.get(actionCommand?.data?.id)
            if (item) {
                passParametersToItem(item)
                validateAndSave(item)
            } else {
                actionCommand.doingActionError =
                    "Don't find ${domainClassName} with id:${actionCommand?.data?.id}"
            }
        }
    }

    @Transactional
    boolean delete(String domainClassName, ActionCommand actionCommand) {

    }
}
