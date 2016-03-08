/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.dropwizard.metrics.meters.ast

import grails.util.Holders
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
class MeteredTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotationNode annotationNode = nodes[0]
        MethodNode methodNode = nodes[1]

        StaticMethodCallExpression getApplicationContextExpression = new StaticMethodCallExpression(ClassHelper.make(Holders), 'getApplicationContext', new ArgumentListExpression())
        MethodCallExpression getBeanExpression = new MethodCallExpression(getApplicationContextExpression, 'getBean', new ConstantExpression('dropwizardMetricsRegistry'))

        String meterNameFromAnnotation = annotationNode.getMember('value').getText()
        MethodCallExpression meterExpression = new MethodCallExpression(getBeanExpression, 'meter', new ConstantExpression(meterNameFromAnnotation))
        MethodCallExpression markExpression = new MethodCallExpression(meterExpression, 'mark', new ArgumentListExpression())

        BlockStatement newCode = new BlockStatement()

        newCode.addStatement(new ExpressionStatement(markExpression))

        Statement originalMethodCode = methodNode.code
        newCode.addStatement(originalMethodCode)

        methodNode.code = newCode
    }
}
