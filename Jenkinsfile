library identifier: "banno-jenkins-shared-pipelines@v1", changelog: false

def version = bannoScalaPipeline(
    githubUrl: "https://github.com/Banno/template-service",
    slackChannel: "#template-alerts"
)

if (env.BRANCH_NAME == "master") {
    library identifier: "environments@master", changelog: false

    bannoEnvironmentsMarathonDeploy(
        appName: "template-service",
        slackChannel: "#template-alerts",
        slackTeamName: "@pupper",
        releaseVersion: version
    )
}
