package com.github.mnogu.gatling.mqtt.action

import com.github.mnogu.gatling.mqtt.protocol.MqttProtocol
import com.github.mnogu.gatling.mqtt.request.builder.MqttAttributes
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import org.fusesource.mqtt.client.{Callback, CallbackConnection, QoS}

class MqttRequestPublishAction(
  val mqttAttributes: MqttAttributes,
  val coreComponents : CoreComponents,
  val mqttProtocol: MqttProtocol,
  val next: Action,
  val clock: Clock)
   extends ExitableAction with NameGen {

  val statsEngine: StatsEngine = coreComponents.statsEngine

  override val name: String = genName("mqttPublish")

  override def execute(session: Session): Unit = recover(session) {
    val connection = session.attributes("connection").asInstanceOf[CallbackConnection]

    mqttAttributes.requestName(session).flatMap { resolvedRequestName =>
      mqttAttributes.topic(session).flatMap { resolvedTopic =>
        sendRequest(
          resolvedRequestName,
          connection,
          resolvedTopic,
          mqttAttributes.payload,
          mqttAttributes.qos,
          mqttAttributes.retain,
          session)
      }
    }
  }

  private def sendRequest(
      requestName: String,
      connection: CallbackConnection,
      topic: String,
      payload: Expression[String],
      qos: QoS,
      retain: Boolean,
      session: Session): Validation[Unit] = {

    payload(session).map { resolvedPayload =>
      val requestStartDate = clock.nowMillis

      connection.publish(
        topic, resolvedPayload.getBytes, qos, retain, new Callback[Void] {
          override def onFailure(value: Throwable): Unit =
            writeData(isSuccess = false, Some(value.getMessage))

          override def onSuccess(void: Void): Unit =
            writeData(isSuccess = true, None)

          private def writeData(isSuccess: Boolean, message: Option[String]) = {
            val requestEndDate = clock.nowMillis

            statsEngine.logResponse(
              session.scenario,
              session.groups,
              requestName,
              requestStartDate,
              requestEndDate,
              if (isSuccess) OK else KO,
              None,
              None
            )

            next ! session
          }
        })
    }
  }
}
