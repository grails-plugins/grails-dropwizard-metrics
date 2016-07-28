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

import grails.plugin.dropwizard.metrics.NamedMetricTransformation
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
@CompileStatic
class MeteredTransformation extends NamedMetricTransformation {

    @Override
    protected void decorateMethodWithMetrics(final Expression metricsRegistryExpression,
                                             final Expression meterNameExpression,
                                             final MethodNode methodNode) {
        final Expression meterExpression = new MethodCallExpression(metricsRegistryExpression, 'meter', meterNameExpression)
        final Expression markExpression = new MethodCallExpression(meterExpression, 'mark', new ArgumentListExpression())

        final BlockStatement newCode = new BlockStatement()

        newCode.addStatement(new ExpressionStatement(markExpression))

        final Statement originalMethodCode = methodNode.code
        newCode.addStatement(originalMethodCode)

        methodNode.code = newCode

    }
}
