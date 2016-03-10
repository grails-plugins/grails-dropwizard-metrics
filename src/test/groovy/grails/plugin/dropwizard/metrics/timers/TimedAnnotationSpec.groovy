package grails.plugin.dropwizard.metrics.timers

// tag::test_class[]

import com.codahale.metrics.MetricRegistry
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class TimedAnnotationSpec extends Specification {

    static doWithSpring = {
        metricRegistry MetricRegistry
    }

    void 'test the @Timed annotation'() {
        setup:
        def registry = applicationContext.metricRegistry
        def obj = new SomeTimedClass()

        when:
        obj.someAction()
        obj.someAction()
        obj.someAction()

        then:
        registry.timer('some timer').count == 3
    }

    void 'test the @Timed annotation with class prefix set to true'() {
        setup:
        def registry = applicationContext.metricRegistry
        def obj = new SomeTimedClass()

        when:
        obj.someOtherAction()
        obj.someOtherAction()
        obj.someOtherAction()

        then:
        registry.timer('grails.plugin.dropwizard.metrics.timers.SomeTimedClass.some other timer').count == 3
    }

    void 'test the @Timed annotation with class prefix set to false'() {
        setup:
        def registry = applicationContext.metricRegistry
        def obj = new SomeTimedClass()

        when:
        obj.yetAnotherAction()
        obj.yetAnotherAction()
        obj.yetAnotherAction()

        then:
        registry.timer('yet other timer').count == 3
    }
}

// tag::sample_class[]
class SomeTimedClass {

    @Timed('some timer')
    void someAction() {
        // ...
    }

    @Timed(value='some other timer', useClassPrefix = true)
    void someOtherAction() {
        // ...
    }

    @Timed(value='yet other timer', useClassPrefix = false)
    void yetAnotherAction() {
        // ...
    }
}
// end::sample_class[]
// end::test_class[]

