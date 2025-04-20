package weavemaze

import scalafx.scene.Scene

import scalafx.Includes.jfxScene2sfx
import scalafx.scene.layout.{Pane, Region, HBox, Priority}


import scalafx.embed.swing.SwingFXUtils
import javax.imageio.ImageIO
import java.io.File
import javafx.scene.image.WritableImage

import scalafx.Includes._
import scalafx.scene.control.Button
import scalafx.geometry.Pos
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.beans.binding.Bindings
import scalafx.application.Platform
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.geometry.Insets
import scalafx.scene.text.{Text, Font}
import scalafx.geometry.Pos
import scalafx.scene.shape.StrokeType.Inside
import scalafx.scene.effect.Glow

import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.HashMap
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter




class Game(name:String, difficulty:Double = 0.3, numTargets: Int = 3, var infinite:Boolean = false, val userRows:Int = 0, val userCols:Int = 0) { 

  //Some Constants
  protected val MaxRows:Int = 100
  protected val MaxCols: Int = 200
  protected val MaxBridges: Double = 0.9
  protected val minRows: Int = 5
  protected val minCols: Int = 5
  protected val minBridgeDensity: Double = 0.1
  protected val hintsFactor = 3
  protected val timeFactor = 1000

  //The Easy Settings
  protected var rows: Int = minRows
  protected var cols: Int = minCols
  protected var bridgeDensity: Double = minBridgeDensity
  protected var initTimeMS:Int = (rows*cols*bridgeDensity*timeFactor).toInt //125 secs


  //Maze variables
  protected var maze:Maze = null
  protected var maze_renderer:Maze_Renderer = null
  protected var solution_renderer:Solution_Renderer = null
  protected var player:Player = null
  protected val targets:ArrayBuffer[Cell] = ArrayBuffer[Cell]()
  protected var remainingTargets = numTargets
  protected val targetsDist = HashMap[Cell, Int]()
  protected var remainingHints:Int = numTargets * hintsFactor
  protected var hintShown = false
  protected var MazeName:String = null
  
  //Full Game Pane Init
  protected val GamePane = new Pane{}
  protected var scorePane: ScoreBar = null
  protected val scale = scalafx.scene.transform.Scale(1,1,0,0)
  protected val FullPane = new Pane {children = Seq(GamePane)}
  FullPane.getTransforms().add(scale)
  
  //Score Constants
  protected val TargetScore = 200
  protected val TimeScore = 100000

  protected var Start = false

  def applyDifficulty():Unit = {

    var timeDifficulty = difficulty
    if userRows < minRows || userCols < minCols  then  
      cols = (minCols + difficulty * MaxCols).toInt
      rows = (minRows + difficulty * MaxRows).toInt
    else
      rows = userRows
      cols = userCols
      val difficultyDiff = difficulty - ((userRows - minRows)/MaxRows + (userCols - minCols)/MaxCols)/2 // requested difficulty - approximated difficulty
      timeDifficulty += difficultyDiff

    if MazeName == null then 
      bridgeDensity = minBridgeDensity + difficulty * MaxBridges
    else
      //loaded maze, no setting rows,cols,bridgeDensity
      timeDifficulty = difficulty
      val difficultyDiff = difficulty - ((userRows - minRows)/MaxRows + (userCols - minCols)/MaxCols + (bridgeDensity - minBridgeDensity)/MaxBridges)/3 
      timeDifficulty += difficultyDiff

    initTimeMS = ((rows*cols*bridgeDensity*timeFactor) * (1 - timeDifficulty)).toInt
  }
  
  //should be called before start() if you want to load an old maze
  def setAndLoadMaze(filename:String):Unit = {
    this.maze = Maze(0,0,0)
    this.maze.loadMaze(filename)
    this.MazeName = filename
    this.rows = this.maze.height
    this.cols = this.maze.width
    this.bridgeDensity = this.maze.bridgeDensity
  }


