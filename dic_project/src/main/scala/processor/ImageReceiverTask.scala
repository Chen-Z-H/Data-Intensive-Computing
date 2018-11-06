package processor

import javafx.concurrent.Task
import javafx.scene.image.Image
import java.io._
import java.net.ServerSocket
import java.net.Socket

import bean.DataPackage
import org.opencv.core.{Core, MatOfByte, Size}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoWriter

class ImageReceiverTask extends Task[Image]{

  final val port = 12345

  override def call(): Image = {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    try {
//      println("Thread started...")
      val server = new ServerSocket(port, -1)
      server.setReceiveBufferSize(999999999)
//      println(server.getReceiveBufferSize)
      println("Listening...")
      while (true) {
        val socket = server.accept()
        val inputStreamReader = new InputStreamReader(socket.getInputStream)
        val bufferedReader = new BufferedReader(inputStreamReader)
        val frame = bufferedReader.readLine()
//        println(frame.length)
//        DataPackage.decodeToMat(frame)
        val buffer = new MatOfByte()
        Imgcodecs.imencode(".png", DataPackage.decodeToMat(frame), buffer)
        updateValue(new Image(new ByteArrayInputStream(buffer.toArray)))
        println("Image updated!")
//        writer.write(DataPackage.decodeToMat(frame))

      }
//      writer.release()
    } catch {
      case e: Exception => println("Server exception: " + e.getMessage)
    }
    null
  }
}
