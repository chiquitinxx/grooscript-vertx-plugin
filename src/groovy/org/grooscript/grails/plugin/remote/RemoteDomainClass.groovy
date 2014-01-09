package org.grooscript.grails.plugin.remote

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by jorge on 22/12/13.
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(['org.grooscript.grails.plugin.remote.RemoteDomainClassImpl'])
public @interface RemoteDomainClass {
}
