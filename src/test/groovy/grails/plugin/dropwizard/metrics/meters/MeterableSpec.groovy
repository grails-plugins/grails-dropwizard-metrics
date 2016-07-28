package grails.plugin.dropwizard.metrics.meters

// tag::test_class[]

import com.codahale.metrics.MetricRegistry
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class MeterableSpec extends Specification {

    static doWithSpring = {
        metricRegistry MetricRegistry
        someBean SomeClass
    }

    void 'test markMeter method'() {
        setup:
        def registry = applicationContext.metricRegistry
        def obj = applicationContext.someBean

        when:
        obj.someAction()
        obj.someAction()
        obj.someAction()

        then:
        registry.meter('some meter').count == 3
    }
}

// tag::sample_class[]
class SomeClass implements Meterable {

    def someAction() {
        markMeter 'some meter'

        // ...
    }
}
// end::sample_class[]
// end::test_class[]
