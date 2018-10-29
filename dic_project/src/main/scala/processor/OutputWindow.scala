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

  val directory = "E:\\photo\\stockholm"
  val imageName = "E:\\photo\\me.jpg"
  val videoPath = "E:\\entertainment\\series\\got\\冰与火之歌：权力的游戏.第五季第09集.1024x576.中英双字幕.rmvb"

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
    val scene = new Scene(layout, 1024, 768)
    primaryStage.setScene(scene)

    val updateTask = new ProcessorTask(directory, videoPath)
    updateTask.messageProperty().addListener(new ChangeListener[String] {
      override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        println(newValue)
        val size = newValue.split("\\+")
        val newHeight = size(0).toDouble
        primaryStage.setHeight(newHeight)
        imageView.setFitHeight(newHeight)
        println("Height has been resized to " + newHeight + " !")
        val newWidth = size(1).toDouble
        primaryStage.setWidth(newWidth)
        imageView.setFitWidth(newWidth)
        println("Width has been resized to " + newWidth + " !")
      }
    })
    imageView.imageProperty().bind(updateTask.valueProperty())
    new Thread(updateTask).start()

    primaryStage.show()
  }

}
