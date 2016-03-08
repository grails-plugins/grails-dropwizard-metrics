package grails.plugin.dropwizard.metrics.timers

// tag::test_class[]

import com.codahale.metrics.MetricRegistry
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class TimedAnnotationSpec {

    static doWithSpring = {
        dropwizardMetricsRegistry MetricRegistry
    }

    void 'test the @Timed annotation'() {
        setup:
        def registry = applicationContext.dropwizardMetricsRegistry
        def obj = new SomeTimedClass()

        when:
        obj.someAction()
        obj.someAction()
        obj.someAction()

        then:
        registry.meter('some timer').count == 3
    }
}

// tag::sample_class[]
class SomeTimedClass {

    @Timed('some timer')
    void someAction() {
        // ...
    }
}
// end::sample_class[]
// end::test_class[]

