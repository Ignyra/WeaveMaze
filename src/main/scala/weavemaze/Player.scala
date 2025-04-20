package weavemaze

import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.layout.Pane

import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.geometry.Insets
import scalafx.scene.layout.BackgroundImage


import scalafx.animation.RotateTransition
import scalafx.util.Duration
import scalafx.animation.Interpolator
import scalafx.animation.TranslateTransition
import scalafx.scene.shape.StrokeType.Inside
import scalafx.scene.effect.Glow


import scala.collection.mutable.ArrayBuffer


class playerBox(size:Double, color:Color, edgeColor:Color) extends Pane {
  val box = new Rectangle {
    width = size
    height = size
    fill = color
    stroke = edgeColor
    strokeWidth = size/3.5 
    strokeType = Inside
  }

  val rotation = new RotateTransition(Duration(250), this) {
      byAngle = 90
      cycleCount = 1 //RotateTransition.Indefinite 
      autoReverse = false
      interpolator = Interpolator.EaseOut
    }

  val bounce = new TranslateTransition(Duration(150), this) {
    autoReverse = false
    cycleCount = 0//TranslateTransition.Indefinite
    interpolator = Interpolator.EaseOut
  }
  
  val bounce2 = new TranslateTransition(Duration(50), this) {
    autoReverse = false
    cycleCount = 0//TranslateTransition.Indefinite
    interpolator = Interpolator.EaseOut
  }

  val bounce3 = new TranslateTransition(Duration(50), this) {
    autoReverse = false
    cycleCount = 0//TranslateTransition.Indefinite
    interpolator = Interpolator.EaseOut
  }

  bounce.setOnFinished(_ => bounce2.play())
  bounce2.setOnFinished(_ => bounce3.play())


  children.add(box)
}


class Player(val name:String, var cell:Cell, tiles:ArrayBuffer[ArrayBuffer[cellTile]], cellGrid:ArrayBuffer[ArrayBuffer[Cell]]) {
  
  val cellSize = tiles(0)(0).tileWidth
  val boxSize = cellSize/2
  //val box = playerBox(boxSize, Color.Black)
  val box = playerBox(boxSize, Color.web(PlayerColor, PlayerColorOP), Color.web(PlayerEdgeColor, PlayerEdgeColorOP))
  
  var numMoves = 0
  
  def updatePos() = {
    val currTile = tiles(cell.x)(cell.y)
    val centerX = currTile.layoutX() + cellSize/2
    val centerY = currTile.layoutY() + cellSize/2

    val oldX = box.translateX.value
    val oldY = box.translateY.value
    val newX = centerX - boxSize /2
    val newY = centerY - boxSize /2
    
    //bouncing animation if exists, applies box.translate as well
    if SMOOTHTRANSITIONS then
      box.bounce.toX = newX 
      box.bounce.toY = newY 
      
      box.bounce2.toX = oldX + (newX - oldX)/1.1
      box.bounce2.toY = oldY + (newY - oldY)/1.1
      
      box.bounce3.toX = newX 
      box.bounce3.toY = newY

      box.bounce.play()
    else
      //translates directly to new 
      box.translateX = newX
      box.translateY = newY
      

      


    //if we are under a bridge, change obacity so it appears like it
    if (cell.exit & T) == T then
      box.opacity = 0.2
    else
      box.opacity = 1

    box.requestLayout() //refresh  

  }
  
  def getAndResetMoves:Int = {
    val m = numMoves
    numMoves = 0
    m
  }
    
  
  def move(dir:Int) = {
    
    var newCell = cell
    //checks if there is an exit to that direction
    if (cell.exit & dir) == dir then
      newCell = cellGrid(cell.x + DX(dir))(cell.y + DY(dir))
      
      //if the player is going into a tunnel (marked by bridge that is perpendicular to /blocked by its direction), 
      //then alert the program by changing the cell type into a tunnel
      if ((newCell.exit & B) == B) && ((newCell.exit & OPPOSITE(dir)) == 0) then
        newCell = newCell.tunnel
      
      //update number of Moves taken
      numMoves += 1
      
    cell = newCell

    scalafx.application.Platform.runLater {
      this.updatePos()
    }
    
    //rotation animation
    if SMOOTHTRANSITIONS then
      if ((dir & N) == N) || ((dir & E) == E) then
        box.rotation.byAngle = 90
      else
        box.rotation.byAngle = -90
      
      box.rotation.play()

  }

  this.updatePos()
} 



 
