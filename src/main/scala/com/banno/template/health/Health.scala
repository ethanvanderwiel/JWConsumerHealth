package com.banno.template.health

import cats.effect.Sync
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.Response
import cats.implicits._

object Health {

  def service[F[_]](implicit F: Sync[F]): HttpService[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    // Always True - This can eventually take services and enhance this Check
    val check = F.delay(true)

    def checkResponse: F[Response[F]] = {
      check
        .ifM(
          Ok("ok"),
          ServiceUnavailable()
        )
        .handleErrorWith(_ => ServiceUnavailable())
    }

    HttpService[F] {
      case _ -> Root / "health" =>
        checkResponse
      case _ -> Root / "ping" => // Temporary Placeholder - Remove After Change to Environment
        checkResponse
    }
  }

}
