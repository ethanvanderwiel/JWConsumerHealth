library "banno-jenkins-shared-pipelines@v1"

def version = bannoScalaPipeline(
    githubUrl: "https://github.com/Banno/ssl-test-client",
    slackChannel: "#team-pupper"
)

//if (env.BRANCH_NAME == "master") {
//    library "environments@master"
//
//    bannoEnvironmentsMarathonDeploy(
//        appName: "ssl-test-client",
//        slackChannel: "#team-pupper",
//        slackTeamName: "@pupper",
//        releaseVersion: version
//    )
//}
