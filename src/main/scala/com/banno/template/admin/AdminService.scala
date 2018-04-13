package com.banno.template.admin

import cats._
import cats.implicits._
import cats.effect._
import com.banno.simplehealth._
import com.banno.simplehealth.prometheus.PrometheusMetricsResponse
import com.banno.simplehealth.HealthChecker._
import io.prometheus.client.CollectorRegistry
import org.http4s._
import org.http4s.dsl._
import scala.concurrent.ExecutionContext

object AdminService {
  final case class AdminServiceExports[F[_]](adminServerService: HttpService[F], httpServerService: HttpService[F])

  /**
    * Publishes The Exports For Both the Http and Administrative Http Services 
    * Using a core set of these building blocks you can build as many checks
    * as necessary to deliver proper health responses for your service.
    */
  def service[F[_]: Effect](cr: CollectorRegistry)(implicit ec: ExecutionContext): AdminServiceExports[F] = {
    implicit val healthChecker : HealthChecker[F] = HealthChecker.impl[F](HealthChecks.checks[F])
    implicit val metricsResp : MetricsResponse[F] = PrometheusMetricsResponse.impl[F](cr)
    implicit val buildInfo : SimpleBuildInfo[F] = com.banno.simplehealth.SimpleBuildInfo.impl[F](
      com.banno.BuildInfo.name,
      com.banno.BuildInfo.version,
      com.banno.BuildInfo.scalaVersion,
      com.banno.BuildInfo.sbtVersion
    )
    implicit val heapDump : HeapDump[F] = HeapDump.impl[F]
    implicit val threadObserver : ThreadObserver[F] = ThreadObserver.impl[F]

    val adminService = AdministrativeService.service[F]
    val legacy = legacyService[F](healthChecker)

    AdminServiceExports[F](adminService, legacy)
  }

  private def legacyService[F[_]: Effect](h: HealthChecker[F])(implicit ec: ExecutionContext): HttpService[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpService[F]{
      case GET -> Root / "ping" => Ok("pong")
      case GET -> Root / "health" => h.resp
    }
  } 

  object HealthChecks {
    def checks[F[_]: Sync]: List[HealthCheck[F]] = List(
      basicAvailablility[F],
      dropwizard.checks.deadlockedThreads[F]

    )
    private def basicAvailablility[F[_]: Applicative]: HealthCheck[F] = HealthCheck[F]("service-is-up", true.pure[F])

  }
}