  def initMaze():Unit = {
    if MazeName == null then //New Maze else use the loaded maze
      this.maze = Maze(rows, cols, bridgeDensity)
      this.maze.makeNewMaze()
    this.maze_renderer = Maze_Renderer(maze)
    this.maze_renderer.generateMazeGui()
    this.solution_renderer = Solution_Renderer(maze_renderer.tiles, maze_renderer.mazeWidth, maze_renderer.mazeHeight)
    this.GamePane.children = Seq(maze_renderer.mazePane, solution_renderer.solutionPane)
    this.scorePane = ScoreBar(initTimeMS, numTargets, remainingHints, infinite, maze_renderer.mazeWidth, maze_renderer.mazeHeight, maze_renderer.mazeHeight * 0.12)
    FullPane.children += scorePane
    GamePane.effect = Glow(GlowLevel)
  }

  def initPlayers():Unit = {
    require(maze != null && maze_renderer != null)
    val (sx, sy) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
    val source = maze.grid(sx)(sy)
    source._makeSource()
    maze.findPath(source)
    player = Player(name, source, maze_renderer.tiles, maze.grid)
    this.GamePane.children.add(player.box)
  }

  def initTargets():Unit = {
    require(player != null) //Make sure initPlayers is called first for player and path init
    for _ <- 0 until numTargets do {
      val (tx, ty) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
      val target = maze.grid(tx)(ty)
      targetsDist(target) = target.dist
      this.scorePane.addMoves(target.dist)
      if numTargets == 1 then  this.scorePane.addMoves((difficulty*target.dist).toInt)
      val additionalTime = ((1-difficulty)*target.dist*timeFactor).toInt //more difficulty equals less time per unit of moving distance
      this.scorePane.addTime(additionalTime)
      targets += target
    }
    solution_renderer.setTargets(targets)
  }

  def showPath():Unit = {
    maze.resetPath() //The path will start from the new place of the player
    player.cell._makeSource() //make the current cell the new source
    maze.findPath(player.cell)
    solution_renderer.makePath()
    solution_renderer.setTargets(this.targets)
  }

  def hidePath():Unit = {
    solution_renderer.resetPathAndTargets()
    solution_renderer.setTargets(this.targets)
  }

  def showHint():Unit = {
    if remainingHints > 0 && !hintShown && scorePane.getCoolDownTimer <= 0 then { //need remaining hints, shouldn't be showing already and no cooldown left
      hintShown = true
      this.maze_renderer.hideMaze()
      this.showPath()
      this.scorePane.setHintTimer((5000 * difficulty).toInt)
    }
  }

  def hideHint():Unit = {
    if remainingHints > 0 && hintShown then {
      this.hidePath()
      this.remainingHints -= 1
      this.scorePane.setNumHints(remainingHints)
      this.maze_renderer.showMaze()
      this.hintShown = false
      this.scorePane.setCoolDownTimer(3000)//set cooldown for 3 secs/
    }
  }

  def checkPlayerAtTarget():Unit = {
    this.targets -= this.player.cell
    if this.targets.length != this.remainingTargets then {
      this.remainingTargets = this.targets.length
      this.scorePane.setRemainingTargets(this.remainingTargets)
      this.scorePane.addPoints(TargetScore)
      val distpertimescore = TimeScore * difficulty * targetsDist(this.player.cell)/scorePane.getAndResetTimeSpent
      this.scorePane.addPoints(distpertimescore.toInt)
      this.solution_renderer.resetPathAndTargets()

      if this.targets.length == 0 then {
        if infinite then
          val prevRoundMoves = scorePane.getRemainingMoves
          this.initTargets()//refills moves and adds perTarget time and adds the targets
          this.scorePane.addMoves(-(prevRoundMoves * difficulty).toInt) //more difficulty, less accumilation of previous round moves
          this.scorePane.addPoints(400)
          this.scorePane.addTime(((rows*cols*bridgeDensity*timeFactor) * (1 - difficulty)).toInt) //full difficulty this time
          this.remainingHints = numTargets * hintsFactor
          this.scorePane.setNumHints(this.remainingHints)
          this.remainingTargets = numTargets
          this.scorePane.setRemainingTargets(this.remainingTargets)
      }
      else {
        val nextDist = this.targets.minBy(c => c.dist).dist
        val additionalMovesNeeded = nextDist - this.scorePane.getRemainingMoves
        if additionalMovesNeeded >0 then {this.scorePane.addMoves(additionalMovesNeeded)}//add the moves nessescery for closest target
        else {this.scorePane.addMoves(((1-difficulty) * nextDist).toInt)} //reward with more moves based on difficulty
        this.solution_renderer.setTargets(targets)
      }

    }
  }
  
