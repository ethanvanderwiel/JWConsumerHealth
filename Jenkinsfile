#!/usr/bin/env groovy

slackChannel = "#team-pupper"
releasedVersion = ""

properties([buildDiscarder(logRotator(daysToKeepStr: '15'))])

catchError {
    if (env.BRANCH_NAME == "master") {
        stage("Release") {
            checkoutSbtAndBuild(sbtTask: "releaseIfChanged", captureReleasedVersion: true)
        }
        stage("Staging Deploy") {
            node("agent") {
                git url: "https://github.com/Banno/template-service.git"
                sh "git checkout ${releasedVersion}"
                dir("deployment/marathon"){
                    sh "./update-marathon.sh staging https://marathon.staging-2.banno-internal.com"
                }
            }
        }

        timeout(time: 1, unit: "DAYS") {
            notifySlackChannel(message: "Please approve UAT deploy", url: "${env.BUILD_URL}input")
            input message: "Promote to UAT?"
        }

        stage("UAT Deploy") {
            node("agent") {
                git url: "https://github.com/Banno/template-service.git"
                sh "git checkout ${releasedVersion}"
                dir("deployment/marathon") {
                    sh "./update-marathon.sh uat https://marathon.uat-2.banno-internal.com"
                }
            }
        }

        def changelog = askDevelopersForGoAheadForProd()
        emailOutReleaseNotesToApprovers(changelog)
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
            git url: "https://github.com/Banno/template-service.git", branch: "master", changelog: true
        } else {
            checkout scm
        }
        
        def sbtHome = tool("sbt 0.13.13")
        sh "git clean -ffdx"

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


String askDevelopersForGoAheadForProd() {
    stage("Prod Dev Approval") {

        def prodChangelog = createChangelog("production-2", "${releasedVersion}")
        notifySlackChannel(message: "Ready to send release notes for production. Please approve.", url: "${env.BUILD_URL}input")

        input(message: "Please input a high level changelog for this release ${releasedVersion} to send to approvers",
            parameters: [
                [$class: 'TextParameterDefinition', description: 'Changelog', name: 'changelog', defaultValue: prodChangelog]
            ])
    }
}


def emailOutReleaseNotesToApprovers(String changelog) {
    stage("Prod Email Approvers") {
        node("agent") {
            def checkout = "git checkout ${releasedVersion}"

            String emailBody = """
Please deploy once approved. Thanks!
To deploy:
ping @pupper that you are starting the deploy
clone https://github.com/Banno/template-service
git fetch --tags
${checkout}
run ./update-marathon.sh production https://marathon.production-2.banno-internal.com
${changelog}
      """
            retry(5) {
                emailext(
                    to: "approvers@banno.com",
                    replyTo: "noreply@banno.com",
                    subject: "[For Approval] Production template-service Release - Version ${releasedVersion}",
                    body: emailBody)
            }

        }

    }
}

def notifySlackChannel(Map<String,String> options) {
    def defaults = [url:     env.BUILD_URL,
        displayName: currentBuild.displayName,
        color:   "#DCDCDC"]
    def fullOptions = [:]
    fullOptions.putAll(defaults)
    fullOptions.putAll(options)

    def involvedPart = ""
    def currentlyInvolved = currentBuildUsersInvolved()
    if (currentlyInvolved != "") {
        involvedPart = " (involved: ${currentlyInvolved}) "
    } else {
        involvedPart = " "
    }

    slackSend(message: "${env.JOB_NAME} ${fullOptions.displayName}${involvedPart}<${fullOptions.url}|${fullOptions.message}>",
        channel: slackChannel,
        color:   fullOptions.color)
}

String currentBuildUsersInvolved() {
    return currentBuildCauseUserName() + " " + currentBuildChangeSetsAuthors()
}

@NonCPS
def currentBuildChangeSetsAuthors() {
    def currentChangeSets = currentBuild.rawBuild.changeSets
    def currentBuildChangeSetsAuthors = ""

    for (int i = 0; i < currentChangeSets.size(); i++) {
        def entries = currentChangeSets[i].items
        for (int j = 0; j < entries.size(); j++) {
            def entry = entries[j]
            if (!currentBuildChangeSetsAuthors.contains(entry.author.getFullName())) {
                currentBuildChangeSetsAuthors += "${entry.author.getFullName()} "
            }
        }
    }

    return currentBuildChangeSetsAuthors.trim()
}

String currentBuildCauseUserName() {
    def causes = currentBuild.rawBuild.causes
    for (int i = 0; i < causes.size(); i++) {
        def cause = causes.get(i)
        if (cause instanceof hudson.model.Cause.UserIdCause) {
            def userCause = (hudson.model.Cause.UserIdCause) cause
            return userCause.userName
        }
    }
}
