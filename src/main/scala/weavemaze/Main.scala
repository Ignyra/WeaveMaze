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

import scalafx.Includes._
import scalafx.scene.control.Button
import scalafx.scene.layout.{VBox, HBox}
import scalafx.geometry.Pos
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.beans.binding.Bindings
import scalafx.application.Platform


import scala.util.Random

object WeaveMaze extends JFXApp3 {
  
  //initialize maze and render
  var maze = Maze(15,30,0.3)
  var maze_renderer = Maze_Renderer(maze)
  var (gridPane, mazeWidth, mazeHeight) = maze_renderer.generateMazeGui()
  
  val tiles = maze_renderer.tiles
  val solution_renderer = Solution_Renderer(tiles, mazeWidth, mazeHeight)


  val stackPane = new Pane {
    children = Seq(gridPane, solution_renderer.solutionPane)
  }
   
  //for scaling
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  stackPane.getTransforms().add(scale)
  
  val aspectRatio:Double = mazeWidth/mazeHeight

    
   
  def saveScreenshot(pane: Pane, filename: String, scale:Double): Unit = {
    
    val tempX = pane.scaleX.value
    val tempY = pane.scaleY.value

    pane.scaleX = scale
    pane.scaleY = scale


    val wImage = pane.snapshot(null, null)
    val bufferedImage = SwingFXUtils.fromFXImage(wImage, null)
    ImageIO.write(bufferedImage, "png", new File(filename))
    println("Screenshot saved")
    
    pane.scaleX = tempX
    pane.scaleY = tempY

  }
    

  override def start(): Unit = {
   
    
    
   
    //player initialization
    val (sx, sy) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
    val source = maze.grid(sx)(sy)
    source._makeSource()
    maze.findPath(source)

    val player = Player("ME", source, maze_renderer.tiles, maze.grid)
    stackPane.children.add(player.box)
    
    
    //target initialization
    var target:Cell = null
    def newTarget():Unit = 
      val (tx, ty) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
      target = maze.grid(tx)(ty)
      solution_renderer.setTarget(target)


    newTarget()

    //path initialization
    def showPath():Unit = 
      maze.resetPath()
      player.cell._makeSource() //make the current cell the new source
      maze.findPath(player.cell)
      
      solution_renderer.makePath()
     
    def hidePath():Unit = 
      solution_renderer.hidePath()
    
    // Set up the primary stage
    stage = new JFXApp3.PrimaryStage {
      //resizable = false
      title = "Weave Maze"
      scene = new Scene(mazeWidth, mazeHeight) {
        //resizable = true
        root = stackPane
        //controls initialization
        onKeyPressed = (event: KeyEvent) => {
          event.code match {
            case KeyCode.Up => player.move(N)
            case KeyCode.Down => player.move(S)
            case KeyCode.Left => player.move(W)
            case KeyCode.Right => player.move(E)
            case KeyCode.Enter => if player.cell == target then newTarget()
            case KeyCode.Space => showPath()
            case KeyCode.S => saveScreenshot(stackPane, "game.png", 2)
            case _ => println("Invalid move")
          }
        }
        
        onKeyReleased = (event: KeyEvent) => {
          event.code match {
            case KeyCode.Space => hidePath()
            case _ => println("")
          }
        }

        scale.xProperty().bind(this.widthProperty().divide(mazeWidth))  
        //scale.yProperty().bind(scale.xProperty())
        scale.yProperty().bind(this.heightProperty().divide(mazeHeight))
        
        
      }

    }

    

    //var fixingAspectRatio = false
    //  

    //stage.widthProperty.onChange {(_,_,newWidth) =>
    //  if (!fixingAspectRatio) then 

    //    fixingAspectRatio = true
    //    Platform.runLater{
    //      stage.setHeight(newWidth.doubleValue() / aspectRatio)
    //      fixingAspectRatio = false
    //    }
    //}

    //stage.heightProperty.onChange {(_,_,newHeight) =>
    //  if (!fixingAspectRatio) then 

    //    fixingAspectRatio = true
    //    Platform.runLater{
    //      stage.setWidth(newHeight.doubleValue() * aspectRatio)
    //      fixingAspectRatio = false
    //    }
    //}
    

    


    //val timeline = new Timeline {
    //  cycleCount = Timeline.Indefinite
    //  keyFrames = Seq(
    //    KeyFrame(Duration(5000000), onFinished = _ => _update2())
    //  )
    //}  
    //
    //
    //timeline.play()


    

 }
}
