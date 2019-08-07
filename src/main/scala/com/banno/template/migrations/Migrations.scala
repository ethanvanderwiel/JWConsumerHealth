package com.banno.jabberwocky.consumer.health.migrations

import cats.implicits._
import cats.effect.Sync
import org.flywaydb.core.Flyway

object Migrations {
  def makeMigrations[F[_]: Sync](url: String, user: String, password: String): F[Unit] =
    Sync[F].delay {
      val flyway = new Flyway
      flyway.setDataSource(url, user, password)
      flyway.migrate()
    }.void
}
