package com.banno.template.config

private[config] object Configurations {

  final case class ServiceConfig(
    http: HttpConfig,
    health: HttpConfig,
    zookeeper: ZookeeperConfig,
    registration: Registration,
    postgres: PostgresConfig,
    vault: VaultConfig,
    graphite: GraphiteConfig
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

  final case class GraphiteConfig(
    enabled: Boolean, 
    prefix: String, 
    identifier: Option[String], 
    instanceIdentifier: Option[String], 
    port: Int, 
    host: String
  )

}
