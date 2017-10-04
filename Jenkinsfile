library "banno-jenkins-shared-pipelines@banno-scala-pipeline"
library "environments@master"

def version = bannoScalaPipeline(
    githubUrl: "https://github.com/Banno/template-service",
    slackChannel: "#template-alerts"
)

if (env.BRANCH_NAME == "master") {
    bannoEnvironmentsMarathonDeploy(
        appName: "template-service",
        slackChannel: "#template-alerts",
        slackTeamName: "@pupper",
        releaseVersion: version
    )
}
