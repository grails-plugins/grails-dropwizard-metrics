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

import com.codahale.metrics.Timer
import grails.plugin.dropwizard.metrics.NamedMetricTransformation
import groovy.transform.CompileStatic
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
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
@CompileStatic
class TimedTransformation extends NamedMetricTransformation {

    @Override
    protected void doTransformation(final AnnotationNode annotationNode,
                                    final MethodNode methodNode,
                                    final SourceUnit source,
                                    final Expression nameExpression) {
        final Expression timerExpression = new MethodCallExpression(new VariableExpression('metricRegistry'), 'timer', nameExpression)

        final Expression timeExpression = new MethodCallExpression(timerExpression, 'time', new ArgumentListExpression())

        final String contextVariableName = '$$_dropwizard_time'
        final Expression declareTimerExpression = new DeclarationExpression(
                new VariableExpression(contextVariableName, ClassHelper.make(Timer.Context)), Token.newSymbol(Types.EQUALS, 0, 0), timeExpression)

        final BlockStatement newCode = new BlockStatement()
        newCode.addStatement(new ExpressionStatement(declareTimerExpression))

        final Expression stopTimer = new MethodCallExpression(new VariableExpression(contextVariableName), 'stop', new ArgumentListExpression())
        final Statement tryCatchStatement = new TryCatchStatement(methodNode.code, new ExpressionStatement(stopTimer))
        newCode.addStatement(tryCatchStatement)

        methodNode.code = newCode
    }
}
