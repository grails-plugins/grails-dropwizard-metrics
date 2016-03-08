package grails.plugin.dropwizard.metrics

import com.codahale.metrics.MetricRegistry
import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation

@CompileStatic
abstract class NamedMetricTransformation implements ASTTransformation {
    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        final AnnotationNode annotationNode = (AnnotationNode)nodes[0]
        final MethodNode methodNode = (MethodNode)nodes[1]

        final Expression metricNameExpression = getMetricNameExpression(annotationNode, methodNode)

        final MethodCallExpression getBeanExpression = getMetricsRegistryExpression()

        decorateMethodWithMetrics(getBeanExpression, metricNameExpression, methodNode)
    }

    protected MethodCallExpression getMetricsRegistryExpression() {
        Expression getApplicationContextExpression = new StaticMethodCallExpression(ClassHelper.make(Holders), 'getApplicationContext', new ArgumentListExpression())
        Expression getBeanExpression = new MethodCallExpression(getApplicationContextExpression, 'getBean', new ConstantExpression('dropwizardMetricsRegistry'))
        getBeanExpression
    }

    protected Expression getMetricNameExpression(final AnnotationNode annotationNode, final MethodNode methodNode) {
        final String metricNameFromAnnotation = annotationNode.getMember('value').getText()

        final Expression metricNameExpression

        final Expression useClassPrefix = annotationNode.getMember('useClassPrefix')
        if (useClassPrefix instanceof ConstantExpression && ((ConstantExpression) useClassPrefix).value) {
            final ArgumentListExpression nameMethodArguments = new ArgumentListExpression()
            nameMethodArguments.addExpression(new ClassExpression(methodNode.declaringClass))
            nameMethodArguments.addExpression(new ConstantExpression(metricNameFromAnnotation))
            metricNameExpression = new StaticMethodCallExpression(ClassHelper.make(MetricRegistry), 'name', nameMethodArguments)
        } else {
            metricNameExpression = new ConstantExpression(metricNameFromAnnotation)
        }
        metricNameExpression
    }

    abstract protected void decorateMethodWithMetrics(MethodCallExpression metricsRegistryExpression, Expression metricNameExpression, MethodNode methodNode)

}
