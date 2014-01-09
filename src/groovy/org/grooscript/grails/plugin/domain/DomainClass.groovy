package org.grooscript.grails.plugin.domain

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * User: jorgefrancoleza
 * Date: 28/01/13
 */

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(['org.grooscript.grails.plugin.domain.DomainClassImpl'])
public @interface DomainClass {
}