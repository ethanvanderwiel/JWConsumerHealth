library "banno-jenkins-shared-pipelines@v1"

def version = bannoScalaPipeline(
    githubUrl: "https://github.com/Banno/template-service",
    slackChannel: "#template-alerts"
)

if (env.BRANCH_NAME == "master") {
    library "environments@master"

    bannoEnvironmentsMarathonDeploy(
        appName: "template-service",
        slackChannel: "#template-alerts",
        slackTeamName: "@placeholder",
        releaseVersion: version
    )
}
