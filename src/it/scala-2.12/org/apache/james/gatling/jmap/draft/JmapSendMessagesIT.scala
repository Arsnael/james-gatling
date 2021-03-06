package org.apache.james.gatling.jmap.draft

import org.apache.james.gatling.Fixture
import org.apache.james.gatling.jmap.draft.scenari.JmapSendMessagesScenario

import scala.concurrent.duration._


class JmapSendMessagesIT extends JmapIT {

  before {
    users.foreach(server.sendMessage(Fixture.homer.username))
  }

  scenario((userFeederBuilder, recipientFeederBuilder) => {
    new JmapSendMessagesScenario().generate(10 seconds, userFeederBuilder, recipientFeederBuilder)
  })
}
