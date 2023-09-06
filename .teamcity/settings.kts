import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.schedule
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

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

version = "2023.05"

project {

    vcsRoot(Project_1)

    buildType(CheckVulnerabilities)
    buildType(UnitTests)
    buildTypesOrder = arrayListOf(UnitTests, CheckVulnerabilities)

    subProject(Deployment)
    subProject(Review)
}

object CheckVulnerabilities : BuildType({
    name = "Check vulnerabilities"

    vcs {
        root(Project_1, "+:project => .")
    }

    steps {
        script {
            name = "Run the script"
            scriptContent = """echo "There are no vulnerabilities""""
        }
    }

    triggers {
        schedule {
            schedulingPolicy = daily {
                hour = 2
            }
            branchFilter = ""
            triggerBuild = always()
            withPendingChangesOnly = false
        }
    }
})

object UnitTests : BuildType({
    name = "Unit tests"

    vcs {
        root(Project_1, "+:project => .")
    }

    steps {
        python {
            name = "Run unit tests"
            environment = poetry {
            }
            command = pytest {
                isCoverageEnabled = true
            }
        }
    }

    triggers {
        vcs {
            branchFilter = ""
        }
    }
})

object Project_1 : GitVcsRoot({
    id("Project")
    name = "Project"
    url = "git@github.com:jjudas-resistant/teamcity-poc.git"
    branch = "main"
    branchSpec = "+:*"
    authMethod = uploadedKey {
        uploadedKey = "teamcity_server"
    }
})


object Deployment : Project({
    name = "Deployment"

    buildType(Deployment_DeployToTesting)
})

object Deployment_DeployToTesting : BuildType({
    name = "Deploy to testing"

    params {
        select("Customer", "", display = ParameterDisplay.PROMPT,
                options = listOf("customer1", "customer2", "customer3"))
    }

    vcs {
        root(Project_1)
    }

    steps {
        script {
            name = "Deploy"
            scriptContent = """echo "Deploying %Customer% to testing""""
        }
    }
})


object Review : Project({
    name = "Review"

    buildType(Review_DeployToReview)
})

object Review_DeployToReview : BuildType({
    name = "Deploy to review"

    params {
        select("Customer", "", display = ParameterDisplay.PROMPT,
                options = listOf("customer1", "customer2", "customer3"))
    }

    vcs {
        root(Project_1)
    }

    steps {
        script {
            name = "Deploy to review"
            scriptContent = """echo "Deploying %Customer% to review for branch %teamcity.build.branch%""""
        }
    }
})
