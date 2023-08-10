package com.github.mnogu.gatling.mqtt.protocol

import io.gatling.core.config.GatlingConfiguration

import scala.language.implicitConversions

/**
  *
  */
object MqttProtocolBuilder {

    implicit def toMqttProtocol(builder: MqttProtocolBuilder): MqttProtocol = builder.build

    def apply(configuration: GatlingConfiguration) : MqttProtocolBuilder =
        MqttProtocolBuilder(MqttProtocol(configuration))
}

case class MqttProtocolBuilder(mqttProtocol : MqttProtocol) {

    def build: MqttProtocol = mqttProtocol

}
