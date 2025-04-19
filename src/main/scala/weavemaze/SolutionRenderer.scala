package weavemaze

import scalafx.scene.paint.Color
import scalafx.scene.shape.{Rectangle, Line, Circle,StrokeLineCap}
import scalafx.scene.layout.Pane

import scala.collection.mutable.ArrayBuffer

//end: true if either end, false is inbetween
//dir: going to N or S or E or W or going to and comeing from NE,NW, SE, SW
//
class routeGuide(dir:Int , tile:Pane, cellSize: Double, lineWidth:Double, circleRadius:Double, lineColor:Color, circleColor:Color, end:Boolean = false) extends Pane {


  def horizontalLine = new Line {
          startX = tile.layoutX()
          startY = tile.layoutY() + cellSize / 2
          endX = tile.layoutX() + cellSize
          endY = tile.layoutY() + cellSize / 2
          stroke = lineColor
          strokeWidth = lineWidth
          strokeLineCap.value = StrokeLineCap.Butt
        }
  
  def verticalLine = new Line {
          startX = tile.layoutX() + cellSize / 2
          startY = tile.layoutY()
          endX = tile.layoutX() + cellSize / 2
          endY = tile.layoutY() + cellSize
          stroke = lineColor
          strokeWidth = lineWidth
          strokeLineCap.value = StrokeLineCap.Butt
        }
  

  def Dot = new Circle {
        centerX = tile.layoutX() + cellSize / 2
        centerY = tile.layoutY() + cellSize / 2
        radius = circleRadius
        fill = circleColor
      }
  
  def halfHLineRight = new Line {
          startX = tile.layoutX() + cellSize / 2 
          startY = tile.layoutY() + cellSize / 2
          endX = tile.layoutX() + cellSize
          endY = tile.layoutY() + cellSize / 2
          stroke = lineColor
          strokeWidth = lineWidth
          strokeLineCap.value = StrokeLineCap.Butt
        }

  def halfHLineLeft = new Line {
          startX = tile.layoutX() 
          startY = tile.layoutY() + cellSize / 2
          endX = tile.layoutX() + cellSize /2
          endY = tile.layoutY() + cellSize / 2
          stroke = lineColor
          strokeWidth = lineWidth
          strokeLineCap.value = StrokeLineCap.Butt
        }
  
  def halfVLineDown = new Line {
          startX = tile.layoutX() + cellSize / 2
          startY = tile.layoutY() + cellSize /2
          endX = tile.layoutX() + cellSize / 2
          endY = tile.layoutY() + cellSize
          stroke = lineColor
          strokeWidth = lineWidth
          strokeLineCap.value = StrokeLineCap.Butt
        }
  
  def halfVLineUp = new Line {
          startX = tile.layoutX() + cellSize / 2
          startY = tile.layoutY()
          endX = tile.layoutX() + cellSize / 2
          endY = tile.layoutY() + cellSize / 2
          stroke = lineColor
          strokeWidth = lineWidth
          strokeLineCap.value = StrokeLineCap.Butt
        }
 
  dir match {
    case N => {
      children.add(halfVLineUp)
    }

    case S => {
      children.add(halfVLineDown)
    }
    case E => {
      children.add(halfHLineRight)
    }
    case W => {
      children.add(halfHLineLeft)
    }
  }
  //if end then children.add(Dot)


}



class Solution_Renderer(tiles: ArrayBuffer[ArrayBuffer[cellTile]] , mazeWidth:Double, mazeHeight:Double) {
  
  val cellSize = tiles(0)(0).tileWidth
    

  val lineWidth = cellSize/10
  val circleRadius = cellSize/3
  
  val lineColor = Color.Red 
  val circleColor = Color.Black

  val solutionPane = new Pane{}
  

  private var target:Cell = null
  private var targetShape:Circle = null 

  //resets any paths or targets, and adds the new target
  //def setTarget(t:Cell):Unit =
  //  this.target = t
  //  val tile = tiles(target.x)(target.y)
  //  targetShape = new Circle {
  //    centerX = tile.layoutX() + cellSize / 2
  //    centerY = tile.layoutY() + cellSize / 2
  //    radius = circleRadius
  //    fill = Color.Red
  //  }
  //  solutionPane.children.setAll(targetShape)
  
  def setTargets(t:ArrayBuffer[Cell]):Unit =
    this.target = t.minBy(c => c.dist) //Showing the hint for the nearest target
    for cell <- t do {
      val tile = tiles(cell.x)(cell.y)
      targetShape = new Circle {
        centerX = tile.layoutX() + cellSize / 2
        centerY = tile.layoutY() + cellSize / 2
        radius = circleRadius
        fill = Color.Red
      }
      solutionPane.children.add(targetShape)
    }
    
  def resetPathAndTargets():Unit = 
    solutionPane.children.setAll()
  
   



  def findDir(c1:Cell, c2:Cell) = 
    if c1.x + 1 == c2.x then 
      E
    else if c1.x - 1 == c2.x then
      W
    else if c1.y + 1 == c2.y then
      S
    else if c1.y - 1 == c2.y then
      N
    else
      throw Exception("Somthing is wrong")
  
 
  
  
  //in our tile rendering, every tile's connection was either to its east or south, so if it is east or south of t1, then it is its connection, if it is north or west of t1, meaning (east or south of t2) then it is a connection of t2 
  def findConnection(t1:cellTile, t2:cellTile, dir: Int) =
  
    dir match
      case E => {
        t1.predH
      }

      case S => {
        t1.predV
      }
     
      case W => {
        t2.predH
      }

      case N => {
        t2.predV
      }
      case _ => {
        throw Exception("Something is wrong.")
      }
  
  
  
  
  def makePath():Unit = 
  
        

    val overlayPane = new Pane {
      prefWidth = mazeWidth
      prefHeight = mazeHeight
      //style = "-fx-background-color: transparent;" // Make it see-through
    }
    
    val MaxIterations = tiles.length * tiles(0).length
    var i = 0

    var c1 = target
    var c2 = c1.pred
  
    //only half lines but we do both c1 and c2 tiles at every step
    while c2 != c1 do
      
      if i > MaxIterations then
        throw Exception("The loop is infinite, something went wrong with finding the path")

      
      val dir = findDir(c1, c2)
      val t1 = tiles(c1.x)(c1.y)
      val t2 = tiles(c2.x)(c2.y)
      //val tCon = findConnection(t1, t2, dir)
      val t1End = c1 == target
      val t2End = c2.pred == c2
      
      val path1 = routeGuide(dir, t1, cellSize, lineWidth, circleRadius, lineColor, circleColor, t1End)
      
      //direction from t2 to t1 is opposite
      val path2 = routeGuide(OPPOSITE(dir), t2, cellSize, lineWidth, circleRadius, lineColor, circleColor, t2End)
      

      //val con = routeGuide(tCon.exit, tCon, cellSize, lineWidth, circleRadius, lineColor, circleColor)
      //overlayPane.children.add(con)
      

      if (c1.exit & T) == T then path1.opacity = 0.3
      if (c2.exit & T) == T then path2.opacity = 0.3
      


      overlayPane.children.add(path1)
      overlayPane.children.add(path2)

      c1 = c2
      c2 = c1.pred
      i+=1
      

    solutionPane.children.setAll(overlayPane)
    //solutionPane.children.add(targetShape)
  

}
