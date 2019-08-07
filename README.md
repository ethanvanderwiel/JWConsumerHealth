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

#### Deploying your service

1. Copy this repository.
1. Find/replace all occurences of `jw-consumer-health` with your app's name.
1. Find/replace all occurences of `com.banno.jabberwocky.consumer.health` with whatever you want your package named.
1. Verify that `jw-consumer-health`, `template`, and `aviato` don't exist anywhere in your project.
1. Create a new repository on github and push up your project.
1. Follow the steps below to [set up Jenkins for your new project](#setting-up-jenkins-for-your-new-project).
1. Your app should have its first release.
1. Refer to [Marathon configuration setup docs](https://github.com/Banno/environments/blob/master/docs/marathon-app-setup.md) for setting up your app in the environments repo. For the version, use the first release version created in the above steps.
1. Your app should be deployed.
1. Make sure everything locally in your project is committed and pushed to the repository.
1. If your app needs to use Vault, follow the steps below to [set up Vault in your service](#setting-up-vault-for-your-new-project).
1. If your app will be using Postgres, follow the steps below to [set up Postgres in your service](#setting-up-postgres-for-your-new-project).

### Setting up Jenkins for your new project

1. Go to Jenkins and click `New Item` at the top-left. (You must be logged in)
1. On the new page, type in the name of your new service. Do **_not_** click `OK` yet.
![Alt text](https://user-images.githubusercontent.com/3231194/27773261-5332a966-5f3b-11e7-8a51-aea095c9d1c8.png)
1. Go to the bottom and in the `copy from` field, find `jw-consumer-health`. Click `OK`.
![Alt text](https://user-images.githubusercontent.com/3231194/27773262-533d2594-5f3b-11e7-9b48-e8fc755455e8.png)
1. After clicking `OK`, find your `Repository` in the `Github` section.
![Alt text](https://user-images.githubusercontent.com/3231194/27773258-53307bb4-5f3b-11e7-86d7-c320382ec358.png)
1. Feel free to change the description for the job.
1. `Save` and you're done. The job you have built uses the `Jenkinsfile` from your repository.
1. Build the project, if it didn't start on its own.

### Setting up Vault for your new project

1. Follow the [Vault AppRole setup docs](https://github.com/Banno/environments/blob/master/docs/vault-app-setup.md) in the environments repo.
1. Uncomment evaluating adding vault secrets to the service config. The `process` function in the `Main` object should look like:

``` scala
  def process(args: List[String]) =
    (for {
      c0   <- Process.eval(loadConfig)
      c    <- Process.eval(addVaultSecretsToConfig(c0))
      mr   =  new MetricRegistry()
      ...
      b    <- startBlazeServer(c.http, service(mr)) merge
               Process.eval(registerHttpIntoServiceDiscovery(c.http)) merge
               Process.eval(printOutStarted)
     } yield b).drain onComplete {
      Process.eval_(unregisterFromServiceDiscovery)
  }
```

1. Open a PR with the changes to be reviewed and released.

### Setting up Postgres for your new project

Each team has their own Postgres server for their services to use. The beginning of the hostname will be needed. ie for `postgres-aviato0-<dc>.<env>.banno-internal.com` it would be `postgres-aviato`

1. Follow the [Postgres dynamic credentials docs](https://github.com/Banno/environments/blob/master/docs/dynamic-postgres-creds.md) in the environments repo.
1. Uncomment the Postgres creds addition to the config. The `updateConfigWithVaultSecrets` function in the `Main` object should look like:

``` scala
def updateConfigWithVaultSecrets(config: ServiceConfig): Task[ServiceConfig] = {
    for {
      vaultClient <- createVaultClient(config.vault)
      postgresCreds <- Task.fromDisjunction { vaultClient.readPath(config.vault.postgresCredsPath).toDisjunction }
      username <- Task.fromDisjunction { postgresCreds.get("username") \/> NoPostgresUsername }
      password <- Task.fromDisjunction { postgresCreds.get("password") \/> NoPostgresPassword }
      ...
    } yield {
      config.copy(postgres = config.postgres.copy(username = username, password = password))
    }
  }
```
1. Uncomment the Postgres health check setup. The `setupHealthChecks` function should have the following:

``` scala
    def dbIsAlive: Task[Unit] = sql"SELECT 1".query[Int].option.transact(transactor).void

    health.check("postgres-is-alive", true)(dbIsAlive.unsafePerformSync)
```
1. Open a PR with the changes to be reviewed and released.
