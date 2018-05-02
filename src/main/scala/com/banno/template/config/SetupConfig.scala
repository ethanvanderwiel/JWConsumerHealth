package com.banno.template.config

import fs2.{Stream, Scheduler}
import cats.implicits._
import cats.effect.{Effect, Async, Sync}
import com.typesafe.config.ConfigFactory
import Configurations._
import cats.Alternative
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
  ): Stream[F,PostgresConfig] = {

    val retryC =  Retry[F](RetryPolicy(RetryPolicy.exponentialBackoff(2.seconds, 5)))(C)

    def getFromVault: Stream[F, PostgresConfig] =
      for {
        uri <- Uri.fromString(v.address).fold(e => Stream.raiseError[Uri](e), Stream.emit(_)).covary[F]
        token <- Stream.eval(Vault.login[F](retryC, uri)(v.roleId)(F))
        credentials <- Stream.eval(
          Vault.readSecret[F, DynamicCredentials](retryC, uri)(token.clientToken, v.postgresCredsPath)
        )
        out <- Stream.emit(dbConfig.copy(username = credentials.data.username, password = credentials.data.password))
          .concurrently(
            Stream.eval(Vault.renewSelfToken(retryC, uri)(token, 25.hours)) ++
            S.fixedDelay(24.hours) >> Stream.eval(Vault.renewSelfToken(retryC, uri)(token, 25.hours))
          )
          .concurrently(
            S.sleep((credentials.renewal.leaseDuration-10).seconds) ++
            Stream.eval(
              Sync[F].delay(logger.info(show"Attempting To Do An Initial Renew LeaseId ${credentials.renewal.leaseId}")) *>
              Vault.renewLease(retryC, uri)(credentials.renewal.leaseId, 25.hours, token.clientToken)
            ) ++
            S.fixedDelay(24.hours) >> Stream.eval(
              Sync[F].delay(logger.info(show"Attempting To Renew LeaseId ${credentials.renewal.leaseId}")) *>
              Vault.renewLease(retryC, uri)(credentials.renewal.leaseId, 25.hours, token.clientToken)
            )
          )
      } yield out

    Alternative[Option]
      .guard(!v.address.isEmpty)
      .fold(dbConfig.pure[Stream[F, ?]])(_ => getFromVault)
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
