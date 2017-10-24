package com.banno.template

import com.banno.BuildInfo
import com.banno.config.discovery.{DiscoveredServiceInstance, ServiceDiscovery}
import com.banno.health.{GraphiteReporter, Health}
import com.banno.vault.client.{AppRoleVaultAuth, DefaultVault, Vault}
import com.banno.vault.VaultClient

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry

import com.typesafe.config.ConfigFactory

import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.metrics._
import org.http4s.util.ProcessApp
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scalaz.stream._

object Main extends ProcessApp {

  private[this] val logger = org.log4s.getLogger
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private[this] var discoveredInstance: Option[DiscoveredServiceInstance] = None

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def process(args: List[String]) =
    (for {
      c0   <- Process.eval(loadConfig)
      c    <- Process.eval(addVaultSecretsToConfig(c0))
      mr   =  new MetricRegistry()
      srvc =  service(mr)
      _    <- Process.eval(setupHealthChecks(mr))
      _    <- Process.eval(setupMetricsReporter(mr))
      b    <- startBlazeServer(c.http, service(mr)) merge
               Process.eval_ { registerHttpIntoServiceDiscovery(c.http) } merge
               Process.eval_ { printOutStarted }
     } yield b) onComplete {
      Process.eval_(unregisterFromServiceDiscovery)
    }

  def service(metricRegistry: MetricRegistry) = Metrics(metricRegistry)(PingRoute.pingRouteService)

  def startBlazeServer(config: HttpConfig, service: HttpService) =
    BlazeBuilder
      .bindHttp(config.port, config.host)
      .mountService(service, "/")
      .serve

  val printOutStarted: Task[Unit] = Task.delay {
    logger.info("template-service has started")
  }

  val loadConfig: Task[ServiceConfig] = Task.delay {
    val config = ConfigFactory.load()
    pureconfig.loadConfigOrThrow[ServiceConfig](config, "com.banno.template")
  }

  def addVaultSecretsToConfig(config: ServiceConfig): Task[ServiceConfig] = {
    if (config.vault.enabled) updateConfigWithVaultSecrets(config) else Task.now(config)
  }

  def updateConfigWithVaultSecrets(config: ServiceConfig): Task[ServiceConfig] = {
    for {
      vaultClient <- createVaultClient(config.vault)
      postgresPassword <- Task.fromDisjunction { vaultClient.readSecretOrFail(config.vault.postgresPasswordPath).toDisjunction }
    } yield {
      config.copy(postgres = config.postgres.copy(password = postgresPassword))
    }
  }

  def createVaultClient(vaultConfig: VaultConfig): Task[Vault] = Task.delay {
    new DefaultVault(
      auth = new AppRoleVaultAuth(vaultConfig.roleId),
      vaultClient = new VaultClient(List(vaultConfig.address))
    )
  }

  def setupHealthChecks(registry: MetricRegistry): Task[Health] = Task.delay {
    val health = new Health {
      val criticalHealthCheckRegistry = new HealthCheckRegistry
      val warningHealthCheckRegistry = new HealthCheckRegistry
      val metricRegistry: MetricRegistry = registry
    }

    health.checkVM()
    health.checkHighLoggedErrorRate()
    val server = health.exposeOverHttp()

    health
  }

  def setupMetricsReporter(registry: MetricRegistry): Task[GraphiteReporter] = Task.delay {
    val reporter = new GraphiteReporter {
      val metricRegistry: MetricRegistry = registry
    }

    reporter.setupGraphiteReporter()

    reporter
  }

  def registerHttpIntoServiceDiscovery(config: HttpConfig): Task[Unit] = Task.delay {
    discoveredInstance = Some(
      ServiceDiscovery.registerInstance(
        name = BuildInfo.name,
        serviceType = "http",
        port = config.port,
        group = None
      )
    )
  }

  def unregisterFromServiceDiscovery: Task[Unit] = Task.delay {
    discoveredInstance.foreach { i =>
      logger.info(s"unregistering ${i.name} from service discovery")
      ServiceDiscovery.unregisterInstance(i)
    }
  }

}
