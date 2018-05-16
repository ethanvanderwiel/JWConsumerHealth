package com.banno.template.config

import fs2.{Stream, Scheduler}
import cats.implicits._
import cats.effect.{Effect, Async, Sync}
import com.typesafe.config.ConfigFactory
import Configurations._
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware._
import com.banno.vault.Vault
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

private[config] object SetupConfig {

  val logger = org.log4s.getLogger

  def loadConfig[F[_]](implicit F: Sync[F]): F[ServiceConfig] = F.delay {
    val config = ConfigFactory.load()
    pureconfig.loadConfigOrThrow[ServiceConfig](config, "com.banno.template")
  }

  def loadPostgresConfig[F[_]](dbConfig: PostgresConfig, v: VaultConfig)(
      implicit F: Effect[F],
      C: Client[F],
      S: Scheduler,
      ec: ExecutionContext
  ): Stream[F,PostgresConfig] =
    if(v.address.isEmpty)
      Stream.eval( F.pure(dbConfig))
    else {
      val retryC = Retry[F](RetryPolicy(RetryPolicy.exponentialBackoff(2.seconds, 5)))(C)
      for {
        uri <- Uri.fromString(v.address).fold(e => Stream.raiseError[Uri](e), Stream.emit(_)).covary[F]
        credentials <- Vault.loginAndKeepSecretLeased[F, DynamicCredentials](retryC, uri)(
          v.roleId,
          v postgresCredsPath,
          v.leaseDuration,
          v.leaseRenewWait)
      } yield dbConfig.copy(username = credentials.username, password = credentials.password)
    }

  def loadConfigTransactor[F[_]](dbConfig: PostgresConfig)(implicit F: Async[F]): Stream[F, Transactor[F]] =
    for {
      _ <- Stream.eval(
        F.delay(logger.info(s"Attempting to Connect to Database - '${dbConfig.jdbcUrl}' as '${dbConfig.username}'"))
      )
      transactor <- HikariTransactor.stream[F](dbConfig.driver, dbConfig.jdbcUrl, dbConfig.username, dbConfig.password)
      _ <- Stream.eval(
        F.delay(logger.info(s"transactor for postgres connected to '${dbConfig.jdbcUrl}' as '${dbConfig.username}'"))
      )
    } yield transactor

    final case class DynamicCredentials(username: String, password: String)
    object DynamicCredentials {
      implicit val dynCredDecoder: Decoder[DynamicCredentials] = deriveDecoder[DynamicCredentials]
    }

}
