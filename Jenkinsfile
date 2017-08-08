@Library("banno-jenkins-shared-pipelines@scala-marathon-app-pipeline") // pointed at branch to test it out
import com.banno.jenkins.pipelines.v1.StandardMarathonScalaPipeline

new StandardMarathonScalaPipeline(
    githubUrl: "https://github.com/Banno/template-service",
    slackChannel: "#team-pupper",
    teamName: "@pupper"
).run()
