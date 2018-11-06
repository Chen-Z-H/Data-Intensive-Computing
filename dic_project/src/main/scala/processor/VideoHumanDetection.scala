package processor

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.HOGDescriptor

import scala.util.control.Breaks._

class VideoHumanDetection() {

//  def this() = {
//    this()
//    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
//  }

  def detectHuman(frame: Mat, time: String) : Mat = {
    var hog = new HOGDescriptor()
    hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector)

    if(frame.empty()){
      println("image not found")
      System.exit(0)
    }
    var found = new MatOfRect()
    var weight = new MatOfDouble()
    var frame_resize = new Mat()
    Imgproc.resize(frame,frame_resize,new Size(1280,720))
    var frame_gray = new Mat()
    Imgproc.cvtColor(frame_resize,frame_gray,Imgproc.COLOR_RGB2GRAY)

    hog.detectMultiScale(frame_gray, found, weight, 0, new Size(4,4), new Size(8,8), 1.05, 2, false)
    var rect: Array[Rect] = found.toArray
    var wArr: Array[Double] = weight.toArray
    if(rect.length>0){
      for(i <- 0 to (rect.length - 1)){
        breakable {
          val r = rect(i)
          if (wArr(i) < 0.7)
            break
          //        r.x +=  Math.round(r.width * 0.1).toInt
          //        r.width += Math.round(r.width * 0.8).toInt
          //        r.y += Math.round(r.height * 0.07).toInt
          //        r.height += Math.round(r.height * 0.85).toInt
          Imgproc.rectangle(frame_resize, r.tl(), r.br(), new Scalar(0, 255, 0), 2)
        }
      }
    }
//    Imgcodecs.imwrite("E:\\code\\IntelliJIDEA\\HumanDetection\\image\\" + time + ".png", frame_resize)
    frame_resize
  }


}
