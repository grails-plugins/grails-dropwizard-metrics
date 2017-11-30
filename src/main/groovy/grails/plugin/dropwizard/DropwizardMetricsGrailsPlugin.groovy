/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.dropwizard

import grails.plugin.dropwizard.reporters.ScheduledReporterFactory
import grails.plugins.Plugin

class DropwizardMetricsGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.14 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Dropwizard Metrics" // Headline display name of the plugin
    def author = "Jeff Scott Brown"
    def authorEmail = "brownj@objectcomputing.com.com"
    def description = '''\
Grails 3 plugin providing convenient access to the Dropwizard Metrics library.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails-plugins.github.io/grails-dropwizard-metrics"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [name: "OCI", url: "http://www.objectcomputing.com/"]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "GitHub Issues", url: "https://github.com/grails-plugins/grails-dropwizard-metrics/issues"]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/grails-plugins/grails-dropwizard-metrics"]

    @Override
    Closure doWithSpring() { { ->

        if(config.getProperty('grails.dropwizard.metrics.reporterFrequency', Integer, 0)) {
            scheduledReporterFactory(ScheduledReporterFactory)

            if (config.getProperty('grails.dropwizard.metrics.console-reporter.active', Boolean, false)) {
                dropwizardConsoleReporter(scheduledReporterFactory: 'consoleReporter')
            }

            if (config.getProperty('grails.dropwizard.metrics.slf4j-reporter.active', Boolean, true)) {
                dropwizardSlf4jReporter(scheduledReporterFactory: 'slf4jReporter')
            }

            if (config.getProperty('grails.dropwizard.metrics.csv-reporter.active', Boolean, false)) {
                dropwizardCsvReporter(scheduledReporterFactory: 'csvReporter')
            }
        }
    } }
}
