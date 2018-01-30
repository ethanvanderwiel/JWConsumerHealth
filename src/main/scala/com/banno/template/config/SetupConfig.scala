package com.banno.template.config

import fs2.Stream
import cats.implicits._
import cats.effect.{Async, Sync}
import com.typesafe.config.ConfigFactory
import Configurations._
import cats.Alternative
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.http4s.client.Client
import com.banno.vault.Vault
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

private[config] object SetupConfig {

  val logger = org.log4s.getLogger

  def loadConfig[F[_]](implicit F: Sync[F]): F[ServiceConfig] = F.delay {
    val config = ConfigFactory.load()
    pureconfig.loadConfigOrThrow[ServiceConfig](config, "com.banno.template")
  }

  def loadPostgresConfig[F[_]](dbConfig: PostgresConfig, v: VaultConfig)(
      implicit F: Async[F],
      C: Client[F]): F[PostgresConfig] = {
    case class DynamicCredentials(username: String, password: String)
    object DynamicCredentials {
      implicit val dynCredDecoder: Decoder[DynamicCredentials] = deriveDecoder[DynamicCredentials]
    }
    def getFromVault =
      for {
        token <- Vault.login[F](C, v.address)(v.roleId)(F)
        credentialsE <- Vault
          .readSecret[F, DynamicCredentials](C, v.address)(token.clientToken, v.postgresCredsPath)
          .attempt
        credentials <- credentialsE.fold(
          e => F.delay(logger.error(e)("Failed To Get Expected Credentials")) >> F.raiseError[DynamicCredentials](e),
          _.pure[F]
        )
      } yield dbConfig.copy(username = credentials.username, password = credentials.password)

    Alternative[Option]
      .guard(!v.address.isEmpty)
      .fold(dbConfig.pure[F])(_ => getFromVault)
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

}
