package com.banno.template.config

import cats.effect._
import fs2._
// import com.banno.template.migrations.Migrations
// import com.banno.zookeeper.Zookeeper
// import com.banno.zookeeper.{ServiceDiscovery => SD}
// import com.banno.zookeeper.http4s.HttpServiceDiscovery
import doobie.util.transactor.Transactor
import java.net.InetAddress
// import scala.concurrent.ExecutionContext
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.server.blaze.BlazeBuilder

trait ConfigService[F[_]] {
  def primaryHttpServer: BlazeBuilder[F]
  def administrativeHttpServer: BlazeBuilder[F]
  def httpClient: Client[F]
// uncomment below if you need ZK based service discover should be able to use marathon LB instead
//  def serviceDiscovery: Stream[F, HttpServiceDiscovery[F]]
  def transactor: Stream[F, Transactor[F]]
// uncomment below if you need ZK based service discover should be able to use marathon LB instead
//  def registerInstance: Stream[F, Unit]
  def runMigrations: F[Unit]
}

object ConfigService {

  def impl[F[_]](implicit Effect: Effect[F] /*, S: Scheduler , ec: ExecutionContext*/ ): Stream[F, ConfigService[F]] =
    for {
      initConfig <- Stream.eval(SetupConfig.loadConfig[F])
      client <- Http1Client.stream[F]()
      // uncomment below if you need ZK based service discover should be able to use marathon LB instead
//      curator <- Zookeeper.stream(Zookeeper.fromConnectString(initConfig.zookeeper.quorum))
      hostname <- Stream.eval(Sync[F].delay {
        sys.env.get("HOST").orElse(sys.env.get("HOSTNAME")).getOrElse(InetAddress.getLocalHost.getHostName)
      })
      // Uncomment the Below To Fetch Dynamic Credentials
      // updatedDbConfig <- SetupConfig.loadPostgresConfig[F](initConfig.postgres, initConfig.vault)(Effect, client, S, ec)
    } yield
      new ConfigService[F] {

        override def primaryHttpServer: BlazeBuilder[F] =
          BlazeBuilder[F](Effect).bindHttp(initConfig.http.port, "0.0.0.0")
        override def administrativeHttpServer: BlazeBuilder[F] =
          BlazeBuilder[F](Effect).bindHttp(initConfig.health.port, "0.0.0.0")
        override def httpClient: Client[F] = client
        // If you need the full DiscoveredServiceInstance instead use
        // com.banno.zookeeper.tagless.ServiceDiscovery
        // uncomment below if you need ZK based service discover should be able to use marathon LB instead
        /*        override def serviceDiscovery: Stream[F, HttpServiceDiscovery[F]] =
          HttpServiceDiscovery.fromCuratorFramework(curator)*/
        // Uncomment Below to Load Transactor Correctly
        // override def transactor: Stream[F, Transactor[F]] = SetupConfig.loadConfigTransactor[F](updatedDbConfig)
        override def transactor: Stream[F, Transactor[F]] = ???
        // uncomment below if you need ZK based service discover should be able to use marathon LB instead
        /*override def registerInstance: Stream[F, Unit] = SD.registerInstanceStreamed(
          curator,
          initConfig.registration.path,
          initConfig.registration.`type`,
          initConfig.registration.name,
          initConfig.http.port,
          None,
          None,
          hostname,
          ""
        )*/
        // Uncomment Below to Enable Running Migrations
        // override def runMigrations: F[Unit] =
        //   Migrations.makeMigrations[F](updatedDbConfig.jdbcUrl, updatedDbConfig.username, updatedDbConfig.password)
        override def runMigrations: F[Unit] = ???
      }

}
