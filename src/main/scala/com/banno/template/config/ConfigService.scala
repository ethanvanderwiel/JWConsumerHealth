package com.banno.template.config

import cats.effect._
import fs2._
// import com.banno.template.migrations.Migrations
import doobie.util.transactor.Transactor
// import scala.concurrent.ExecutionContext
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.server.blaze.BlazeBuilder

trait ConfigService[F[_]] {
  def primaryHttpServer: BlazeBuilder[F]
  def administrativeHttpServer: BlazeBuilder[F]
  def httpClient: Client[F]
  def transactor: Stream[F, Transactor[F]]
  def runMigrations: F[Unit]
}

object ConfigService {

  def impl[F[_]](implicit Effect: Effect[F] /*, S: Scheduler , ec: ExecutionContext*/ ): Stream[F, ConfigService[F]] =
    for {
      initConfig <- Stream.eval(SetupConfig.loadConfig[F])
      client <- Http1Client.stream[F]()
      // Uncomment the Below To Fetch Dynamic Credentials
      // updatedDbConfig <- SetupConfig.loadPostgresConfig[F](initConfig.postgres, initConfig.vault)(Effect, client, S, ec)
    } yield
      new ConfigService[F] {

        override def primaryHttpServer: BlazeBuilder[F] =
          BlazeBuilder[F](Effect).bindHttp(initConfig.http.port, "0.0.0.0")
        override def administrativeHttpServer: BlazeBuilder[F] =
          BlazeBuilder[F](Effect).bindHttp(initConfig.health.port, "0.0.0.0")
        override def httpClient: Client[F] = client
        // Uncomment Below to Load Transactor Correctly
        // override def transactor: Stream[F, Transactor[F]] = SetupConfig.loadConfigTransactor[F](updatedDbConfig)
        override def transactor: Stream[F, Transactor[F]] = ???
        // Uncomment Below to Enable Running Migrations
        // override def runMigrations: F[Unit] =
        //   Migrations.makeMigrations[F](updatedDbConfig.jdbcUrl, updatedDbConfig.username, updatedDbConfig.password)
        override def runMigrations: F[Unit] = ???
      }

}
