package com.banno.jabberwocky.consumer.health.config

import cats.effect.IO
import org.specs2.mutable.Specification

class ConfigSpec extends Specification {

  "Config must load" >> {
    SetupConfig.loadConfig[IO].attempt.unsafeRunSync() must beRight[Configurations.ServiceConfig]
  }
}
