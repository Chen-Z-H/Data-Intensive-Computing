package processor

import java.io.{ByteArrayInputStream, File}
import java.util.Base64

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions
import org.apache.spark.sql.streaming.GroupState
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import javafx.scene.image.Image
import javafx.concurrent._
import javafx.stage.Window
import org.opencv.core.{Core, Mat, MatOfByte}
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio.CV_CAP_PROP_FPS
import org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_WIDTH
import org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_HEIGHT
import org.opencv.imgcodecs.Imgcodecs

import scala.util.control.Breaks._

class ProcessorTask() extends Task[Image]{

  var directory = ""
  var videoPath = ""
  val topic = "detection"

  def this(directory: String = "", videoPath: String = "") {
    this()
    this.directory = directory
    this.videoPath = videoPath
  }

  override def call(): Image = {
    val kafkaConf = Map[String, String](
      "metadata.broker.list" -> "localhost:9092",
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "kafka-spark-streaming",
      "zookeeper.connection.timeout.ms" -> "1000"
    )
    val sparkConf = new SparkConf().setAppName("processor").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    ssc.checkpoint("./checkpoint")
    println("Waiting...")

    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, Set("detection"))
    println("received!")

//    val schema = DataTypes.createStructType(Array(
//      DataTypes.createStructField("time", DataTypes.TimestampType, true),
//      DataTypes.createStructField("rate", DataTypes.IntegerType, true),
//      DataTypes.createStructField("width", DataTypes.IntegerType, true),
//      DataTypes.createStructField("height", DataTypes.IntegerType, true),
//      DataTypes.createStructField("data", DataTypes.StringType, true)))

    val gson = new Gson()

    messages.map(x => Base64.getDecoder.decode(x._2)).
      map(x => gson.fromJson(x.toString, classOf[DataPackage])).foreachRDD(
      rdd =>
//        println("received!")
        for (dp <- rdd.collect()) {
          updateValue(new Image(new ByteArrayInputStream(dp.getFrameBytes)))
        }
    )

    ssc.start()
    ssc.awaitTermination()

    null
  }

//  override def call(): Image = {
//    val directoryFile = new File(directory)
//    val imageFiles = directoryFile.listFiles()
//    val images = for (image <- imageFiles) yield new Image(image.toURI.toString)
//    for (image <- images) {
//      if (isCancelled()) break
//      updateValue(image)
//      Thread.sleep(100)
//    }
//    null
//  }

//  override def call(): Image = {
//    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
//    val camera = new VideoCapture(videoPath)
//    val rate = camera.get(CV_CAP_PROP_FPS)
//    val width = camera.get(CV_CAP_PROP_FRAME_WIDTH)
//    val height = camera.get(CV_CAP_PROP_FRAME_HEIGHT)
//
//    updateMessage(height + "+" + width)
//
//    if (camera.isOpened) {
//      println("File open succeed!")
//      val frame = new Mat()
//      while (camera.read(frame)) {
//        val buffer = new MatOfByte()
//        Imgcodecs.imencode(".png", frame, buffer)
//        updateValue(new Image(new ByteArrayInputStream(buffer.toArray)))
//        Thread.sleep((800 / rate).toInt)
//      }
//    } else {
//      println("File open failed!")
//    }
//    null
//  }
}
