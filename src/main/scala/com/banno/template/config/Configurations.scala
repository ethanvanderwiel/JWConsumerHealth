package com.banno.jabberwocky.consumer.health.config

import scala.concurrent.duration._

private[config] object Configurations {

  final case class ServiceConfig(
      http: HttpConfig,
      health: HttpConfig,
      postgres: PostgresConfig,
      vault: VaultConfig
  )

  final case class HttpConfig(
      host: String,
      port: Int
  )

  final case class PostgresConfig(
      jdbcUrl: String,
      username: String,
      password: String,
      driver: String
  )

  final case class VaultConfig(
      address: String,
      roleId: String,
      postgresCredsPath: String,
      leaseDuration: FiniteDuration,
      leaseRenewWait: FiniteDuration
  )

}
