package com.banno.ssltestclient

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

case class ServiceConfig(
  http: HttpConfig,
  postgres: PostgresConfig
)
