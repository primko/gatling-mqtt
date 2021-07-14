# Gatling-MQTT

This is an extension version of an unofficial [Gatling](http://gatling.io/) stress test plugin
for [MQTT](http://mqtt.org/).

Plugin improved to be able to provide next scenario:

connect -> publish -> publish -> publish -> ... -> publish -> publish -> disconnect

The origin plugin has the only possible scenario option:

connect -> publish -> disconnect -> connect -> publish -> disconnect -> ... -> connect -> publish -> disconnect

Additionally this extended version doesn't take into account connect and disconnect actions while performance report generation.


## Usage

### Scala & SBT

1. Install OpenJDK 8 or 11
2. Instal SBT
   1. https://www.scala-sbt.org/1.x/docs/Setup.html

### Cloning this repository

    $ git clone https://github.com/thingsboard/gatling-mqtt.git
    $ cd gatling-mqtt

### Creating a jar file

    $ sbt assembly

If you want to change the version of Gatling used to create a jar file,
change the following line in [`build.sbt`](build.sbt):

```scala
"io.gatling" % "gatling-core" % "3.6.1" % "provided",
```

and run `sbt assembly`.

### Putting the jar file to lib directory

Put the jar file to `./lib` directory in your SBT project:

    $ cp target/scala-2.13/gatling-mqtt-assembly-*.jar /path/to/sbt/project/lib/

This plugin supports the following options:

* host
* clientId
* cleanSession
* keepAlive
* userName
* password
* willTopic
* willMessage
* willQos
* willRetain
* version
* connectAttemptsMax
* reconnectAttemptsMax
* reconnectDelay
* reconnectDelayMax
* reconnectBackOffMultiplier
* receiveBufferSize
* sendBufferSize
* trafficClass
* maxReadRate
* maxWriteRate

See the document of [mqtt-client](https://github.com/fusesource/mqtt-client)
for the description of these options.
For example, the `host` option corresponds `setHost()` method in mqtt-client.
That is, you can obtain an option name in this plugin
by removing `set` from a method name in mqtt-client
and then making the first character lowercase.

The following options also support [Expression](https://gatling.io/docs/gatling/reference/3.6/session/expression_el/):

* host
* clientId
* userName
* password
* willTopic
* willMessage
* version

Here is a sample simulation file:

```scala
import com.github.mnogu.gatling.mqtt.Predef._
import io.gatling.core.Predef._
import org.fusesource.mqtt.client.QoS

import scala.concurrent.duration._
import scala.language.postfixOps

class MqttSimulation extends Simulation {
  val mqttConf = mqtt.host("tcp://localhost:1883")

  val connect = exec(mqtt("connect")
    .connect())

  val publish = repeat(100) {
    exec(mqtt("publish")
      .publish("foo", "Hello", QoS.AT_LEAST_ONCE, retain = false))
      .pause(1000 milliseconds)
  }

  val disconnect = exec(mqtt("disconnect")
    .disconnect())

  val scn = scenario("MQTT Test")
    .exec(connect, publish, disconnect)

  setUp(
    scn.inject(rampUsers(10).during(1.seconds))
  ).protocols(mqttConf)
}

```

The following parameters of `publish()` support Expression:

* topic
* payload

Here is a bit complex sample simulation file:

```scala
import com.github.mnogu.gatling.mqtt.Predef._
import io.gatling.core.Predef._
import org.fusesource.mqtt.client.QoS

import scala.concurrent.duration._
import scala.language.postfixOps

class MqttSimulation extends Simulation {

  val mqttConf = mqtt
      // MQTT broker
    .host("tcp://localhost:1883")
      // clientId: the values of "client" column in mqtt.csv
      //
      // See below for mqtt.csv.
     .clientId("${client}")

  val connect = exec(mqtt("connect")
    .connect())

   // send 100 publish MQTT messages
  val publish = repeat(100) {
    exec(mqtt("publish")
       // topic: "foo"
       // payload: "Hello"
       // QoS: AT_LEAST_ONCE
       // retain: false
      .publish("foo", "Hello", QoS.AT_LEAST_ONCE, retain = false))
       // 1 seconds pause between sending messages
      .pause(1000 milliseconds)
    }

  val disconnect = exec(mqtt("disconnect")
    .disconnect())

  val scn = scenario("MQTT Test")
     // The content of mqtt.csv would be like this:
     //
     //   client,topic,payload
     //   clientId1,topic1,payload1
     //   clientId2,topic2,payload2
     //   ...
    .feed(csv("mqtt.csv").circular)
    .exec(connect, publish, disconnect)

  setUp(
    scn
       // linearly connect 10 devices over 1 seconds and send 100 publish messages
      .inject(rampUsers(10).during(1.seconds))
  ).protocols(mqttConf)
}
```

## License

Apache License, Version 2.0
