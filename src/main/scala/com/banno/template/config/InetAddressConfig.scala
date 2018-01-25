package com.banno.template.config

import java.net.{InetAddress, NetworkInterface}

import cats.effect.Sync
import cats.implicits._
import fs2.Stream

private[config] object InetAddressConfig {

  def getValidAddressOrFail[F[_]: Sync]: F[InetAddress] = {
    getAddresses[F]
      .filter(_.getHostAddress.startsWith("10."))
      .compile
      .last
      .flatMap {
        case Some(e) => Sync[F].delay(e)
        case None => Sync[F].raiseError(new Throwable("No Valid IP Ranges Found"))
      }
  }

  def getAddresses[F[_]](implicit F: Sync[F]): Stream[F, InetAddress] = {
    streamEnum(NetworkInterface.getNetworkInterfaces)
      .flatMap(streamAddresses[F])
  }

  import java.util.{Enumeration => E}
  def streamEnum[F[_]: Sync, A](e: E[A]): Stream[F, A] = {
    Stream.unfoldEval(e) { e =>
      def hasNext(e: E[A]): F[Boolean] = Sync[F].delay(e.hasMoreElements)
      def next(e: E[A]): F[A] = Sync[F].delay(e.nextElement())
      hasNext(e).ifM(
        next(e).map(a => (a, e).some),
        Option.empty[(A, E[A])].pure[F]
      )
    }
  }

  def streamAddresses[F[_]: Sync](n: NetworkInterface): Stream[F, InetAddress] =
    Stream
      .eval(Sync[F].delay(n.getInetAddresses))
      .flatMap(streamEnum(_))

  def getHostname[F[_]: Sync]: F[String] = Sync[F].delay(
    sys.env
      .get("HOST")
      .orElse(sys.env.get("HOSTNAME"))
      .getOrElse(InetAddress.getLocalHost.getHostName)
  )

}
