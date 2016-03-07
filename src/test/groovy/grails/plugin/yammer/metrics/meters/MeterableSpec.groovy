package grails.plugin.yammer.metrics.meters

import com.codahale.metrics.MetricRegistry
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class MeterableSpec extends Specification {

    static doWithSpring = {
        yammerMetricsRegistry MetricRegistry
    }

    void 'test markMeter method'() {
        setup:
        def registry = applicationContext.yammerMetricsRegistry
        def obj = new SomeClass()

        when:
        obj.someAction()
        obj.someAction()
        obj.someAction()

        then:
        registry.meter('stuff').count == 3
    }
}

class SomeClass implements Meterable {

    def someAction() {
        markMeter 'stuff'

        // carry on...
    }
}
