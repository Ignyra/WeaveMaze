package weavemaze

import javafx.beans.property.SimpleObjectProperty
import scalafx.scene.paint.Color
import javafx.scene.paint.Paint
import scalafx.Includes._
import scala.collection.mutable.ArrayBuffer
//Colors: https://javadoc.io/doc/org.scalafx/scalafx_2.13/15.0.1-R21/scalafx/scene/paint/Color$.html

val FontType = "/fonts/OpenDyslexicMNerdFontMono-Regular.otf"
val GlowLevel = 0.3
val SMOOTHTRANSITIONS = true


val MaxRows:Int = 100
val MaxCols: Int = 200
val MaxBridges: Double = 0.9
val MinRows: Int = 5
val MinCols: Int = 5
val MinBridgeDensity: Double = 0.1


object UIColors {
  val MazeTileColor = SimpleObjectProperty[Paint](Color.web("#111821",1.0))
  val MazeEdgeColor = SimpleObjectProperty[Paint](Color.web("Cyan", 1.0))
  val BackgroundColor = SimpleObjectProperty[Paint](Color.web("#0A0F1C",1.0))
  val PlayerColor = SimpleObjectProperty[Paint](Color.web("#39FF14",0.1))
  val PlayerEdgeColor = SimpleObjectProperty[Paint](Color.web("#66FF66",1.0))
  val InfoBarColor = SimpleObjectProperty[Paint](Color.web("#0E121F",0.85))
  val InfoBarEdgeColor = SimpleObjectProperty[Paint](Color.web("Crimson",1.0))
  val InfoTextColor = SimpleObjectProperty[Paint](Color.web("#E0E0E0",1.0))
  val TargetColor = SimpleObjectProperty[Paint](Color.web("#FF007F",0.1))
  val TargetEdgeColor = SimpleObjectProperty[Paint](Color.web("#FF66CC",1.0))
  val presets = ArrayBuffer[()=>Unit]()
  var current = 0
  def switchTheme():Unit = {
    current += 1 
    if current == presets.length then current = 0 
    presets(current)()
  }
  
  presets += default
  def default():Unit = {
    MazeTileColor.value = Color.web("#111821",1.0)
    MazeEdgeColor.value = Color.web("Cyan", 1.0)
    BackgroundColor.value = Color.web("#0A0F1C",1.0)
    PlayerColor.value = Color.web("#39FF14",0.1)
    PlayerEdgeColor.value = Color.web("#66FF66",1.0)
    InfoBarColor.value = Color.web("#0E121F",0.85)
    InfoBarEdgeColor.value = Color.web("Crimson",1.0)
    InfoTextColor.value = Color.web("#E0E0E0",1.0)
    TargetColor.value = Color.web("#FF007F",0.1)
    TargetEdgeColor.value = Color.web("#FF66CC",1.0)
  }

  presets += preset1
  def preset1():Unit = {
    MazeTileColor.value = Color.web("Black",1.0)
    MazeEdgeColor.value = Color.web("Cyan",1.0)
    BackgroundColor.value = Color.web("FloralWhite",1.0)
    PlayerColor.value = Color.web("Gold",1.0)
    PlayerEdgeColor.value= Color.web("Gold",1.0)
    InfoBarColor.value = Color.web("BlanchedAlmond",1.0)
    InfoBarEdgeColor.value = Color.web("Crimson",1.0)
    InfoTextColor.value = Color.web("Black",1.0)
    TargetColor.value = Color.web("Red",1.0)
    TargetEdgeColor.value = Color.web("Crimson",1.0)
  }
  
  //presets += preset2
  def preset2():Unit = {
    ???
  }

}
