package com.github.mnogu.gatling.mqtt.action

import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.util.NameGen
import org.fusesource.mqtt.client.CallbackConnection

class MqttRequestDisconnectAction(val next: Action)
  extends Action with NameGen {

  override val name: String = genName("mqttDisconnect")

  override def execute(session: Session): Unit = {
    val connection = session.attributes("connection").asInstanceOf[CallbackConnection]
    connection.disconnect(null)
    next ! session
  }
}