  //attach listeners
  def saveScreenshot(pane: Pane, filename: String, res:Double): Unit = {
    val tempX = pane.scaleX.value
    val tempY = pane.scaleY.value
    pane.scaleX = res
    pane.scaleY = res

    val wImage = new WritableImage(
      pane.layoutBounds().getWidth.toInt * res.toInt,
      pane.layoutBounds().getHeight.toInt * res.toInt
    )

    pane.snapshot(null, wImage)
    val bufferedImage = SwingFXUtils.fromFXImage(wImage, null)
    ImageIO.write(bufferedImage, "png", new File(filename))
    println(f"Screenshot saved: $filename")

    pane.scaleX = tempX
    pane.scaleY = tempY

  }

  def saveScore(filePath: String = "scoreboard.txt"): Unit = {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val line = s"${timestamp}, ${name}, score=${this.scorePane.getScore}, diff=${difficulty}"
    val file = new java.io.PrintWriter(new java.io.FileOutputStream(filePath, true))
    file.println(line)
    file.close()
  }

  def attach(scene:Scene):Unit = {
    //controls
    scene.addEventHandler(KeyEvent.KeyPressed, (e:KeyEvent) => {
      if this.scorePane.isRunning then {
        e.code match {
          case KeyCode.Up => player.move(N)
          case KeyCode.Down => player.move(S)
          case KeyCode.Left => player.move(W)
          case KeyCode.Right => player.move(E)
          case KeyCode.Enter => checkPlayerAtTarget()
          case KeyCode.Space => showHint()
          case KeyCode.R => if infinite then this.scorePane.pause() //then trigger Done Scene with WON and show the full run score
          case KeyCode.S => 
            saveScreenshot(FullPane, "game.png", 2)
            this.maze.saveMaze("Maze") //testing
            this.saveScore() //testing
          case _ => null
        }

        this.scorePane.addMoves(-this.player.getAndResetMoves)

        if hintShown && this.scorePane.getHintTimer <=0 then {
          hideHint()
        }

      } else {
        if !this.Start then {
          this.scorePane.resume()
          this.Start = true
        }
      }
    })
    scene.addEventHandler(KeyEvent.KeyReleased, (e:KeyEvent) => {
      if e.code == KeyCode.Space then {
        hideHint()
      }
    })
  }

  def start():Scene = {
    this.applyDifficulty()
    this.initMaze()
    this.initPlayers()
    this.initTargets()
    val w = maze_renderer.mazeWidth
    val h = maze_renderer.mazeHeight + scorePane.barHeight
    
    val scene = new Scene(w, h) { //replace with Menu's Scene current width and height so transition looks smoother
      root = FullPane
      fill = Color.web(BackgroundColor, BackgroundColorOP)
    }
    this.attach(scene)
    scale.xProperty().bind(scene.widthProperty().divide(w))  
    scale.yProperty().bind(scene.heightProperty().divide(h))
    scene
  }

}

class ScoreBar(private var remainingTimeMS:Int,val numTargets:Int, val nHints:Int, val infinite:Boolean, val mazeWidth:Double, val mazeHeight:Double, val barHeight:Double = 40) extends Pane {
  

  this.layoutX = 0 
  this.layoutY = mazeHeight

  
  private var score:Int = 0
  private var remainingMoves:Int = 0
  private var initTime:Int = 0
  private var remainingHints:Int = nHints 
  private var remainingHintTime:Int = 0
  private var cooldownTime:Int = 0
  private var remainingTargets:Int = numTargets

  private val bar = new Rectangle {
    width = mazeWidth
    height = barHeight
    fill = Color.web(InfoBarColor, InfoBarColorOP)
    stroke = Color.web(InfoBarEdgeColor, InfoBarEdgeColorOP)
    strokeWidth = barHeight/30
    strokeType = Inside
  }

