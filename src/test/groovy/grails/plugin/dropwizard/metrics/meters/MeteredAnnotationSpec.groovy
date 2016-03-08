package grails.plugin.dropwizard.metrics.meters

// tag::test_class[]

import com.codahale.metrics.MetricRegistry
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class MeteredAnnotationSpec {

    static doWithSpring = {
        dropwizardMetricsRegistry MetricRegistry
    }

    void 'test the @Metered annotation'() {
        setup:
        def registry = applicationContext.dropwizardMetricsRegistry
        def obj = new SomeOtherClass()

        when:
        obj.someAction()
        obj.someAction()
        obj.someAction()

        then:
        registry.meter('some meter').count == 3
    }
}

// tag::sample_class[]
class SomeOtherClass {

    @Metered('some meter')
    void someAction() {
        // ...
    }
}
// end::sample_class[]
// end::test_class[]

