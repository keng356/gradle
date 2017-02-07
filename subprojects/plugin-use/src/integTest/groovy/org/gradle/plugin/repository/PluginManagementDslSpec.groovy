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

package org.gradle.plugin.repository

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.util.GradleVersion
import spock.lang.IgnoreRest

import static org.hamcrest.Matchers.containsString

class PluginManagementDslSpec extends AbstractIntegrationSpec {

    def "pluginManagement block can be read from settings.gradle"() {
        given:
        settingsFile << """
            pluginManagement {}
        """

        expect:
        succeeds 'help'
    }

    def "pluginManagement block supports defining a maven plugin repository"() {
        given:
        settingsFile << """
            pluginManagement {
                repositories {
                    maven {
                        url "http://repo.internal.net/m2"
                        authentication {
                            basic(BasicAuthentication)
                        }
                        credentials {
                            username = "noob"
                            password = "hunter2"
                        }
                    }
                }
            }
        """

        expect:
        succeeds 'help'
    }

    def "pluginManagement block supports defining a ivy plugin repository"() {
        given:
        settingsFile << """
            pluginManagement {
                repositories {
                    ivy {
                        url "http://repo.internal.net/ivy"
                        authentication {
                            basic(BasicAuthentication)
                        }
                        credentials {
                            username = "noob"
                            password = "hunter2"
                        }
                    }
                }
            }
        """

        expect:
        succeeds 'help'
    }


    def "pluginManagement block supports adding rule based plugin repository"() {
        given:
        settingsFile << """
            pluginManagement {
                pluginResolutionStrategy.eachPlugin { request ->
                    if(resolution.requestedPlugin.id.name == 'foo') {
                        resolution.useTarget('com.acme:foo:+')
                    }
                }
                repositories { 
                    mavenLocal()
                }
            }
        """

        expect:
        succeeds 'help'
    }

    def "pluginManagement block supports adding rule based plugin repository with isolation and rename"() {
        given:
        settingsFile << """
            pluginManagement {
                pluginResolutionStrategy.eachPlugin { request ->
                    if(resolution.requestedPlugin.id.name == 'foo') {
                        resolution.useModule('com.acme:foo:+') {
                            usePluginName('org.example.plugin')
                        }
                    }
                }
                repositories { 
                    mavenLocal()
                }
            }
        """

        expect:
        succeeds 'help'
    }

    def "other blocks can follow the pluginManagement block"() {
        given:
        settingsFile << """
            pluginManagement {}
            rootProject.name = 'rumpelstiltskin'
        """

        expect:
        succeeds 'help'
    }


    def "pluginManagement block is not supported in ProjectScripts"() {
        given:
        buildScript """
            pluginManagement {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(2)
        failure.assertThatCause(containsString("Only Settings scripts can contain a pluginManagement {} block."))
        includesLinkToUserguide()
    }

    def "pluginManagement block is not supported in InitScripts"() {
        given:
        def initScript = file "definePluginRepos.gradle"
        initScript << """
            pluginManagement {}
        """
        args('-I', initScript.absolutePath)

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(2)
        failure.assertThatCause(containsString("Only Settings scripts can contain a pluginManagement {} block."))
        includesLinkToUserguide()
    }

    def "pluginManagement block must come before imperative blocks in the settings.gradle script"() {
        given:
        settingsFile << """
            rootProject.name = 'rumpelstiltskin'
            pluginManagement {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("The pluginManagement {} block must appear before any other statements in the script."))
        includesLinkToUserguide()
    }

    def "pluginManagement block must come before buildScript blocks in the settings.gradle script"() {
        given:
        settingsFile << """
            buildScript {}
            pluginManagement {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("The pluginManagement {} block must appear before any other statements in the script."))
        includesLinkToUserguide()
    }

    def "pluginManagement block must be a top-level block (not nested)"() {
        given:
        settingsFile << """
            if (true) {
                pluginManagement {}
            }
        """

        when:
        fails 'help'

        then:
        failure.assertThatCause(containsString("Could not find method pluginManagement()"))
    }

    def "Only one pluginManagement block is allowed in each script"() {
        given:
        settingsFile << """
            pluginManagement {}
            pluginManagement {}
        """

        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("At most, one pluginManagement {} block may appear in the script."))
        includesLinkToUserguide()
    }

    def "Can access properties in pluginManagement block"() {
        given:
        settingsFile << """
            pluginManagement {
                repositories {
                    maven {
                        url repoUrl
                    }
                }
            }
        """
        expect:
        succeeds 'help', '-PrepoUrl=some/place'

    }

    def "Can access SettingsScript API in pluginManagement block"() {
        given:
        settingsFile << """
            pluginManagement {
                repositories {
                    maven {
                        url file('bar')
                    }
                }
            }
        """
        expect:
        succeeds 'help'
    }

    @IgnoreRest
    def "Cannot access Settings API in pluginManagement block"() {
        given:
        settingsFile << """
            pluginManagement {
                include 'foo'
            }
        """
        when:
        fails 'help'

        then:
        failure.assertHasLineNumber(3)
        failure.assertThatCause(containsString("Could not find method include()"))
    }

    void includesLinkToUserguide() {
        failure.assertThatCause(containsString("https://docs.gradle.org/${GradleVersion.current().getVersion()}/userguide/plugins.html#sec:plugin_management"))
    }
}
