package grails.plugin.yammer.metrics.meters

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import grails.web.api.ServletAttributes
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

@CompileStatic
trait Meterable extends ServletAttributes {

    private MetricRegistry retrieveMetricRegistry() {
        applicationContext.getBean('yammerMetricsRegistry', MetricRegistry)
    }

    private Meter retrieveMeter(String name) {
        retrieveMetricRegistry().meter(name)
        retrieveMetricRegistry().meter(name)
        retrieveMetricRegistry().meter(name)
    }

    void markMeter(String name) {
        retrieveMeter(name).mark()
    }
}
