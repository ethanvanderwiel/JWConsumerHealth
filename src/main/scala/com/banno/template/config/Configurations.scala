package com.banno.template.config

private[config] object Configurations {

  final case class ServiceConfig(
    http: HttpConfig,
    health: HttpConfig,
    zookeeper: ZookeeperConfig,
    registration: Registration,
    postgres: PostgresConfig,
    vault: VaultConfig
  )

  final case class Registration(
    path: String,
    name: String,
    `type`: String
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
    postgresCredsPath: String
  )

  final case class ZookeeperConfig(
    quorum: String
  )

}
