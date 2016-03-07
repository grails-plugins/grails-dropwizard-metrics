package grails.plugin.yammer.metrics.meters

import com.codahale.metrics.MetricRegistry
import grails.test.mixin.TestFor
import grails.web.Controller
import spock.lang.Specification

@TestFor(SomeController)
class MeterableSpec extends Specification {

    MetricRegistry registry

    void setup() {
        registry = applicationContext.getBean('yammerMetricsRegistry')
    }

    static doWithSpring = {
        yammerMetricsRegistry MetricRegistry
    }

    void 'test markMeter method'() {
        when:
        controller.someAction()
        controller.someAction()
        controller.someAction()

        then:
        registry.meter('stuff').count == 3
    }
}

@Controller
class SomeController implements Meterable {

    def someAction() {
        markMeter 'stuff'
    }
}
