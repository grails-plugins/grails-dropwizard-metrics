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
package yammer.metrics

import com.codahale.metrics.Metric
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricSet
import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import com.codahale.metrics.servlets.HealthCheckServlet
import com.codahale.metrics.servlets.MetricsServlet
import grails.plugins.Plugin
import groovy.util.logging.Slf4j
import org.springframework.boot.context.embedded.ServletRegistrationBean

import java.util.concurrent.TimeUnit

@Slf4j
class YammerMetricsGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.14 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Yammer Metrics" // Headline display name of the plugin
    def author = "Jeff Scott Brown"
    def authorEmail = "brownj@ociweb.com"
    def description = '''\
Grails 3 plugin providing convenient access to the Yammer Metrics library.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails-plugins.github.io/grails-yammer-metrics"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "OCI", url: "http://www.ociweb.com/" ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub Issues", url: "https://github.com/grails-plugins/grails-yammer-metrics/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/grails-plugins/grails-yammer-metrics" ]

    Closure doWithSpring() {
        { ->
            yammerHealthCheckRegistry(HealthCheckRegistry)

            String healthCheckUri = config.getProperty('grails.yammer.health.uri', String, null)
            if(healthCheckUri) {
                yammerHealthCheckServlet(HealthCheckServlet, ref('yammerHealthCheckRegistry'))
                yammerHealthCheckServletRegistryBean(ServletRegistrationBean, ref('yammerHealthCheckServlet'), healthCheckUri)
            }

            yammerMetricsRegistry MetricRegistry
            String metricsUri = config.getProperty('grails.yammer.metrics.uri', String, null)
            if(metricsUri) {
                yammerMetricsServlet(MetricsServlet, ref('yammerMetricsRegistry'))
                yammerMetricsServletRegistryBean(ServletRegistrationBean, ref('yammerMetricsServlet'), metricsUri)
            }

            yammerGarbageCollectorMetricSet GarbageCollectorMetricSet
            yammerMemoryUsageGaugeSet MemoryUsageGaugeSet
            yammerThreadStatesGaugeSet ThreadStatesGaugeSet
            yammerThreadDeadlockHealthCheck ThreadDeadlockHealthCheck
        }
    }

    @Override
    void doWithApplicationContext() {
        def metricSetBeans = applicationContext.getBeansOfType(MetricSet)
        def registry = applicationContext.yammerMetricsRegistry
        if (metricSetBeans) {
            for (Map.Entry entry : metricSetBeans) {
                MetricSet set = entry.value
                if (!(set instanceof MetricRegistry)) {
                    String beanName = entry.key
                    registerMetrics beanName, set, registry
                }
            }
        }
        def healthCheckBeans = applicationContext.getBeansOfType(HealthCheck)
        if(healthCheckBeans) {
            def healthRegistry = applicationContext.yammerHealthCheckRegistry
            for(Map.Entry entry : healthCheckBeans) {
                HealthCheck check = entry.value
                String beanName = entry.key
                healthRegistry.register beanName, check
            }
        }
        def logReporterFequency = config.getProperty('grails.yammer.metrics.reporterFrequency', Integer, 0)
        if(logReporterFequency > 0) {
            Slf4jReporter logbackReporter = Slf4jReporter.forRegistry(registry).outputTo(log)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS).build()
            logbackReporter.start logReporterFequency, TimeUnit.SECONDS
        }
    }

    protected void registerMetrics(String beanName, MetricSet metricSet, MetricRegistry registry) {
        for (Map.Entry<String, Metric> entry : metricSet.metrics.entrySet()) {
            if (entry.value instanceof MetricSet) {
                registerMetrics(beanName, entry.value, registry)
            } else {
                registry.register "grails.yammer.${beanName}.${entry.key}", entry.value
            }
        }
    }
}
