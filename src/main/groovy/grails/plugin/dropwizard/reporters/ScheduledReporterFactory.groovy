package grails.plugin.dropwizard.reporters

import com.codahale.metrics.*
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext

import java.util.concurrent.TimeUnit

@Slf4j
class ScheduledReporterFactory {

    @Autowired
    MetricRegistry metricRegistry

    @Autowired
    ApplicationContext applicationContext

    @Value('${grails.dropwizard.metrics.reporterFrequency:0}')
    Integer reporterFrequency

    @Value('${grails.dropwizard.metrics.csv-reporter.output-dir:./}')
    String csvOutputDir

    @Value('${grails.dropwizard.metrics.graphite-reporter.graphite-server:localhost}')
    String graphiteServer

    @Value('${grails.dropwizard.metrics.graphite-reporter.graphite-server-port:2003}')
    Integer graphiteServerPort

    @Value('${grails.dropwizard.metrics.graphite-reporter.graphite-metric-prefix:example.com}')
    String graphiteMetricPrefix

    @Value('${grails.dropwizard.metrics.graphite-reporter.metric-filter-bean:}')
    String metricFilterBean

    ConsoleReporter consoleReporter() {
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
                                                         .convertRatesTo(TimeUnit.SECONDS)
                                                         .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                         .build()

        consoleReporter.start reporterFrequency, TimeUnit.SECONDS
        log.info 'consoleReporter started'

        consoleReporter
    }

    CsvReporter csvReporter() {
        File outputDir = new File(csvOutputDir)
        if(outputDir.exists()) {
            CsvReporter csvReporter = CsvReporter.forRegistry(metricRegistry)
                                                 .convertRatesTo(TimeUnit.SECONDS)
                                                 .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                 .build(outputDir)

            csvReporter.start reporterFrequency, TimeUnit.SECONDS
            log.info 'csvReporter started'

            return csvReporter
        } else {
            log.warn "CSV Reporter output directory ${csvOutputDir} does not exist"
            return null
        }
    }

    Slf4jReporter slf4jReporter() {
        Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .outputTo(LoggerFactory.getLogger(Slf4jReporter))
            .build()

        slf4jReporter.start reporterFrequency, TimeUnit.SECONDS
        log.info 'slf4jReporter started'

        slf4jReporter
    }

    GraphiteReporter graphiteReporter() {
        MetricFilter metricFilter = metricFilterBean ? applicationContext.getBean(metricFilterBean) as MetricFilter : MetricFilter.ALL
        final Graphite graphite = new Graphite(new InetSocketAddress(graphiteServer, graphiteServerPort))
        final GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(metricRegistry)
                                                                  .convertRatesTo(TimeUnit.SECONDS)
                                                                  .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                                  .prefixedWith(graphiteMetricPrefix)
                                                                  .filter(metricFilter)
                                                                  .build(graphite)
        graphiteReporter.start reporterFrequency, TimeUnit.SECONDS
        log.info 'graphiteReporter started'

        graphiteReporter
    }
}
