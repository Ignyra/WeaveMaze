package weavemaze

import scalafx.application.JFXApp3
import scalafx.stage.Stage





object WeaveMaze extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Weave Maze"
      scene = Menu 
    }
 }
}
