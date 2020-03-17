package grails.plugin.dropwizard.reporters

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class DropwizardReporterConfigurationIntSpec extends Specification {

    static final String CONSOLE_REPORTER_CONFIG = 'grails.dropwizard.metrics.console-reporter'
    static final String CSV_REPORTER_CONFIG = 'grails.dropwizard.metrics.csv-reporter'
    static final String SLF4_REPORTER_CONFIG = 'grails.dropwizard.metrics.slf4j-reporter'

    GrailsApplication grailsApplication

    void "verify Slf4jReporter available by default"() {
        expect:
            grailsApplication.config[SLF4_REPORTER_CONFIG]
            grailsApplication.mainContext.containsBean('dropwizardSlf4jReporter')
    }

    void "verify ConsoleReporter not available by default"() {
        expect:
            grailsApplication.config[CONSOLE_REPORTER_CONFIG]
            !grailsApplication.mainContext.containsBean('dropwizardConsoleReporter')
    }

    void "verify CsvReporter not available by default"() {
        expect:
            grailsApplication.config[CSV_REPORTER_CONFIG]
            !grailsApplication.mainContext.containsBean('dropwizardCsvReporter')
    }
}
