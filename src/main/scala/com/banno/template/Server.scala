package com.banno.template

import fs2._
import cats.effect._
import cats.implicits._
import com.banno.template.admin.AdminService
import com.banno.template.admin.AdminService.AdminServiceExports
import com.banno.template.config.ConfigService
import _root_.io.prometheus.client.CollectorRegistry
import org.http4s.server.prometheus.{PrometheusMetrics, PrometheusExportService}

import scala.concurrent.ExecutionContext

object Server {

  private val logger = org.log4s.getLogger

  def serve[F[_]](implicit Effect: Effect[F], EC: ExecutionContext): Stream[F, StreamApp.ExitCode] =
    for {
      Scheduler <- Scheduler(10)
      configService <- ConfigService.impl[F](Effect/*, Scheduler, EC*/)
      _ = configService.httpClient // TODO: Use HttpClient
      _ <- configService.serviceDiscovery // TODO: Use Service Discovery
      // _ <- configService.transactor // TODO: Use Transactor
      // _ <- Stream.eval(configService.runMigrations) // TODO: Uncomment When You Have Migrations
      cr = new CollectorRegistry() // Currently Doesnt Need any config options
      _ <- Stream.eval(PrometheusExportService.addDefaults[F](cr))

      AdminServiceExports(adminService, httpAdminService) = AdminService.service[F](cr)
      httpService <- Stream.eval(PrometheusMetrics[F](cr).run(httpAdminService))

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
