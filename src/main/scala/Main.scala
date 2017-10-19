package com.banno.ssltestclient

import com.banno.BuildInfo
import com.banno.config.discovery.{DiscoveredServiceInstance, ServiceDiscovery}
import com.banno.health.{GraphiteReporter, Health}

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry

import com.typesafe.config.ConfigFactory

import org.http4s.HttpService
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.metrics._
import org.http4s.server.Router
import org.http4s.server.{Server, ServerApp}

import scalaz._, Scalaz._
import scalaz.concurrent.Task

object Main extends ServerApp {

  private[this] val logger = org.log4s.getLogger
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private[this] var discoveredInstance: Option[DiscoveredServiceInstance] = None


  override def server(args: List[String]): Task[Server] =
    for {
      c    <- loadConfig
      mr   =  new MetricRegistry()
      srvc =  service(mr)
      _    <- setupHealthChecks(mr)
      _    <- setupMetricsReporter(mr)
      b    <- startBlazeServer(c.http, srvc)
      _    <- registerHttpIntoServiceDiscovery(c.http)
      _    <- printOutStarted
    } yield b

  def service(metricRegistry: MetricRegistry) = Metrics(metricRegistry)(PingRoute.pingRouteService)

  def startBlazeServer(config: HttpConfig, service: HttpService): Task[Server] = BlazeBuilder
    .bindHttp(config.port, config.host)
    .mountService(service, "/")
    .start

  val printOutStarted: Task[Unit] = Task.delay {
    logger.info("ssl-test-client has started")
  }

  val loadConfig: Task[ServiceConfig] = Task.delay {
    val config = ConfigFactory.load()
    pureconfig.loadConfigOrThrow[ServiceConfig](config, "com.banno.ssltestclient")
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

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def unregisterFromServiceDiscovery: Task[Unit] = Task.delay {
    discoveredInstance.foreach { i =>
      logger.info(s"unregistering ${i.name} from service discovery")
      ServiceDiscovery.unregisterInstance(i)
    }
  }

  override def shutdown(server: Server): Task[Unit] = {
    unregisterFromServiceDiscovery <* super.shutdown(server)
  }

}
