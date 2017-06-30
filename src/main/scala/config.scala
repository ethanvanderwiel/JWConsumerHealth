package com.banno.template

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
  postgresPasswordPath: String
) {
  val enabled: Boolean = address.nonEmpty
}

case class ServiceConfig(
  http: HttpConfig,
  postgres: PostgresConfig,
  vault: VaultConfig
)
