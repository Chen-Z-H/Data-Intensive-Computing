package generator

import java.util.Properties

import org.apache.kafka.clients.producer._

import scala.util.Random

object ScalaProducerExample extends App {
  def getRandomVal: String = {
    val i = Random.nextInt(alphabet.size)
    val key = alphabet(i)
    val value = Random.nextInt(alphabet.size)
//    fakeValue = fakeValue + 1
    key + "," + value
  }

  var count = 0
  var fakeValue = 0
  val alphabet = 'a' to 'z'
  val events = 10000
  val topic = "avg"
  val brokers = "localhost:9092"  //kafka也是一个集群，其中每个broker都可以分布在不同的机器上，因此有着自己的URL
  val rnd = new Random()

  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
  props.put(ProducerConfig.CLIENT_ID_CONFIG, "ScalaProducerExample")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
//  props.put(ProducerConfig.ACKS_CONFIG, "all")
//  props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
//  props.put(ProducerConfig.RETRIES_CONFIG, "5")
//  props.put(ProducerConfig., "5")
  val producer = new KafkaProducer[String, String](props)

  while (true) {
    val rv = getRandomVal
    val data = new ProducerRecord[String, String](topic, null, rv)
    producer.send(data, new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
//        print("Error: " + rv + " failed!!!\n")
        if (exception != null) {
          print(exception)
        } else {
          print(rv + " acknowledged! Offset: " + metadata.offset() + " \n")
        }

      }
    })
//    print(rv + "\n")
//    count = count + 1
//    Thread.sleep(50)
  }

  producer.close()
}

