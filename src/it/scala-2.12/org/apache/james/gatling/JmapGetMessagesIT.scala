package org.apache.james.gatling

import org.apache.james.gatling.control.UserFeeder
import org.apache.james.gatling.jmap.scenari.JmapGetMessagesScenario

import scala.concurrent.duration._

class JmapGetMessagesIT extends JmapIT {
  private val MAILS_NUMBER = 10

  before {
    users.foreach(server.sendMessage(Fixture.homer.username))
  }

  scenario((feederBuilder, recipientFeederBuilder) => {
    new JmapGetMessagesScenario().generate(10 seconds, UserFeeder.toFeeder(users), recipientFeederBuilder, MAILS_NUMBER)
  })
}
