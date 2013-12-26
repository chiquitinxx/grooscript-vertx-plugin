package org.grooscript.grails.plugin.remote

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grooscript.grails.plugin.GrooscriptVertxService
import org.grooscript.grails.plugin.promise.GsPromise
import org.grooscript.grails.plugin.promise.RemotePromise
import org.grooscript.grails.util.GrooscriptGrails

import java.lang.reflect.Modifier

/**
 * Created by jorge on 22/12/13.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class RemoteDomainClassImpl  implements ASTTransformation {
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        if (!nodes[0] instanceof AnnotationNode ||
                !nodes[1] instanceof ClassNode) {
            throw new RuntimeException('RemoteDomainClassImpl only applies to classes.')
        }

        ClassNode classNode = (ClassNode) nodes[1]
        if (classNode.hasProperty('id')) {
            return
        }
        try {
            addInstanceProperties(classNode)

            addSaveMethod(classNode)
            addDeleteMethod(classNode)
            addStaticGetMethod(classNode)
            addStaticListMethod(classNode)
        } catch(e) {
            println 'RemoteDomainClassImpl Exception:'+ e.message
        }
    }

    private addInstanceProperties(ClassNode classNode) {
        classNode.addProperty('id', Modifier.PUBLIC, ClassHelper.Long_TYPE,null,null,null)
        classNode.addProperty('version', Modifier.PUBLIC, ClassHelper.Long_TYPE,new ConstantExpression(0),null,null)
        classNode.addProperty('classNameWithoutPackage', Modifier.STATIC, ClassHelper.STRING_TYPE,
                new ConstantExpression(classNode.nameWithoutPackage),null,null)
    }

    private addStaticGetMethod(ClassNode classNode) {
        def params = new Parameter[1]
        params[0] = new Parameter(ClassHelper.long_TYPE, 'value')
        classNode.addMethod('get', Modifier.STATIC, new ClassNode(RemotePromise), params,
                ClassNode.EMPTY_ARRAY, new AstBuilder().buildFromCode {
            return new org.grooscript.grails.plugin.promise.RemotePromise(domainAction: 'read',
                className: this.classNameWithoutPackage, data: [id: value])
        }[0])
    }

    private addStaticListMethod(ClassNode classNode) {
        def params = new Parameter[1]
        params[0] = new Parameter(new ClassNode(HashMap), 'params')
        classNode.addMethod('list', Modifier.STATIC, new ClassNode(RemotePromise), params,
                ClassNode.EMPTY_ARRAY, new AstBuilder().buildFromCode {
            return new org.grooscript.grails.plugin.promise.RemotePromise(domainAction: 'list',
                    className: this.classNameWithoutPackage, data: params ?: [:])
        }[0])
    }

    private addSaveMethod(ClassNode classNode) {
        classNode.addMethod('save', Modifier.PUBLIC, new ClassNode(RemotePromise), Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new AstBuilder().buildFromCode {
            def action = (this.id ? 'update' : 'create')
            def data = org.grooscript.grails.util.GrooscriptGrails.getRemoteDomainClassProperties(this)
            return new org.grooscript.grails.plugin.promise.RemotePromise(domainAction: action,
                    className: this.classNameWithoutPackage,
                    data: data)
        }[0])
    }

    private addDeleteMethod(ClassNode classNode) {
        classNode.addMethod('delete', Modifier.PUBLIC, new ClassNode(RemotePromise), Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new AstBuilder().buildFromCode {
            return new org.grooscript.grails.plugin.promise.RemotePromise(domainAction: 'delete',
                    className: this.classNameWithoutPackage,
                    data: [id: this.id])
        }[0])
    }
}
