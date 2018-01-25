package com.banno.template.config

import cats.effect.Effect
import fs2._
import com.banno.template.migrations.Migrations
import com.banno.zookeeper.Zookeeper
import com.banno.zookeeper.{ServiceDiscovery => SD}
import com.banno.zookeeper.tagless.ServiceDiscovery
import doobie.util.transactor.Transactor
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.server.blaze.BlazeBuilder

trait ConfigService[F[_]] {
  def primaryHttpServer: BlazeBuilder[F]
  def administrativeHttpServer: BlazeBuilder[F]
  def httpClient : Client[F]
  def serviceDiscovery: Stream[F, ServiceDiscovery[F]]
  def transactor: Stream[F, Transactor[F]]
  def registerInstance: Stream[F, Unit]
  def runMigrations: F[Unit]
}

object ConfigService {

  def impl[F[_]](implicit Effect: Effect[F]): Stream[F, ConfigService[F]] =
    for {
      initConfig <- Stream.eval(SetupConfig.loadConfig[F])
      client <- Http1Client.stream[F]()
      curator <- Zookeeper.stream(Zookeeper.fromConnectString(initConfig.zookeeper.quorum))
      address <- Stream.eval(InetAddressConfig.getValidAddressOrFail[F])
      hostname <- Stream.eval(InetAddressConfig.getHostname[F])
      updatedDbConfig <- Stream.eval(SetupConfig.loadPostgresConfig[F](initConfig.postgres, initConfig.vault)(Effect, client))
    } yield
      new ConfigService[F] {
        override def primaryHttpServer: BlazeBuilder[F] =
          BlazeBuilder[F](Effect).bindHttp(initConfig.http.port, "0.0.0.0")
        override def administrativeHttpServer: BlazeBuilder[F] =
          BlazeBuilder[F](Effect).bindHttp(initConfig.health.port, "0.0.0.0")
        override def httpClient: Client[F] = client
        override def serviceDiscovery: Stream[F, ServiceDiscovery[F]] = ServiceDiscovery.fromCuratorFramework(curator)
        override def transactor: Stream[F, Transactor[F]] = SetupConfig.loadConfigTransactor[F](updatedDbConfig)
        override def registerInstance: Stream[F, Unit] = SD.registerInstanceStreamed(
          curator,
          initConfig.registration.path,
          initConfig.registration.`type`,
          initConfig.registration.name,
          initConfig.http.port,
          None,
          Some(address.getHostAddress),
          hostname,
          ""
        )
        override def runMigrations: F[Unit] =
          Migrations.makeMigrations[F](updatedDbConfig.jdbcUrl, updatedDbConfig.username, updatedDbConfig.password)
      }

}
