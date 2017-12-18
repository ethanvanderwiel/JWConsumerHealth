package com.banno.template

import scala.util.control.NoStackTrace

case class HttpConfig(
  host: String,
  port: Int
)

case class PostgresConfig(
  jdbcUrl: String,
  username: String,
  password: String,
  driver: String
)

case class VaultConfig(
  address: String,
  roleId: String,
  postgresCredsPath: String
) {
  val enabled: Boolean = address.nonEmpty
}

case class ServiceConfig(
  http: HttpConfig,
  postgres: PostgresConfig,
  vault: VaultConfig
)

case object NoPostgresUsername extends RuntimeException with NoStackTrace
case object NoPostgresPassword extends RuntimeException with NoStackTrace