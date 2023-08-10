package com.github.mnogu.gatling.mqtt.test

import com.github.mnogu.gatling.mqtt.Predef._
import com.github.mnogu.gatling.mqtt.protocol.MqttProtocol
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import org.fusesource.mqtt.client.QoS

import scala.concurrent.duration._
import scala.language.postfixOps

class MqttSimulation extends Simulation {
  val mqttConf: MqttProtocol = mqtt.host("tcp://localhost:1883")

  val connect: ChainBuilder = exec(mqtt("connect")
    .connect())

  val publish: ChainBuilder = repeat(100) {
    exec(mqtt("publish")
      .publish("foo", "Hello", QoS.AT_LEAST_ONCE, retain = false))
      .pause(1000 milliseconds)
  }

  val disconnect: ChainBuilder = exec(mqtt("disconnect")
    .disconnect())

  val scn: ScenarioBuilder = scenario("MQTT Test")
    .exec(connect, publish, disconnect)

  setUp(
    scn.inject(rampUsers(10).during(1.seconds))
  ).protocols(mqttConf)
}
