package camera

import java.io.ByteArrayInputStream

import org.apache.kafka.clients.producer._
import org.opencv.core.{Core, Mat, MatOfByte}
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio.CV_CAP_PROP_FPS
import org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_WIDTH
import org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_HEIGHT
import org.opencv.imgcodecs.Imgcodecs
import java.util.{Base64, Properties}

import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.scene.image.Image

object VideoStreamGenerator {

  val broker = "localhost:9092"
  val topic = "detection"
  val videoPath = "E:\\entertainment\\series\\got\\冰与火之歌：权力的游戏.第五季第09集.1024x576.中英双字幕.rmvb"

  def _getProperties(): Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "HumanDetection")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    props
  }

  def main(args: Array[String]): Unit = {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    val camera = new VideoCapture(videoPath)
    val rate = camera.get(CV_CAP_PROP_FPS)
    val width = camera.get(CV_CAP_PROP_FRAME_WIDTH)
    val height = camera.get(CV_CAP_PROP_FRAME_HEIGHT)

    val gson = new Gson()
    val producer = new KafkaProducer[String, String](_getProperties())

    if (camera.isOpened) {
      println("Video has been loaded!")
      val frame = new Mat()
      while (camera.read(frame)) {
        val buffer = new MatOfByte()
        Imgcodecs.imencode(".png", frame, buffer)

        val frameObject = new JsonObject()
        frameObject.addProperty("time", System.currentTimeMillis().toString)
        frameObject.addProperty("rate", rate.toString)
        frameObject.addProperty("width", width.toString)
        frameObject.addProperty("height", height.toString)
        frameObject.addProperty("frame", Base64.getEncoder.encodeToString(buffer.toArray))

        val data_in_json = gson.toJson(frameObject)

        val dataFrame = new ProducerRecord[String, String](topic, null, data_in_json)
        producer.send(dataFrame, new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
            println("Sended at " + System.currentTimeMillis().toString)
          }
        })
      }
      camera.release()
    } else {
      println("Video loading failed!")
    }

  }
}
