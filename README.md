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
2. Find/replace all occurences of `template-service` with your app's name.
3. Find/replace all occurences of `com.banno.template` with whatever you want your package named.
4. Verify that `template-service` and `template` don't exist anywhere in your project.
5. Create a new repository on github and push up your project.

Your project is now ready to be deployed to Marathon. There are a few ways you can do this, and we're going to use Jenkins.

### Setting up Jenkins for your new project
1. Go to Jenkins and click `New Item` at the top-left. (You must be logged in)
2. On the new page, type in the name of your new service. Do **_not_** click `OK` yet.
![Alt text](https://user-images.githubusercontent.com/3231194/27773261-5332a966-5f3b-11e7-8a51-aea095c9d1c8.png)
3. Go to the bottom and in the `copy from` field, find `template-service`. Click `OK`.
![Alt text](https://user-images.githubusercontent.com/3231194/27773262-533d2594-5f3b-11e7-9b48-e8fc755455e8.png)
4. After clicking `OK`, find your `Repository` in the `Github` section.
![Alt text](https://user-images.githubusercontent.com/3231194/27773258-53307bb4-5f3b-11e7-86d7-c320382ec358.png)
5. Feel free to change the description for the job.
6. `Save` and you're done. The job you have built uses the `Jenkinsfile` from your repository.
7. Build the project, if it didn't start on its own.
8. Your project should be deployed on Marathon Staging/UAT.

### Vault Setup

Please refer to the [Vault AppRole setup docs](https://github.com/Banno/environments/blob/master/docs/vault-app-setup.md)
