package grails.plugin.dropwizard.metrics.meters

import com.codahale.metrics.MetricRegistry
import grails.artefact.Artefact
import grails.test.mixin.TestFor

@TestFor(SomeController)
class MeteredControllerActionSpec {

    static doWithSpring = {
        dropwizardMetricsRegistry MetricRegistry
    }

    void 'test the @Metered annotation'() {
        setup:
        def registry = applicationContext.dropwizardMetricsRegistry

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