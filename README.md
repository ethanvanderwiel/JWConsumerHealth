### This is a template for deploying a service with Marathon.

This service is the minimum requirements for getting a service deployed on marathon with:
- Jenkinsfile for automatic builds and deploys to Staging/UAT
- Vault, for accessing Postgres or other secrets
- Writing logs to Kibana using logging-shim
- Health checks using Banno-health
- Metrics reporting
- Registering service for service discovery
- Commonly used Scala libraries are already added as dependencies
- Ping route

### Getting your own service up and running
1. Copy this repository.
1. Find/replace all occurences of `template-service` with your app's name.
1. Find/replace all occurences of `com.banno.template` with whatever you want your package named.
1. Verify that `template-service`, `template`, and `pupper` don't exist anywhere in your project.
1. At this point there is a small chicken and egg problem, to get around that, comment out these lines in your copied Jenkinsfile: https://github.com/Banno/template-service/blob/master/Jenkinsfile#L8-L17
1. Create a new repository on github and push up your project.
1. Follow the steps below to set up Jenkins for your new project.
1. Your app should have its first release.
1. If your app will be using Postgres, there is a [health check](https://github.com/Banno/template-service/blob/master/src/main/scala/Main.scala#L101) enabled by default and part of the configuration will be set up in the next step. If the app will not be using Postgres, the health check will need removed.
1. If your app needs to use Vault, refer to the [Vault AppRole setup docs](https://github.com/Banno/environments/blob/master/docs/vault-app-setup.md).
1. Refer to [Marathon configuration setup docs](https://github.com/Banno/environments/blob/master/docs/marathon-app-setup.md) for setting up your app in the environments repo. For the version, use the first release version created in the above steps.
1. Your app should be deployed.
1. You can now uncomment the lines that were commented out in the previous step: https://github.com/Banno/template-service/blob/master/Jenkinsfile#L8-L17
1. Make sure everything locally in your project is committed and pushed to the repository.
1. As your application is deployed through the environments you can [add nagios checks](https://github.com/banno/nagios-config#adding-checks-for-a-new-service) to be notified when the app is unhealthy.

### Setting up Jenkins for your new project
1. Go to Jenkins and click `New Item` at the top-left. (You must be logged in)
1. On the new page, type in the name of your new service. Do **_not_** click `OK` yet.
![Alt text](https://user-images.githubusercontent.com/3231194/27773261-5332a966-5f3b-11e7-8a51-aea095c9d1c8.png)
1. Go to the bottom and in the `copy from` field, find `template-service`. Click `OK`.
![Alt text](https://user-images.githubusercontent.com/3231194/27773262-533d2594-5f3b-11e7-9b48-e8fc755455e8.png)
1. After clicking `OK`, find your `Repository` in the `Github` section.
![Alt text](https://user-images.githubusercontent.com/3231194/27773258-53307bb4-5f3b-11e7-86d7-c320382ec358.png)
1. Feel free to change the description for the job.
1. `Save` and you're done. The job you have built uses the `Jenkinsfile` from your repository.
1. Build the project, if it didn't start on its own.
