package yammer.metrics

import grails.plugins.*

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
}
