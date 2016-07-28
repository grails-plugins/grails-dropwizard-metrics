package grails.plugin.dropwizard.metrics

import com.codahale.metrics.MetricRegistry
import grails.plugin.dropwizard.ast.MetricRegistryAware
import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation

@CompileStatic
abstract class NamedMetricTransformation implements ASTTransformation, CompilationUnitAware {

    CompilationUnit compilationUnit

    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        final AnnotationNode annotationNode = (AnnotationNode)nodes[0]
        final MethodNode methodNode = (MethodNode)nodes[1]

        final Expression metricNameExpression = getMetricNameExpression(annotationNode, methodNode)

        implementMetricRegistryAware(compilationUnit, source, methodNode.declaringClass)

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

    protected implementMetricRegistryAware(CompilationUnit unit, SourceUnit source, ClassNode classNode) {
        def metricRegistryAwareClassNode = ClassHelper.make(MetricRegistryAware)
        boolean implementsTrait = classNode.declaresInterface(metricRegistryAwareClassNode)
        if(!implementsTrait) {
            classNode.addInterface(metricRegistryAwareClassNode)
            org.codehaus.groovy.transform.trait.TraitComposer.doExtendTraits(classNode, source, unit)
        }
    }

    abstract protected void decorateMethodWithMetrics(Expression metricsRegistryExpression, Expression metricNameExpression, MethodNode methodNode)

}
