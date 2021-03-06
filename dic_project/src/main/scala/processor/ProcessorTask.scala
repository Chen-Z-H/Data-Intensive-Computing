package processor

import java.io._
import java.util.Base64

import bean.DataPackage
import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.opencv.core.{Core, Size}
import java.net.Socket

import scala.collection.mutable.ArrayBuffer
//import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import javafx.scene.image.Image
import javafx.concurrent._
import javafx.stage.Window

import scala.util.control.Breaks._

class ProcessorTask() extends Task[Image]{

  var directory = ""
  var videoPath = ""
  val topic = "detection"
  final val PORT = 12345
  final val IP_ADDRESS = "localhost"

  def this(directory: String = "", videoPath: String = "") {
    this()
    this.directory = directory
    this.videoPath = videoPath
  }

  override def call(): Image = {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    /*---------------------------------------------------------------------*/
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    val kafkaConf = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "zookeeper.connect" -> "localhost:2181",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "kafka-spark-streaming",
      "zookeeper.connection.timeout.ms" -> "1000",
      "message.max.bytes" -> "10485760",
      "fetch.message.max.bytes" -> "10485760",
      "kafka.max.partition.fetch.bytes" -> "10485760",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val sparkConf = new SparkConf().setAppName("processor").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    ssc.checkpoint("./checkpoint")
    println("Waiting...")

    val topics = Array("human")
    topics.map(_.toString).toSet

    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaConf))


    val packages = messages.map(x => x.value()).foreachRDD(
      rdd => {
        //        println("received!")
        val detector = new VideoHumanDetection()
        var socket = new Socket(IP_ADDRESS, PORT)
        val out = new PrintStream(socket.getOutputStream)
        try {
          for (dp <- rdd.collect()) {
            val gson = new Gson()
            val frame: DataPackage = gson.fromJson(dp.toString, classOf[DataPackage])
            //              updateValue(new Image(new ByteArrayInputStream(frame.getFrameBytes)))
            println(frame.getTime)
//            val out = new DataOutputStream(socket.getOutputStream())
//            Thread.sleep(100)
            out.println(frame.getFrame)
          }
//          out.close()
        } catch {
          case e: Exception => println("Exception at ProcessorTask: " + e.getMessage)
        } finally {
          if (socket != null) {
            try {
              socket = null
            } catch {
              case e: Exception => {
                socket = null
                println("Exception at ProcessorTask: " + e.getMessage)
              }
            }
          }
        }
      }
    )


    ssc.start()
    ssc.awaitTermination()

    null
  }
}
