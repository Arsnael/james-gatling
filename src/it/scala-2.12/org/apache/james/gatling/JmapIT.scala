package org.apache.james.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.funspec.GatlingFunSpec
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import org.apache.james.gatling.Fixture.{bart, simpsonDomain}
import org.apache.james.gatling.JamesServer.RunningServer
import org.apache.james.gatling.control.{RecipientFeeder, UserFeeder}
import org.slf4j
import org.slf4j.LoggerFactory

abstract class JmapIT extends GatlingFunSpec {
  protected val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  protected val server: RunningServer = JamesServer.start()
  lazy val protocolConf: Protocol = http.baseUrl(s"http://localhost:${server.mappedJmapPort}")

  protected def mappedJmapPort = server.mappedJmapPort

  protected lazy val users = List(bart)

  before {
    server.addDomain(simpsonDomain)
    users.foreach(server.addUser)
  }

  after {
    server.stop()
  }

  protected def scenario(scenarioFromFeeder: (FeederBuilder, FeederBuilder) => ScenarioBuilder) = {
    val feeder = UserFeeder.toFeeder(users)
    val recipientFeeder = RecipientFeeder.usersToFeeder(users).random
    scenarioFromFeeder(feeder, recipientFeeder).actionBuilders.reverse.foreach { actionBuilder =>
      spec(actionBuilder)
    }
  }
}
