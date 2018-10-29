package processor

import java.io.{ByteArrayInputStream, File}

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

  def this(directory: String = "", videoPath: String = "") {
    this()
    this.directory = directory
    this.videoPath = videoPath
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

  override def call(): Image = {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    val camera = new VideoCapture(videoPath)
    val rate = camera.get(CV_CAP_PROP_FPS)
    val width = camera.get(CV_CAP_PROP_FRAME_WIDTH)
    val height = camera.get(CV_CAP_PROP_FRAME_HEIGHT)

    updateMessage(height + "+" + width)

    if (camera.isOpened) {
      println("File open succeed!")
      val frame = new Mat()
      while (camera.read(frame)) {
        val buffer = new MatOfByte()
        Imgcodecs.imencode(".jpg", frame, buffer)
        updateValue(new Image(new ByteArrayInputStream(buffer.toArray)))
        Thread.sleep((800 / rate).toInt)
      }
    } else {
      println("File open failed!")
    }
    null
  }
}
