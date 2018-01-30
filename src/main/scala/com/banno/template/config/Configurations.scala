package com.banno.template.config

private[config] object Configurations {

  case class ServiceConfig(
      http: HttpConfig,
      health: HttpConfig,
      zookeeper: ZookeeperConfig,
      registration: Registration,
      postgres: PostgresConfig,
      vault: VaultConfig
  )

  case class Registration(
      path: String,
      name: String,
      `type`: String
  )

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
  )

  case class ZookeeperConfig(
      quorum: String
  )

}
