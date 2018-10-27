package sparkstreaming

import java.io.{File, PrintWriter}
import java.util.HashMap

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.util.Random
import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector._
import com.datastax.driver.core.{Cluster, Host, Metadata, Session}
import com.datastax.spark.connector.streaming._


object KafkaSpark {
  def main(args: Array[String]) {
    // connect to Cassandra and make a keyspace and table as explained in the document
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect()
    session.execute("CREATE KEYSPACE IF NOT EXISTS avg_space WITH REPLICATION = " +
                            "{ 'class' : 'SimpleStrategy', 'replication_factor' : 1 };")
    session.execute("CREATE TABLE IF NOT EXISTS avg_space.avg (word text PRIMARY KEY, count float)")

    // make a connection to Kafka and read (key, value) pairs from it
    val kafkaConf = Map[String, String](
      "metadata.broker.list" -> "localhost:9092",
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "kafka-spark-streaming",
      "auto.commit.enable" -> "false",
      "auto.offset.reset" -> "largest",
//      "auto.commit.interval.ms" -> "1000",
      "zookeeper.coonection.timeout.ms" -> "1000"
    )

    val sparkConf = new SparkConf().setAppName("Lab3").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("./checkpoint")
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, Set("avg"))
//    <FILL IN>
    val pairs = messages.map(x => (x._2.split(",")(0), x._2.split(",")(1).toDouble))
    pairs.saveAsTextFiles("log", "txt")

    val writer = new PrintWriter(new File("loggg.txt"))
//    pairs.saveToCassandra("avg_space", "avg", SomeColumns("word", "count"))
    def mappingFunc(key: String, value: Option[Double], state: State[(Int, Double)]): (String, Double) = {
      var newState = (0, 0.0)
      if (state.exists()) {
        val existingState = state.get()
        newState = (existingState._1 + 1, (existingState._1 *  existingState._2 + value.get) / (existingState._1 + 1))
        state.update(newState)
      } else {
        state.update(newState)
      }
      (key, newState._2)
    }

//    def mappingFunc(key: String, value: Option[String], state: State[Int]): (String, String) = {
//      var newState = 0
//      if (state.exists()) {
//        val existingState = state.get()
//        newState = existingState + 1
//        state.update(newState)
//      } else {
//        state.update(newState)
//      }
//      (key, value.getOrElse(" "))
//    }


    val spec = StateSpec.function[String, Double, (Int, Double), (String, Double)](mappingFunc _)
    val stateDstream = pairs.mapWithState(spec)
    stateDstream.foreachRDD(rdd => rdd.foreach(pair => writer.println(s"MapWithState: key=${pair._1} value=${pair._2}")))
    writer.flush()
//    stateDstream.saveAsTextFiles("temp", "txt")
    //    val stateDstream = messages.mapWithState[Double, Double](StateSpec.function(mappingFunc))
    // store the result in Cassandra
    stateDstream.saveToCassandra("avg_space", "avg", SomeColumns("word", "count"))

    ssc.start()
    ssc.awaitTermination()
  }
}


