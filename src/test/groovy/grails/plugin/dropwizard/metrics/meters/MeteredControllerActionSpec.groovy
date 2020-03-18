package grails.plugin.dropwizard.metrics.meters

import com.codahale.metrics.MetricRegistry
import grails.artefact.Artefact
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class MeteredControllerActionSpec extends Specification implements ControllerUnitTest<SomeController> {

    Closure doWithSpring() {
        { ->
            metricRegistry MetricRegistry
        }
    }

    void 'test the @Metered annotation'() {
        setup:
            def registry = applicationContext.metricRegistry

        when:
            controller.someAction()
            controller.someAction()

        then:
            registry.meter('some action meter').count == 2
    }
}

@Artefact('Controller')
class SomeController {

    @Metered('some action meter')
    void someAction() {
        // ...
    }
}