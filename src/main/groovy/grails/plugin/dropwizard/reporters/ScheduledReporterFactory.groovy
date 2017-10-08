package grails.plugin.dropwizard.reporters

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.CsvReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
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

    /**
     * Instantiates a ConsoleReporter to be registered as a Spring bean named dropwizardConsoleReporter
     * @return A ConsoleReporter
     */
    ConsoleReporter consoleReporter() {
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
                                                         .convertRatesTo(TimeUnit.SECONDS)
                                                         .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                         .build()

        consoleReporter.start reporterFrequency, TimeUnit.SECONDS
        log.info 'consoleReporter started'

        consoleReporter
    }

    /**
     * Instantiates a CsvReporter to be registered as a Spring bean named dropwizardCsvReporter
     * @return A CsvReporter
     */
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

    /**
     * Instantiates a Slf4jReporter to be registered as a Spring bean named dropwizardSlf4jReporter
     * @return A Slf4jReporter
     */
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
}
