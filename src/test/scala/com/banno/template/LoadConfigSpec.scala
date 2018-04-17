package com.banno.template.config

import org.specs2.mutable.Specification
import cats.effect.IO

class ConfigSpec extends Specification {

  "Config must load" >> {
    SetupConfig.loadConfig[IO].attempt.unsafeRunSync() must beRight[Configurations.ServiceConfig]
  }
}
