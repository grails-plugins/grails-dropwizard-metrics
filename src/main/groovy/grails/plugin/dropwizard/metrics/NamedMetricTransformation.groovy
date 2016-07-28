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
        doTransformation(annotationNode, methodNode, source, metricNameExpression)
    }

    abstract protected void doTransformation(AnnotationNode annotationNode, MethodNode methodNode, SourceUnit source, Expression metricNameExpression)

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
        implementTrait(classNode, metricRegistryAwareClassNode, source)
    }

    protected void implementTrait(ClassNode classNode, ClassNode traitClassNode, SourceUnit source) {
        boolean implementsTrait = classNode.declaresInterface(traitClassNode)
        if (!implementsTrait) {
            classNode.addInterface(traitClassNode)
            org.codehaus.groovy.transform.trait.TraitComposer.doExtendTraits(classNode, source, compilationUnit)
        }
    }
}
