package grails.plugin.dropwizard.metrics

import com.codahale.metrics.MetricRegistry
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.springframework.beans.factory.annotation.Autowired

import java.lang.reflect.Modifier

@CompileStatic
abstract class NamedMetricTransformation implements ASTTransformation {

    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        final AnnotationNode annotationNode = (AnnotationNode)nodes[0]
        final MethodNode methodNode = (MethodNode)nodes[1]

        final Expression metricNameExpression = getMetricNameExpression(annotationNode, methodNode)

        addAutowiredPropertyToClass(methodNode.declaringClass, MetricRegistry, 'metricRegistry')

        decorateMethodWithMetrics(new VariableExpression('metricRegistry'), metricNameExpression, methodNode)
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

    protected addAutowiredPropertyToClass(ClassNode classNode, Class propertyType, String propertyName) {
        if (!classNode.hasProperty(propertyName)) {
            FieldNode metricsRegistryFieldNode = new FieldNode(propertyName, Modifier.PRIVATE, ClassHelper.make(propertyType), classNode, new EmptyExpression())
            AnnotationNode autowiredAnnotationNode = new AnnotationNode(ClassHelper.make(Autowired))
            autowiredAnnotationNode.setMember('required', new ConstantExpression(false))
            metricsRegistryFieldNode.addAnnotation(autowiredAnnotationNode)
            PropertyNode metricsRegistryPropertyNode = new PropertyNode(metricsRegistryFieldNode, Modifier.PUBLIC, null, null)
            classNode.addProperty(metricsRegistryPropertyNode)
        }
    }

    abstract protected void decorateMethodWithMetrics(Expression metricsRegistryExpression, Expression metricNameExpression, MethodNode methodNode)

}
