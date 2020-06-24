import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.BuildFailureOnText
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.failOnText
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

    artifactRules = "+:**"

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
    }

    triggers {
        retryBuild {
            attempts = 1000
            moveToTheQueueTop = true
        }
    }

    failureConditions {
        failOnText {
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "Total: 1"
            failureMessage = "OK"
            reverse = false
        }
    }
})
