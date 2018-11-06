package camera

import bean.DataPackage
import org.apache.kafka.clients.producer._
import org.opencv.core.{Core, Mat, MatOfByte, Size}
import org.opencv.videoio.{VideoCapture, VideoWriter}
import org.opencv.videoio.Videoio.CV_CAP_PROP_FPS
import org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_WIDTH
import org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_HEIGHT
import org.opencv.videoio.Videoio.CV_CAP_PROP_FOURCC
import org.opencv.imgcodecs.Imgcodecs
import java.util.{Base64, Properties}

import com.google.gson.Gson
import org.opencv.imgproc.Imgproc


object VideoStreamGenerator {

  val broker = "localhost:9092"
  val topic = "human"
  val videoPath = ""

  def _getProperties(): Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "HumanDetection")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.RETRIES_CONFIG, "1")
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "10485760")
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10485760")
    props.put(ProducerConfig.LINGER_MS_CONFIG, "5")
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip")
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "20971520")
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
    val fourcc = camera.get(CV_CAP_PROP_FOURCC).toByte
//    camera.set(CV_CAP_PROP_FRAME_WIDTH, 1280)
//    camera.set(CV_CAP_PROP_FRAME_HEIGHT, 720)

    val gson = new Gson()
    val producer = new KafkaProducer[String, String](_getProperties())

    if (camera.isOpened) {
      println("Video has been loaded!")
      val ori = new Mat()

      while (camera.read(ori)) {
        val frame = new Mat()
        Imgproc.resize(ori, frame, new Size(1280,720))
        val buffer = new MatOfByte()
        Imgcodecs.imencode(".png", frame, buffer)

        val curTime = System.currentTimeMillis()
        val frameObject = new DataPackage(
          curTime,
          rate,
          height,
          width,
          fourcc,
          Base64.getEncoder.encodeToString(buffer.toArray)
        )

        val data_in_json = gson.toJson(frameObject)
        val dataFrame = new ProducerRecord[String, String](topic, curTime.toString, data_in_json)
//            Thread.sleep(100)
        producer.send(dataFrame, new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
            println("Sended at " + curTime.toString)
          }
        })
      }
        camera.release()
      } else {
        println("Video loading failed!")
      }

    }
}
