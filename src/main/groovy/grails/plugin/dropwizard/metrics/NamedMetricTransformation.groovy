/*
 * Copyright 2017 the original author or authors.
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
        if(nodes[1] instanceof MethodNode){
            MethodNode method = (MethodNode) nodes[1]

            processMethodAnnotation(annotationNode, method, source)
        }
        else if(nodes[1] instanceof ClassNode){
            ClassNode classNode = (ClassNode) nodes[1]

            classNode.methods.findAll{!it.name.contains('$')}.each{ MethodNode method ->
                processClassAnnotation(annotationNode, method, source)
            }
        }
    }

    private processMethodAnnotation(AnnotationNode annotationNode, MethodNode methodNode, final SourceUnit source){
        final Expression metricNameExpression = getMetricNameExpression(annotationNode, methodNode, false)
        implementMetricRegistryAware(compilationUnit, source, methodNode.declaringClass)
        doTransformation(annotationNode, methodNode, source, metricNameExpression)
    }

    private processClassAnnotation(AnnotationNode annotationNode, MethodNode methodNode, final SourceUnit source){
        final Expression metricNameExpression = getMetricNameExpression(annotationNode, methodNode, true)
        implementMetricRegistryAware(compilationUnit, source, methodNode.declaringClass)
        doTransformation(annotationNode, methodNode, source, metricNameExpression)
    }

    abstract protected void doTransformation(AnnotationNode annotationNode, MethodNode methodNode, SourceUnit source, Expression metricNameExpression)

    protected Expression getMetricNameExpression(final AnnotationNode annotationNode, final MethodNode methodNode, final Boolean dynamic) {
        final String metricNameFromAnnotation

        if(dynamic) {
            final String methodName = methodNode.getName()

            final String parameters = methodNode.getParameters().join(', ')

            metricNameFromAnnotation = "${methodName}(${parameters}) meter"
        }
        else metricNameFromAnnotation = annotationNode.getMember('value').getText()

        final Expression metricNameExpression

        final Expression useClassPrefix = annotationNode.getMember('useClassPrefix')
        if (useClassPrefix instanceof ConstantExpression && ((ConstantExpression) useClassPrefix).value) {
            final ArgumentListExpression nameMethodArguments = new ArgumentListExpression()
            nameMethodArguments.addExpression(new ClassExpression(methodNode.declaringClass))
            nameMethodArguments.addExpression(new ConstantExpression(metricNameFromAnnotation))
            metricNameExpression = new StaticMethodCallExpression((ClassNode) ClassHelper.make(MetricRegistry), 'name', nameMethodArguments)
        } else {
            metricNameExpression = new ConstantExpression(metricNameFromAnnotation)
        }
        metricNameExpression
    }

    protected implementMetricRegistryAware(CompilationUnit unit, SourceUnit source, ClassNode classNode) {
        implementTrait(classNode, MetricRegistryAware, source)
    }

    protected void implementTrait(ClassNode classNode, Class traitClass, SourceUnit source) {
        final ClassNode traitClassNode = ClassHelper.make(traitClass)
        boolean implementsTrait = classNode.declaresInterface(traitClassNode)
        if (!implementsTrait) {
            classNode.addInterface(traitClassNode)
            org.codehaus.groovy.transform.trait.TraitComposer.doExtendTraits(classNode, source, compilationUnit)
        }
    }
}
