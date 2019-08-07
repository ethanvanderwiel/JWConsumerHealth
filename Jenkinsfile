library identifier: "banno-jenkins-shared-pipelines@v1", changelog: false

def version = bannoScalaPipeline(
    githubUrl: "https://github.com/ethanvanderwiel/jw-consumer-health"
)

if (env.BRANCH_NAME == "master") {
    library identifier: "environments@master", changelog: false

    bannoEnvironmentsMarathonDeploy(
        appName: "jw-consumer-health",
        releaseVersion: version
    )
}
