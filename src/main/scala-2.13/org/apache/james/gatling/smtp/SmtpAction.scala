package org.apache.james.gatling.smtp

import javax.mail.internet.InternetAddress

import courier.{Envelope, Mailer, Text}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import org.apache.james.gatling.control.UserFeeder

import scala.concurrent.ExecutionContext
import scala.util.{Failure => TFailure, Success => TSuccess}

class SmtpAction(val clock: Clock,
                 val statsEngine: StatsEngine,
                 val next: Action,
                 requestName: String,
                 subject: String,
                 body: String,
                 protocol: SmtpProtocol) extends RequestAction {

  override val name: String = "sendMail"

  override def requestName: Expression[String] = _ => io.gatling.commons.validation.Success(requestName)

  override def sendRequest(session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    for {
      username <- session(UserFeeder.usernameSessionParam).validate[String]
      password <- session(UserFeeder.passwordSessionParam).validate[String]
    } yield {
      val baseMailer = Mailer(protocol.host, protocol.port).startTls(protocol.ssl).trustAll(true)
      val mailer = credentials(protocol, username, password)
        .map(creds => baseMailer.auth(true).as(creds._1, creds._2))
        .getOrElse(baseMailer.auth(false))()

      val envelope = Envelope.from(new InternetAddress(username))
        .to(new InternetAddress(username))
        .subject(subject)
        .content(Text(body))

      mailer(envelope)(ExecutionContext.global).onComplete {
        case TSuccess(_) => ok(session, start)
        case TFailure(e) =>
          logger.error("Exception caught while sending mail", e)
          ko(session, start, e.getMessage)
      }(ExecutionContext.global)
    }
  }

  // ponytail: preserve the original auth semantics where auth=false sends the user's
  // credentials (the default "NoAuthentication" scenario still authenticates).
  private def credentials(protocol: SmtpProtocol, username: String, password: String): Option[(String, String)] =
    if (protocol.auth) None else Some((username, password))

  private def ok(session: Session, start: Long): Unit = {
    statsEngine.logResponse(session.scenario, session.groups, requestName, start, clock.nowMillis, OK, None, None)
    next ! session
  }

  private def ko(session: Session, start: Long, message: String): Unit = {
    statsEngine.logResponse(session.scenario, session.groups, requestName, start, clock.nowMillis, KO, None, Some(message))
    next ! session.markAsFailed
  }
}
