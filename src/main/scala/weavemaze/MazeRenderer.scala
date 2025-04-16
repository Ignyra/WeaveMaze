package weavemaze

import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.layout.Pane

import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.geometry.Insets
import scalafx.scene.layout.BackgroundImage


import scala.collection.mutable.ArrayBuffer



class cellTile(val exit:Int, val tileHeight:Double, val tileWidth:Double, edgeThickness:Double, tileColor:Color, edgeColor: Color, var predV:cellTile = null, var predH:cellTile = null) extends Pane {
  val tile = new Rectangle {
    width = tileWidth
    height = tileHeight
    fill = tileColor
    stroke = Color.Transparent
    strokeWidth = 0
  }
  val TopEdge = new Rectangle {
    width = tileWidth
    height = edgeThickness
    fill = edgeColor
    stroke = Color.Transparent
    layoutY = 0
    strokeWidth = 0

  }
  val LeftEdge = new Rectangle {
    width = edgeThickness
    height = tileHeight
    fill = edgeColor
    stroke = Color.Transparent
    layoutX = 0
    strokeWidth = 0
  }
  
  val BottomEdge = new Rectangle {
    width = tileWidth
    height = edgeThickness
    fill = edgeColor
    stroke = Color.Transparent
    layoutY = tileHeight - edgeThickness
    strokeWidth = 0
  }
  val RightEdge = new Rectangle {
    width = edgeThickness
    height = tileHeight
    fill = edgeColor
    stroke = Color.Transparent
    layoutX = tileWidth - edgeThickness
  }

  
  children = Seq(tile) 

  
  if ((exit & N) == 0) then
    children.add(TopEdge)
       
  if ((exit & S) == 0) then 
    children.add(BottomEdge)
    
  if ((exit & E) == 0) then 
    children.add(RightEdge)
    
  if ((exit & W) == 0) then 
    children.add(LeftEdge)
    

}

case class connectionHorizontalTile(override val tileHeight:Double, override val tileWidth:Double, edgeThickness:Double, tileColor:Color, edgeColor:Color) extends cellTile(E | W, tileHeight, tileWidth, edgeThickness, tileColor, edgeColor) 


case class connectionVerticalTile(override val tileHeight:Double, override val tileWidth:Double, edgeThickness:Double, tileColor:Color, edgeColor:Color) extends cellTile(S | N, tileHeight, tileWidth, edgeThickness, tileColor, edgeColor) 


class Maze_Renderer(maze:Maze, cellSize:Double = 15.0) {
  

  val cols = maze.width
  val rows = maze.height
  val mazePane = new Pane {}
  
  private var mainGridPane:Pane = null
  
  val spacingSize = cellSize/2.0

  val mazeWidth = cols * (cellSize + spacingSize) - spacingSize 
  val mazeHeight = rows * (cellSize + spacingSize) - spacingSize
  
  val tiles: ArrayBuffer[ArrayBuffer[cellTile]]= ArrayBuffer.fill(cols, rows)(null)
  
  def showMaze():Unit = 
    require(mainGridPane != null)
    this.mazePane.children.setAll(mainGridPane)
  
  def hideMaze():Unit = 
    this.mazePane.children.setAll()

  
  def generateMazeGui():Unit = 
    
     

  
    val edgeThickness = cellSize/7.0
    val tileColor = Color.White
    val edgeColor = Color.Black
    val backgroundColor = Color.AntiqueWhite
     
    val gridPane = new Pane {
      prefWidth = mazeWidth
      prefHeight = mazeHeight
      //style = s"-fx-background-color: ${backgroundColor.toString.drop(2).take(6)};"
    }
    
    gridPane.background = new Background(Array(
      new BackgroundFill(backgroundColor, CornerRadii.Empty, Insets.Empty)
    ))
  
      
    
    for x <- 0 until cols do 
      for y <- 0 until rows do 
        
        val cell = maze.grid(x)(y)
        
        val tile = cellTile(cell.exit, cellSize, cellSize, edgeThickness, tileColor, edgeColor)
  
        tile.layoutX = x * (cellSize + spacingSize)
        tile.layoutY = y * (cellSize + spacingSize)
   
        
  
        //Most front: bridges since their tunnel connections can overlay over their actual edges
        if ((cell.exit & B) == B) then 
          tile.delegate.setViewOrder(1) 
        //connections should be above non-bridges since the exits (white edges) can overlay on some corners from the connections making it look ugly
        else
          tile.delegate.setViewOrder(3)
        
        //cell.tile = tile
        this.tiles(x)(y) = tile
  
        gridPane.children.add(tile)
        
        
  
        // ADD HORIZONTAL CONNECTION IF THERE IS AN EXIT OR A PERPENDICULAR BRIDGE WITH A PATH UNDER IT (EAST EXIT UNDER)
        if ((cell.exit & E) == E) || (((cell.exit & B) == B) &&  ((cell.exit & E) == 0)) then
          val connectionTile = connectionHorizontalTile(cellSize, spacingSize + 2*edgeThickness, edgeThickness, tileColor,edgeColor)
          connectionTile.layoutX = tile.layoutX.value + cellSize - edgeThickness
          connectionTile.layoutY = tile.layoutY.value
          connectionTile.delegate.setViewOrder(2)
          tile.predH = connectionTile
          gridPane.children.add(connectionTile)
       
        // ADD VERTICAL CONNECTION IF THERE IS AN EXIT OR A PERPENDICULAR BRIDGE WITH A PATH UNDER IT (SOUTH EXIT UNDER) 
        if ((cell.exit & S) == S) ||  (((cell.exit & B) == B) &&  ((cell.exit & S) == 0)) then
          val connectionTile = connectionVerticalTile(spacingSize + 2*edgeThickness, cellSize, edgeThickness, tileColor,edgeColor)
          connectionTile.layoutX = tile.layoutX.value
          connectionTile.layoutY = tile.layoutY.value + cellSize - edgeThickness
          connectionTile.delegate.setViewOrder(2)
          tile.predV = connectionTile
          gridPane.children.add(connectionTile)
      
  
       // works but sometimes adds to the front and covers some edges because to goes backwards after main tiles have been put
       // if ((cell.exit & W) == W) then
       //   val connectionTile = connectionHorizontalTile(cellSize, spacingSize + 2*edgeThickness, edgeThickness, tileColor,edgeColor)
       //   connectionTile.layoutX = tile.layoutX.value - spacingSize - edgeThickness
       //   connectionTile.layoutY = tile.layoutY.value
       //   gridPane.children.add(connectionTile)
       //   println("h tile")
       // if ((cell.exit & N) == N) then
       //   val connectionTile = connectionVerticalTile(spacingSize + 2*edgeThickness, cellSize, edgeThickness, tileColor,edgeColor)
       //   connectionTile.layoutX = tile.layoutX.value
       //   connectionTile.layoutY = tile.layoutY.value - spacingSize -edgeThickness
       //   gridPane.children.add(connectionTile)
       //   println("v tile")
       //
  
    this.mainGridPane = gridPane
    this.mazePane.children.setAll(mainGridPane)

} 
