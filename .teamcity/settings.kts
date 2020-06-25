import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.BuildFailureOnMetric
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.failOnMetricChange
import jetbrains.buildServer.configs.kotlin.v2019_2.ideaInspections
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.retryBuild

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {

    buildType(Inspections)
}

object Inspections : BuildType({
    name = "Inspections"

    artifactRules = "+:%system.teamcity.build.tempDir%/inspection*result/UseExpressionBody.xml"

    params {
        param("system.teamcity.dont.delete.temp.result.dir", "true")
    }

    vcs {
        root(DslContext.settingsRoot)

        cleanCheckout = true
    }

    steps {
        ideaInspections {
            name = "Idea Inspections"
            pathToProject = "settings.gradle.kts"
            jvmArgs = "-Xmx6G -XX:ReservedCodeCacheSize=1G"
            targetJdkHome = "%env.JDK_18%"
            profileName = "Project Default"
        }
        script {
            scriptContent = """
                if test -n "${'$'}(find %system.teamcity.build.tempDir% -maxdepth 2 -name 'UseExpressionBody.xml' -print -quit)"
                then
                    echo "Some inspections fail"
                    exit 1
                fi
            """.trimIndent()
        }
    }

    triggers {
        retryBuild {
            enabled = false
            attempts = 1000
            moveToTheQueueTop = true
        }
    }

    failureConditions {
        failOnMetricChange {
            metric = BuildFailureOnMetric.MetricType.INSPECTION_ERROR_COUNT
            threshold = 0
            units = BuildFailureOnMetric.MetricUnit.DEFAULT_UNIT
            comparison = BuildFailureOnMetric.MetricComparison.MORE
            compareTo = value()
            stopBuildOnFailure = true
        }
        failOnMetricChange {
            metric = BuildFailureOnMetric.MetricType.INSPECTION_WARN_COUNT
            threshold = 0
            units = BuildFailureOnMetric.MetricUnit.DEFAULT_UNIT
            comparison = BuildFailureOnMetric.MetricComparison.MORE
            compareTo = value()
        }
    }
})