  private val textfont = Font.loadFont(this.getClass.getResourceAsStream(FontType), barHeight/4.5)
  private val textColor = Color.web(InfoTextColor, InfoTextColorOP)
  private val spacingEdges = "    "
  private val timerText = new Text(formattedTime(0)) {
    fill = textColor
    font = textfont
  }
  private val scoreText = new Text(f"\udb85\udf42 Score: $score") {
    fill = textColor
    font = textfont
  }
  private val movesText = new Text(f"\uF013 Moves Left: $remainingMoves") {
    fill = textColor
    font = textfont
  }
  private val hintsText = new Text(spacingEdges + f"\uf400 Hints Left: $remainingHints") {    
    fill = textColor
    font = textfont
  }
  private val targetsText = new Text(f"\udb81\udcfe Targets Left: $remainingTargets") {
    fill = textColor
    font = textfont
  }

  def formattedTime(ms: Int): String = {
    val minutes = ms / 60000
    val seconds = (ms % 60000) / 1000
    val hundredths = (ms % 1000) / 10
    f"\udb81\udd1b Time: $minutes%02d:$seconds%02d.$hundredths%02d" + spacingEdges
  }

  val hbox = new HBox {
    spacing = 0
    alignment = Pos.Center
    prefWidth = mazeWidth
    prefHeight = barHeight
  }
  val txts = Seq(hintsText, movesText, scoreText, targetsText, timerText)
  for ((txt, idx) <- txts.zipWithIndex) {
    hbox.children += txt

    if (idx < txts.length - 1) {
      val spacer = new Region()
      HBox.setHgrow(spacer, Priority.Always)
      hbox.children += spacer
    }
  }

  private var running = false  
  private val updateInterval = 10 //in milliseconds

  def startTimer(): Unit = {
    running = true
    Future {
      while (running && remainingTimeMS > 0) {
        Thread.sleep(updateInterval)
        remainingTimeMS -= updateInterval
        remainingHintTime -= updateInterval
        cooldownTime -= updateInterval
        initTime += updateInterval
        Platform.runLater {
          timerText.text = formattedTime(remainingTimeMS)
          scoreText.text = f"\udb85\udf42 Score: $score"
          movesText.text = f"\uF013 MovesLeft: $remainingMoves"
          targetsText.text = f"\udb81\udcfe Targets Left: $remainingTargets"
          if cooldownTime > 0 then {hintsText.text =spacingEdges + f"\uf400 Hints Left: $remainingHints (\udb82\udd7f $cooldownTime)"}
          else {hintsText.text = spacingEdges + f"\uf400 Hints Left: $remainingHints"}
        }
        if isOver then {
          running = false
          //trigger Done Scene: Won if remainingTargets == 0 else Lost
        }
      }
    }
  }


  
  def isRunning = running
  def pause():Unit = {running = false}
  def resume():Unit = {startTimer()}
  def addTime(timeMS:Int) = {remainingTimeMS += timeMS}
  def getTime:Int = remainingTimeMS
  def getAndResetTimeSpent = {
    val t = initTime
    initTime = 0
    t
  }
  def isOver = remainingTimeMS <= 0 || remainingMoves <= 0 || (remainingTargets == 0 && !infinite)

  def addPoints(points:Int):Unit = {score += points}
  def getScore:Int = score 

  def getRemainingMoves = remainingMoves
  def addMoves(m:Int):Unit = {remainingMoves += m}

  def setNumHints(h:Int):Unit = {remainingHints = h}
  def setHintTimer(t:Int):Unit = {remainingHintTime = t}
  def getHintTimer:Int = remainingHintTime
  def getCoolDownTimer:Int = cooldownTime
  def setCoolDownTimer(t:Int):Unit = {cooldownTime = t}
  
  def setRemainingTargets(n:Int) = {remainingTargets = n}
  
  this.effect = Glow(GlowLevel)
  this.children = Seq(bar, hbox)
}


class MultiPlayerGame(val difficulty:Double = 0.3, val numPlayers:Int = 1, var numTargets: Int = 1) extends Game("Me", difficulty, numTargets,false, 0,0) {
  
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
