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
package grails.plugin.dropwizard.metrics.timers.ast

import com.codahale.metrics.MetricRegistry
import grails.util.Holders
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
class TimedTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotationNode annotationNode = nodes[0]
        MethodNode methodNode = nodes[1]

        Expression getApplicationContextExpression = new StaticMethodCallExpression(ClassHelper.make(Holders), 'getApplicationContext', new ArgumentListExpression())
        Expression getBeanExpression = new MethodCallExpression(getApplicationContextExpression, 'getBean', new ConstantExpression('dropwizardMetricsRegistry'))

        String timerNameFromAnnotation = annotationNode.getMember('value').getText()

        Expression timerNameExpression

        Expression useClassPrefix = annotationNode.getMember('useClassPrefix')
        if(useClassPrefix instanceof ConstantExpression && ((ConstantExpression)useClassPrefix).value) {
            ArgumentListExpression nameMethodArguments = new ArgumentListExpression()
            nameMethodArguments.addExpression(new ClassExpression(methodNode.declaringClass))
            nameMethodArguments.addExpression(new ConstantExpression(timerNameFromAnnotation))
            timerNameExpression = new StaticMethodCallExpression(ClassHelper.make(MetricRegistry), 'name', nameMethodArguments)
        } else {
            timerNameExpression = new ConstantExpression(timerNameFromAnnotation)
        }

        Expression timerExpression = new MethodCallExpression(getBeanExpression, 'timer', timerNameExpression)

        Expression timeExpression = new MethodCallExpression(timerExpression, 'time', new ArgumentListExpression())

        String contextVariableName = '$$_dropwizard_time'
        Expression declareTimerExpression = new DeclarationExpression(
                new VariableExpression(contextVariableName, ClassHelper.make(com.codahale.metrics.Timer.Context)), Token.newSymbol(Types.EQUALS, 0, 0), timeExpression)

        BlockStatement newCode = new BlockStatement()
        newCode.addStatement(new ExpressionStatement(new MethodCallExpression(new VariableExpression('this'), 'println', timerNameExpression)))
        newCode.addStatement(new ExpressionStatement(declareTimerExpression))

        Expression stopTimer = new MethodCallExpression(new VariableExpression(contextVariableName), 'stop', new ArgumentListExpression())
        Statement tryCatchStatement = new TryCatchStatement(methodNode.code, new ExpressionStatement(stopTimer))
        newCode.addStatement(tryCatchStatement)

        methodNode.code = newCode
    }
}
