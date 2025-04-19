package weavemaze

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Stage

import scalafx.animation.{KeyFrame, Timeline}
import scalafx.util.Duration
import scalafx.Includes.jfxScene2sfx
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.Pane


import scalafx.embed.swing.SwingFXUtils
import javax.imageio.ImageIO
import java.io.File
import javafx.scene.image.WritableImage

import scalafx.Includes._
import scalafx.scene.control.Button
import scalafx.scene.layout.{VBox, HBox}
import scalafx.geometry.Pos
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.beans.binding.Bindings
import scalafx.application.Platform


import scala.util.Random

object WeaveMaze extends JFXApp3 {
  override def start(): Unit = {
    val game = Game("Name", difficulty = 0.5,infinite = true, userRows = 15, userCols = 30)
    stage = new JFXApp3.PrimaryStage {
      title = "Weave Maze"
      scene = game.start() 
    }
 }
}
