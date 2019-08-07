package com.banno.jabberwocky.consumer.health

import fs2._
import cats.effect._
import cats.implicits._
import com.banno.jabberwocky.consumer.health.admin.AdminService
import com.banno.jabberwocky.consumer.health.admin.AdminService.AdminServiceExports
import com.banno.jabberwocky.consumer.health.config.ConfigService
import _root_.io.prometheus.client.CollectorRegistry
import org.http4s.server.prometheus.{PrometheusExportService, PrometheusMetrics}

import scala.concurrent.ExecutionContext

object Server {

  private val logger = org.log4s.getLogger

  def serve[F[_]](implicit Effect: Effect[F], EC: ExecutionContext): Stream[F, StreamApp.ExitCode] =
    for {
      scheduler <- Scheduler(2)
      configService <- ConfigService.impl[F](Effect /*, scheduler, EC*/ )
      _ = configService.httpClient // TODO: Use HttpClient
      // _ <- configService.transactor // TODO: Use Transactor
      // _ <- Stream.eval(configService.runMigrations) // TODO: Uncomment When You Have Migrations
      cr = new CollectorRegistry() // Currently Doesn't Need any config options
      _ <- Stream.eval(PrometheusExportService.addDefaults[F](cr))

      AdminServiceExports(adminService, httpAdminService) = AdminService.service[F](cr)
      httpService <- Stream.eval(PrometheusMetrics[F](cr).run(httpAdminService))
      exitCode <- Stream(
        configService.primaryHttpServer
          .mountService(httpService, "/")
          .serve,
        configService.administrativeHttpServer
          .mountService(adminService, "/")
          .serve
      ).join(2)
        .concurrently(
          Stream.eval(Effect.delay(logger.info("jw-consumer-health has started")))
        )

    } yield exitCode

}
