package grails.plugin.dropwizard.metrics.timers

import com.codahale.metrics.MetricRegistry
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

// tag::test_class[]

class TimedAnnotationSpec extends Specification implements GrailsUnitTest {

    Closure doWithSpring() {
        { ->
            metricRegistry MetricRegistry
            someTimedBean SomeTimedClass
        }
    }

    void 'test the @Timed annotation'() {
        setup:
            def registry = applicationContext.metricRegistry
            def obj = applicationContext.someTimedBean

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
            def obj = applicationContext.someTimedBean

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
            def obj = applicationContext.someTimedBean

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

    @Timed(value = 'some other timer', useClassPrefix = true)
    void someOtherAction() {
        // ...
    }

    @Timed(value = 'yet other timer', useClassPrefix = false)
    void yetAnotherAction() {
        // ...
    }
}

// end::sample_class[]
// end::test_class[]

