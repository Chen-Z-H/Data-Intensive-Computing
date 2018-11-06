package processor

import java.io.{ByteArrayInputStream, PrintStream}
import java.net.Socket
import java.util.Base64

import bean.DataPackage
import com.google.gson.Gson
import javafx.scene.image.Image
import kafka.serializer.StringDecoder
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.opencv.core.{Core, Size}

//import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{Seconds, StateSpec, StreamingContext}
import org.apache.spark.streaming._
//import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe


object Processor {

  final val PORT = 12345
  final val IP_ADDRESS = "127.0.0.1"

  def main(args: Array[String]): Unit = {

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


     messages.map(x => x.value()).foreachRDD(
      rdd => {
        //        println("received!")
        val detector = new VideoHumanDetection()
        var socket: Socket = null
          try {
          for (dp <- rdd.collect()) {
            socket = new Socket(IP_ADDRESS, PORT)
            val out = new PrintStream(socket.getOutputStream)

            val gson = new Gson()
            val frame: DataPackage = gson.fromJson(dp.toString, classOf[DataPackage])
            //              updateValue(new Image(new ByteArrayInputStream(frame.getFrameBytes)))
            println(frame.getFrame.length)
            //            val out = new DataOutputStream(socket.getOutputStream())
            val processed = detector.detectHuman(frame.decodeToMat(), frame.getTime.toString)
            out.println(DataPackage.encodeToString(processed))
            out.flush()
            out.close()
            socket.close()
          }
        } catch {
          case e: Exception => println("Exception at ProcessorTask: " + e.getMessage)
        } finally {
            if (socket != null) {
              socket.close()
            }
        }
      }
    )

    ssc.start()
    ssc.awaitTermination()
    /*---------------------------------------------*/
  }
}
