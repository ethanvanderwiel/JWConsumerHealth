library "banno-jenkins-shared-pipelines@v2-candidate"
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
