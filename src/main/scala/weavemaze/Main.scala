package weavemaze

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Stage

import scalafx.animation.{KeyFrame, Timeline}
import scalafx.util.Duration
import scalafx.Includes.jfxScene2sfx
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.Pane


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
  
  val solution = new Pane{}
  val stackPane = new Pane {
    children = Seq(gridPane, solution)
  }
   
  //for scaling
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  stackPane.getTransforms().add(scale)
  
  val aspectRatio:Double = mazeWidth/mazeHeight

  var generateButton: Button = _
  var solveButton: Button = _
  var buttonBox: HBox = _

  def _update1():Unit =

    maze = Maze(15,15,0.3)
    maze_renderer = Maze_Renderer(maze)

    val new_pane = maze_renderer.generateMazeGui()
    
    gridPane = new_pane(0)
    mazeWidth = new_pane(1) 
    mazeHeight = new_pane(2)
    
    
    scalafx.application.Platform.runLater {
      stackPane.children.setAll(gridPane ,buttonBox)
    }      

  def _update2():Unit =

    maze.resetPath() 

    val (sx, sy) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
    val source = maze.grid(sx)(sy)
    source.pred = source
    maze.findPath(source)
    
    val (tx, ty) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
    val target = maze.grid(tx)(ty)
    

  

      
    

    val tiles = maze_renderer.tiles
    val solution_renderer = Solution_Renderer(tiles, mazeWidth, mazeHeight)
    
    
    val pathPane = solution_renderer.makePath(target)
  
    
    
    scalafx.application.Platform.runLater {
      stackPane.children.clear()
      stackPane.children.setAll(gridPane, pathPane ,buttonBox) 
    }
    
    
   
  
    

  override def start(): Unit = {
   
    
    
    //render buttons for update
    generateButton = new Button("Generate Maze") {
      onAction = _ => _update1()
    }

    solveButton = new Button("Solve Maze") {
      onAction = _ => _update2()
    }
      
    buttonBox = new HBox {
      children = Array(generateButton, solveButton)
      alignment = Pos.Center
      //style = "-fx-background-color: rgba(255, 255, 255, 0.7);"
      prefHeight = 50
    }

    buttonBox.setTranslateY(mazeHeight/2 + 5)

    //stackPane.children.add(buttonBox)
    
    //player initialization
    val (sx, sy) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
    val source = maze.grid(sx)(sy)
    source.pred = source
    maze.findPath(source)

    val player = Player("ME", source, maze_renderer.tiles, maze.grid)
    stackPane.children.add(player.box)
    
    
    //path initialization
    var target:Cell = null
    def newPath():Unit = 
      maze.resetPath()
      player.cell.pred = player.cell
      maze.findPath(player.cell)
      
      val (tx, ty) = (Random().nextInt(maze.width), Random().nextInt(maze.height))
      target = maze.grid(tx)(ty)
      
      val tiles = maze_renderer.tiles
      val solution_renderer = Solution_Renderer(tiles, mazeWidth, mazeHeight)
      val pathPane = solution_renderer.makePath(target)
      
      solution.children.setAll(pathPane)
    
    newPath()
    
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
            case KeyCode.Space => if player.cell == target then newPath()
            case _ => println("Invalid move")
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
    

    
    def _update3() = {
      _update1()
      _update2()

    }
      val timeline = new Timeline {
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(
        KeyFrame(Duration(5000000), onFinished = _ => _update2())
      )
    }  
    
    
    timeline.play()


    

 }
}
