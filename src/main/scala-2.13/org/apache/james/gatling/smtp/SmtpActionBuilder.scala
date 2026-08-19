package org.apache.james.gatling.smtp

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import org.apache.james.gatling.smtp.SmtpProtocol.SmtpComponents

case class SmtpActionBuilder(requestName: String,
                             _subject: String,
                             _body: String) extends ActionBuilder with NameGen {

  def subject(subject: String) = copy(_subject = subject)

  def body(body: String) = copy(_body = body)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val components: SmtpComponents = ctx.protocolComponentsRegistry.components(SmtpProtocol.SmtpProtocolKey)

    new SmtpAction(ctx.coreComponents.clock, ctx.coreComponents.statsEngine, next,
      genName(requestName), _subject, _body, components.protocol)
  }
}
