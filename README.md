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
1. Go to Jenkins and click `New Item` at the top-left.
2. On the new page, go to the bottom and in the `copy from` field, find `template-service`. 
3. Once that drop-down is populated go back to the top and name your project and press `Ok`. 



4. 
