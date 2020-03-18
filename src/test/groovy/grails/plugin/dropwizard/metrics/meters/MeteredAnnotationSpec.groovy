package grails.plugin.dropwizard.metrics.meters

// tag::test_class[]

import com.codahale.metrics.MetricRegistry
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class MeteredAnnotationSpec extends Specification implements GrailsUnitTest {

    Closure doWithSpring() {
        { ->
            metricRegistry MetricRegistry
            someMeteredBean SomeOtherClass
        }
    }

    void 'test the @Metered annotation'() {
        setup:
            def registry = applicationContext.metricRegistry
            def obj = applicationContext.someMeteredBean

        when:
            obj.someAction()
            obj.someAction()
            obj.someAction()

        then:
            registry.meter('some meter').count == 3
    }

    void 'test the @Metered annotation with useClassPrefix set to true'() {
        setup:
            def registry = applicationContext.metricRegistry
            def obj = applicationContext.someMeteredBean

        when:
            obj.someOtherAction()
            obj.someOtherAction()
            obj.someOtherAction()

        then:
            registry.meter('grails.plugin.dropwizard.metrics.meters.SomeOtherClass.some other meter').count == 3
    }

    void 'test the @Metered annotation with useClassPrefix set to false'() {
        setup:
            def registry = applicationContext.metricRegistry
            def obj = applicationContext.someMeteredBean

        when:
            obj.yetAnotherAction()
            obj.yetAnotherAction()
            obj.yetAnotherAction()

        then:
            registry.meter('yet another meter').count == 3
    }
}

// tag::sample_class[]
class SomeOtherClass {

    @Metered('some meter')
    void someAction() {
        // ...
    }

    @Metered(value = 'some other meter', useClassPrefix = true)
    void someOtherAction() {
        // ...
    }

    @Metered(value = 'yet another meter', useClassPrefix = false)
    void yetAnotherAction() {
        // ...
    }
}

// end::sample_class[]
// end::test_class[]

