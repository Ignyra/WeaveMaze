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
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.text.Font


import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Game(difficulty:Double = 0.3, numTargets: Int = 1) { 
  
  //Some Constants
  protected val MaxRows:Int = 100
  protected val MaxCols: Int = 200
  protected val MaxBridges: Double = 0.9
  
  //The Easy Settings
  protected var rows: Int = 5
  protected var cols: Int = 5
  protected var bridgeDensity: Double = 0.1
  protected var remainingTimeMS:Int = (rows*cols*bridgeDensity*50000).toInt //125 secs

  //Maze variables
  protected var maze:Maze = null
  protected var maze_renderer:Maze_Renderer = null
  protected var solution_renderer:Solution_Renderer = null
  protected var player:Player = null
  protected val targets:ArrayBuffer[Cell] = ArrayBuffer[Cell]()
  protected var remainingTargerts = numTargets 
  
  //Full Game Pane Init
  protected val GamePane = new Pane{}
  protected var scorePane: ScoreBar = null
  protected val scale = scalafx.scene.transform.Scale(1,1,0,0)
  protected val FullPane = new VBox{children = Seq(GamePane, scorePane)}
  FullPane.getTransforms().add(scale)



  def applyDifficulty:Unit = {
    //Difficulty is treated as a reserve, it is appliec to each of the main 4 variables until it is emptied. The division is random
    val range = 0.6
    val start = 0.4

    var remainingDiff = difficulty

    var currDiff = (range * Random().nextDouble() + start) * remainingDiff
    rows = (rows + currDiff * MaxRows).toInt
    remainingDiff -= currDiff
    
    currDiff = (range * Random().nextDouble() + start) * remainingDiff
    cols = (cols + currDiff * MaxCols).toInt
    remainingDiff -= currDiff
    
    currDiff = (range * Random().nextDouble() + start) * remainingDiff
    bridgeDensity = bridgeDensity + currDiff * MaxBridges
    remainingDiff -= currDiff
    
    //The remaining difficulty acts as a reduction factor to the usual allocated time
    remainingTimeMS = ((rows*cols*bridgeDensity*50000) * (1 - remainingDiff)).toInt

  }

  def initMaze:Unit = {
    this.maze = Maze(rows, cols, bridgeDensity) 
    this.maze_renderer = Maze_Renderer(maze)
    this.maze_renderer.generateMazeGui()
    this.solution_renderer = Solution_Renderer(maze_renderer.tiles, maze_renderer.mazeWidth, maze_renderer.mazeHeight)
    this.GamePane.children = Seq(maze_renderer.mazePane, solution_renderer.solutionPane)
    this.scorePane = ScoreBar(remainingTimeMS, maze_renderer.mazeWidth)
  }
  
  def initPlayers(name:String):Unit = {
    require(maze != null && maze_renderer != null)
    val (sx, sy) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
    val source = maze.grid(sx)(sy)
    player = Player(name, source, maze_renderer.tiles, maze.grid)
    this.GamePane.children.add(player.box)
  }

  def initTargets():Unit = {
    require(player != null)
    for _ <- 0 until numTargets do {
      val (tx, ty) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
      val target = maze.grid(tx)(ty)
      remainingTimeMS = (remainingTimeMS +  (1-difficulty)*target.dist*1000).toInt //more difficulty equals less time per unit of moving distance
      targets += target
    }
    solution_renderer.setTargets(targets)
  }

  def showPath():Unit = {
    maze.resetPath() //The path will start from the new place of the player
    player.cell._makeSource() //make the current cell the new source
    maze.findPath(player.cell)
    solution_renderer.makePath()
  }
  
  def hidePath():Unit = {solution_renderer.hidePath()}
  
  def checkPlayerAtTarget():Unit = {
    this.targets -= this.player.cell
    if this.targets.length != this.remainingTargerts then {
      //update score via addPoints
      //redraw targets with setTargets
    }
  }
  
  def start():Unit = {
    ???
  }
  
  
}       
  
class ScoreBar(private var remainingTimeMS:Int, val mazeWidth:Double, val barHeight:Double = 40) extends Pane {
  
  private var score:Int = 0
  

  private val bar = new Rectangle {
    width = mazeWidth
    height = barHeight
    fill = Color.FloralWhite
    stroke = Color.Crimson
    strokeWidth = 1
  }
  
  private val fontSize = barHeight/1.3
  private val timerText = new Text(formattedTime(remainingTimeMS)) {
    fill = Color.Black
    font = Font("Consolas", fontSize.toInt)
    layoutY = barHeight/2 
    layoutX = mazeWidth - 0.2*mazeWidth //End of the bar
  }
  private val scoreText = new Text(f"$score") {
    fill = Color.Black
    font = Font("Consolas", fontSize.toInt)
    layoutY = barHeight/2 
    layoutX = 0.2*mazeWidth //Start of the bar
  }

  def formattedTime(ms: Long): String = {
    val minutes = ms / 60000
    val seconds = (ms % 60000) / 1000
    val hundredths = (ms % 1000) / 10
    f"$minutes%02d:$seconds%02d.$hundredths%02d"
  }
  

  private var running = false  
  private val updateInterval = 10 //in milliseconds

  def startTimer(): Unit = {
    running = true
    Future {
      while (running && remainingTimeMS > 0) {
        Thread.sleep(updateInterval)
        remainingTimeMS -= updateInterval
        timerText.text = formattedTime(remainingTimeMS)
        scoreText.text = f"$score"
      }
      if (remainingTimeMS <= 0) {
        running = false
      }
    }
  } 
  
  def pause():Unit = {running = false}
  def resume():Unit = {startTimer()}
  def addTime(timeMS:Int) = {remainingTimeMS += timeMS}
  def isOver = remainingTimeMS <= 0
  
  def addPoints(points:Int):Unit = {score += points}
  def getScore:Int = score 
  
  this.children = Seq(bar, timerText, scoreText)
}


class MultiPlayerGame(val difficulty:Double = 0.3, val numPlayers:Int = 1, var numTargets: Int = 1) extends Game(difficulty, numTargets) {
  
  def initPlayers(Names:Array[String]):Array[Player] = {
    require(maze != null && maze_renderer != null)
    val players = ArrayBuffer[Player]()
    for (name <- Names)  do {
      val (sx, sy) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
      val source = maze.grid(sx)(sy)
      source._makeSource()
      maze.findPath(source)
      players += Player(name, source, maze_renderer.tiles, maze.grid)
    }
    players.toArray
  }

}
