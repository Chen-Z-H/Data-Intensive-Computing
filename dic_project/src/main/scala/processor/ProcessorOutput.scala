package processor


import java.io.File

import javafx.application.Application
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Scene
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.StackPane
import javafx.stage.{Stage, WindowEvent}
//import javafx.concurrent._
import javafx.event.EventHandler

import scala.util.control.Breaks._


object ProcessorOutput {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[OutputWindow])
  }
}

class OutputWindow extends Application {

//  val videoPath = ""

  @Override
  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("A simple video monitor")
    primaryStage.setOnCloseRequest(new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = {
        System.exit(0)
      }
    })

    val imageView = new ImageView()

    val layout = new StackPane()
    layout.getChildren.add(imageView)
    val scene = new Scene(layout, 1280, 720)
    primaryStage.setScene(scene)

//    val receiverTask = new ProcessorTask(directory, videoPath)
    val updateTask = new ImageReceiverTask()

//    updateTask.messageProperty().addListener(new ChangeListener[String] {
//      override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
//        println(newValue)
//        val size = newValue.split("\\+")
//        val newHeight = size(0).toDouble
//        primaryStage.setHeight(newHeight)
//        imageView.setFitHeight(newHeight)
//        println("Height has been resized to " + newHeight + " !")
//        val newWidth = size(1).toDouble
//        primaryStage.setWidth(newWidth)
//        imageView.setFitWidth(newWidth)
//        println("Width has been resized to " + newWidth + " !")
//      }
//    })

    imageView.imageProperty().bind(updateTask.valueProperty())
    println("Server started...")
    new Thread(updateTask).start()
//    new Thread(receiverTask).start()

    primaryStage.show()
  }

}