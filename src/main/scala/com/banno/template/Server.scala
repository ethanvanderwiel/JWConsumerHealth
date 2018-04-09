package com.banno.template

import fs2._
import cats.effect.Effect
import cats.implicits._
import com.banno.template.admin.AdminService
import com.banno.template.admin.AdminService.AdminServiceExports
import com.banno.template.config.ConfigService
import com.banno.simplehealth.dropwizard.jvm.addAllMetrics
import org.http4s.server.metrics.{Metrics => MetricsMiddleware}

import scala.concurrent.ExecutionContext

object Server {

  private val logger = org.log4s.getLogger

  def serve[F[_]](implicit Effect: Effect[F], EC: ExecutionContext): Stream[F, StreamApp.ExitCode] =
    for {
      Scheduler <- Scheduler(10)
      configService <- ConfigService.impl[F](Effect/*, Scheduler, EC*/)
      _ = configService.httpClient // TODO: Use HttpClient
      _ <- configService.serviceDiscovery // TODO: Use Service Discovery
      metricRegistry <- configService.graphiteRegistry
      // _ <- configService.transactor // TODO: Use Transactor
      // _ <- Stream.eval(configService.runMigrations) // TODO: Uncomment When You Have Migrations
      
      _ <- Stream.eval(addAllMetrics[F](metricRegistry))
      AdminServiceExports(adminService, httpAdminService) = AdminService.service[F](metricRegistry)
      httpService = MetricsMiddleware(metricRegistry, "com.banno.template.health")(Effect)(httpAdminService)

      _ <- configService.registerInstance
      exitCode <- Stream(
        configService.primaryHttpServer
          .mountService(httpService, "/")
          .serve,
        configService.administrativeHttpServer
          .mountService(adminService, "/")
          .serve
      )
        .join(2)
        .concurrently(
          Stream.eval(Effect.delay(logger.info("template-service has started")))
        )

    } yield exitCode

}
