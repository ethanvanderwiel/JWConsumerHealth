releasedVersion = ""

properties([buildDiscarder(logRotator(daysToKeepStr: '15'))])

catchError {
    if (env.BRANCH_NAME == "master") {
        stage("Release") {
            checkoutSbtAndBuild(sbtTask: "releaseIfChanged", captureReleasedVersion: true)
        }
        stage("Staging Deploy") {
            node("agent") {
                git url: "https://github.com/Banno/symxchange-api.git"
                sh "git checkout ${releasedVersion}"
                dir("deployment/marathon"){
                    sh "./update-marathon.sh staging https://marathon.staging-2.banno-internal.com"
                }
            }
        }

        stage("UAT Deploy") {
            node("agent") {
                git url: "https://github.com/Banno/symxchange-api.git"
                sh "git checkout ${releasedVersion}"
                dir("deployment/marathon") {
                    sh "./update-marathon.sh uat https://marathon.uat-2.banno-internal.com"
                }
            }
        }
    }
    else {
        stage("Build") {
            checkoutSbtAndBuild(sbtTask: "clean test")
        }
    }
}



def checkoutSbtAndBuild(Map build) {
    def defaults = [sbtTask: "compile", captureReleasedVersion: false]
    def fullBuild = [:]
    fullBuild.putAll(defaults)
    fullBuild.putAll(build)

    node("agent") {
        if (env.BRANCH_NAME == "master") {
            git url: "https://github.com/Banno/symxchange-api.git", branch: "master", changelog: true
        } else {
            checkout scm
        }
        def sbtHome = tool("sbt 0.13.13")

        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
            timeout(time: 20, unit: 'MINUTES') {
                try {
                    sh "${sbtHome}/bin/sbt ${fullBuild.sbtTask}"

                    if (fullBuild.captureReleasedVersion) {
                        releasedVersion = sh(script: "git describe --abbrev=0 --tags", returnStdout: true).trim()
                    }
                } catch(err) {
                    step([$class: 'JUnitResultArchiver', testResults: 'target/test-reports/*.xml'])
                    throw err
                }
            }
        }
    }
}
