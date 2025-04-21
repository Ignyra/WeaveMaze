package weavemaze

import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import scalafx.scene.text.Text
import scalafx.scene.text.{Text, Font}
import scalafx.beans.property.StringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Paint
import scalafx.scene.shape.Rectangle
import scalafx.scene.Node
import scalafx.scene.Cursor
import scalafx.beans.binding.Bindings
import scalafx.beans.value.ObservableValue
import scalafx.animation.PauseTransition
import scalafx.util.Duration
import scalafx.scene.control.TextField
import scalafx.scene.control.ScrollPane

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.compiletime.ops.double
import scalafx.collections.ObservableBuffer

val StandardWidth = 800
val StandardHeight = 600
//var sharedColor = ObjectProperty[Color](Color.Black) //scale ObjectProperty bindings don't work with Color, jave SimpleObjectProperty does work

object Menu extends Scene (StandardWidth, StandardHeight){
  fill <== UIColors.BackgroundColor
  val title = new Text("Weave Maze") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/15)
    fill <== UIColors.InfoTextColor
  }
  val NewGame = sceneButton("New Game", ()=> WeaveMaze.stage.scene = NewGameScene )
  val LoadGame = sceneButton("Load Game", ()=> WeaveMaze.stage.scene = LoadGameScene )
  val scoreBoard = sceneButton("ScoreBoard", ()=> WeaveMaze.stage.scene = ScoreBoard )
  val ChangeColor = sceneButton("Change Theme", ()=>  UIColors.switchTheme())
  val buttonsbox = sceneVbox(30, sceneHbox(20, NewGame, LoadGame), scoreBoard, ChangeColor)
  val finalbox = sceneVbox(100, title, buttonsbox)
  
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  finalbox.getTransforms().add(scale)
  this.content = finalbox
  scale.xProperty().bind(this.widthProperty().divide(StandardWidth))  
  scale.yProperty().bind(this.heightProperty().divide(StandardHeight))
  var gameScene:Scene = null
  var game:Game = null
}


object NewGameScene extends Scene (StandardWidth, StandardHeight){
  fill <== UIColors.BackgroundColor 
  val title = new Text("Weave Maze") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/15)
    fill <== UIColors.InfoTextColor
  }
  val randomDims = new CheckBox("")
  val infinite = new CheckBox("")
  val colsSlider = new Slider {
    min = MinCols
    max = MaxCols
    value =30
    blockIncrement = 1
    majorTickUnit = 1
    minorTickCount = 0
    snapToTicks = true
    disable <== randomDims.selected
  }
  val rowsSlider = new Slider{
    min = MinRows
    max = MaxRows
    value = 15
    blockIncrement = 1
    majorTickUnit = 1
    minorTickCount = 0
    snapToTicks = true // Integers Only
    disable <== randomDims.selected
  }
  val difficultySlider = new Slider {
    min = 0.01
    max = 1
    value = 0.5
    blockIncrement = 0.05
    majorTickUnit = 1
    minorTickCount = 0
  }
  val targetsSlider = new Slider {
    min = 1
    max <== Bindings.when(randomDims.selected).choose(100).otherwise(rowsSlider.value.multiply(colsSlider.value).divide(5)) //Limit the amount of targets based on dims
    value = 3
    snapToTicks = true
    blockIncrement = 1
    majorTickUnit = 1
    minorTickCount = 0
  }
  val nameField = TextField()
  nameField.promptText = "Player X"
  
  val rowBindLabel = Bindings.when(randomDims.selected).choose(Double.NaN).otherwise(rowsSlider.value)
  val colBindLabel = Bindings.when(randomDims.selected).choose(Double.NaN).otherwise(colsSlider.value)
  
  val slidersSpacing = 10
  val sliderHspacing = 150
   
  val labels = sceneVbox(slidersSpacing,
    sceneHbox(sliderHspacing*1.85, sceneButton("Infinite Mode?", null, null, 60, 2.5, 20),infinite),
    sceneHbox(sliderHspacing*1.85, sceneButton("Random Dimensions based on Difficulty?", null, null, 60, 2.5, 20),randomDims),
    sceneHbox(sliderHspacing, sceneButton("", null, rowBindLabel.asString("Number of Rows: %.0f"), 60, 2.5, 20),rowsSlider),
    sceneHbox(sliderHspacing, sceneButton("", null, colBindLabel.asString("Number of Columns: %.0f"), 60, 2.5, 20),colsSlider),
    sceneHbox(sliderHspacing, sceneButton("", null, difficultySlider.value.asString("Difficulty: %.2f"), 60, 2.5, 20),difficultySlider),
    sceneHbox(sliderHspacing, sceneButton("", null, targetsSlider.value.asString("Number of Targets: %.0f"), 60, 2.5, 20),targetsSlider),
    sceneHbox(sliderHspacing, sceneButton("Enter Player Name:", null,null,60,2.5,20), nameField)
  )

  def startGame():Unit = {
    val game = Game(nameField.text.value, difficultySlider.value.value.toDouble,targetsSlider.value.value.toInt,infinite.selected.value, rowBindLabel.value.intValue(), colBindLabel.value.intValue())
    WeaveMaze.stage.scene = game.start()
  }

  val startButton = sceneButton("START", startGame, null, 25, 3, 8)
  val backButton = sceneButton("back", ()=>WeaveMaze.stage.scene = Menu,null, 25, 3, 8)
  
  val finalbox = sceneVbox(slidersSpacing, title, labels, startButton, backButton)
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  finalbox.getTransforms().add(scale)
  content = finalbox
  scale.xProperty().bind(this.widthProperty().divide(StandardWidth))
  scale.yProperty().bind(this.heightProperty().divide(StandardHeight))
}


object LoadGameScene extends Scene (StandardWidth, StandardHeight){
  fill <== UIColors.BackgroundColor 
  val title = new Text("Weave Maze") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/15)
    fill <== UIColors.InfoTextColor
  }
  val infinite = new CheckBox("")
  val difficultySlider = new Slider {
    min = 0.01
    max = 1
    value = 0.5
    blockIncrement = 0.05
    majorTickUnit = 1
    minorTickCount = 0
  }
  val targetsSlider = new Slider {
    min = 1
    max = 200  
    value = 3
    snapToTicks = true
    blockIncrement = 1
    majorTickUnit = 1
    minorTickCount = 0
  }
  val nameField = TextField()
  nameField.promptText = "Player X"
  val mazeField = TextField()
  mazeField.promptText="Maze X"
  val loadText = StringProperty("Enter Maze Name:")
  
  var isLoaded = false
  var game:Game = null
  def loadGame():Unit = {
    game = Game(nameField.text.value, difficultySlider.value.value.toDouble,targetsSlider.value.value.toInt,infinite.selected.value, 0, 0)
    try {
      game.setAndLoadMaze(mazeField.text.value)
      isLoaded = true
      loadText.value = "Maze Loaded! You can Start"
    } catch {
      case e: java.io.FileNotFoundException => loadText.value = "Maze File Missing"
      case e:Exception => loadText.value = "Maze File Corrupted"
    }

    if !isLoaded then
      val pause = PauseTransition(Duration(1500))
      pause.onFinished = _ => loadText.value = "Enter Maze Name:"
      pause.play()
  }
  
  val slidersSpacing = 10
  val sliderHspacing = 150
   
  val labels = sceneVbox(slidersSpacing,
    sceneHbox(sliderHspacing*1.85, sceneButton("Infinite Mode?", null, null, 60, 2.5, 20),infinite),
    sceneHbox(sliderHspacing, sceneButton("", null, difficultySlider.value.asString("Difficulty: %.2f"), 60, 2.5, 20),difficultySlider),
    sceneHbox(sliderHspacing, sceneButton("", null, targetsSlider.value.asString("Number of Targets: %.0f"), 60, 2.5, 20),targetsSlider),
    sceneHbox(sliderHspacing, sceneButton("Enter Player Name:", null,null,60,2.5,20), nameField),
    sceneHbox(sliderHspacing, sceneButton("Enter Maze Name:", null,loadText,60,2.5,20), mazeField)
  )

  def startGame():Unit = {
    if isLoaded then
      WeaveMaze.stage.scene = game.start()
    else
      loadText.value = "No Maze Loaded!"
      val pause = PauseTransition(Duration(1500))
      pause.onFinished = _ => loadText.value = "Enter Maze Name:"
      pause.play()
  }
  val loadButton = sceneButton("Load", loadGame, null, 25, 3, 8)
  val startButton = sceneButton("START", startGame, null, 25, 3, 8)
  val backButton = sceneButton("back", ()=>WeaveMaze.stage.scene = Menu,null, 25, 3, 8)
  
  val finalbox = sceneVbox(slidersSpacing, title, labels, loadButton, startButton, backButton)
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  finalbox.getTransforms().add(scale)
  content = finalbox
  scale.xProperty().bind(this.widthProperty().divide(StandardWidth))
  scale.yProperty().bind(this.heightProperty().divide(StandardHeight))
}


object Done extends Scene (StandardWidth, StandardHeight){
  fill <== UIColors.BackgroundColor
  val title = new Text("") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/25)
    fill <== UIColors.InfoTextColor
  }
  
  def textChange(t:StringProperty, s1:String, s2:String, time:Int = 1500) = {
    t.value = s1
    val pause = PauseTransition(Duration(time))
    pause.onFinished = _ => t.value = s2
    pause.play()
  }

  val screenshotField = TextField()
  screenshotField.promptText = "Enter File Name: Maze.png"
  val mazeField = TextField()
  mazeField.promptText = "Enter Maze Name..."
  val saveMazeText = StringProperty("Save Maze")
  val saveScreenshotText = StringProperty("Save Screenshot")
  val saveScoreText = StringProperty("Save Score")
  
  
  var screenshotFileName:String = null
  def saveScreenshot():Unit = {
    if File("data/images/" + screenshotField.text.value).exists() then {
      if screenshotField.text.value == screenshotFileName then
        try {
          Menu.game.saveScreenshot("data/images/" + screenshotFileName)
          textChange(saveScreenshotText, "Screenshot Saved", "Save Screenshot", 1000)
        }
        catch{case e:Exception => textChange(saveScreenshotText, "Error Saving Image", "Save Screenshot")}
      else
        screenshotFileName = screenshotField.text.value 
        saveScreenshotText.value = "File Exists, Click Again to Confirm"
    } else {
      try {
        Menu.game.saveScreenshot("data/images/" + screenshotField.text.value)
        textChange(saveScreenshotText, "Screenshot Saved", "Save Screenshot", 1000)
      }
      catch{case e:Exception => textChange(saveScreenshotText, "Error Saving Image","Save Screenshot")}
    }
  }
  
  var mazeFileName:String = null
  def saveMaze():Unit = {
    if File("data/mazes/" + mazeField.text.value + ".bin").exists() then {
      if mazeField.text.value == mazeFileName then
        try {
          Menu.game.saveMaze(mazeFileName)
          textChange(saveMazeText, "Maze Saved", "Save Maze", 1000)
        }
        catch{case e:Exception => textChange(saveMazeText, "Error Saving Maze", "Save Maze")}
      else
        mazeFileName = mazeField.text.value 
        saveMazeText.value = "File Exists, Click Again to Confirm"
    } else {
      try {
        Menu.game.saveMaze(mazeField.text.value)
        textChange(saveMazeText, "Maze Saved", "Save Maze", 1000)
      }
      catch{case e:Exception => textChange(saveMazeText, "Error Saving Maze", "Save Maze")}
    }
  }

  def resume():Unit = {
    WeaveMaze.stage.scene = Menu.gameScene
    Menu.game.resume()
  }

  def saveScore():Unit = {
    try {
      Menu.game.saveScore(st)
      textChange(saveScoreText, "Score Saved", "Save Score", 500)
    }
    catch {case e: Exception => textChange(saveScoreText, "Error Occured", "Save Score")}
  }

  def playAgain():Unit = {
    Menu.game.start()
    WeaveMaze.stage.scene = Menu.gameScene
  }

  val slidersSpacing = 30
  val sliderHspacing = 140

  val finalbox = sceneVbox(slidersSpacing)
  val resumeButton = sceneButton("Resume", resume, null, 25, 3, 8)
  val saveScoreButton = sceneButton("Save Score!", saveScore, saveScoreText, 25, 3, 8)
  val playButton = sceneButton("Play it Again!", playAgain, null, 25, 3, 8)
  val quitButton = sceneButton("Main Menu", ()=>WeaveMaze.stage.scene = Menu,null, 25, 3, 8)
  val changeColor = sceneButton("Change Theme", ()=>  UIColors.switchTheme())
  val screenshotButton = sceneHbox(sliderHspacing, sceneButton("", saveScreenshot,saveScreenshotText,60,2.5,20), screenshotField)
  val saveMazeButton = sceneHbox(sliderHspacing, sceneButton("", saveMaze,saveMazeText,60,2.5,20), mazeField)
  
  var score:Int = 0
  
  //should be called whenever switching to this scene
  var st:String = null
  def setDone(state:String = null):Unit = {
    if state == null then {
      st = null
      title.text = f"Weave Maze Paused | Score: $score"
      finalbox.children = Seq(
        title,
        resumeButton,
        changeColor,
        quitButton,
        screenshotButton,
        saveMazeButton,
      )
    } else if state == "infinite" then {
      st = null
      title.text = f"Weave Maze Paused | Score: $score"
      finalbox.children = Seq(
        title,
        resumeButton,
        saveScoreButton,
        changeColor,
        quitButton,
        screenshotButton,
        saveMazeButton,
      )
    } else {
      st = state
      title.text = f"You $state | Score $score"
      finalbox.children = Seq(
        title,
        saveScoreButton,
        playButton,
        quitButton,
        screenshotButton,
        saveMazeButton,
      )
    }
  }

  
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  finalbox.getTransforms().add(scale)
  content = finalbox
  scale.xProperty().bind(this.widthProperty().divide(StandardWidth))
  scale.yProperty().bind(this.heightProperty().divide(StandardHeight))
}


object ScoreBoard extends Scene (StandardWidth, StandardHeight){ 
  fill <== UIColors.BackgroundColor
  
  val title = new Text("Weave Maze | ScoreBoard") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/15)
    fill <== UIColors.InfoTextColor
  }
  
  
  

  var linesSplits = new ObservableBuffer[Seq[String]]()
  def reload():Unit = {
    linesSplits.clear()
    try {
      val lines = scala.io.Source.fromFile("data/scoreboard.txt").getLines().toList
      for line <- lines do 
        val l = line.split(",")
        if l.length == 7 then //valid
          linesSplits += l.toSeq
    } catch{case e:Exception => 
      linesSplits += Seq("---", "No", "Recorded", "Info", "Was", "Found", "---")
    }
  }
  
  val table = new TableView[Seq[String]](linesSplits) {
    columns ++= List(
      new TableColumn[Seq[String], String] {
        text = "Time"
        cellValueFactory = {celld=> StringProperty(celld.value(0))}
      },
      new TableColumn[Seq[String], String] {
        text = "Name"
        cellValueFactory = {celld=> StringProperty(celld.value(1))}
      },
      new TableColumn[Seq[String], String] {
        text = "Score"
        cellValueFactory = {celld=> StringProperty(celld.value(2))}
      },
      new TableColumn[Seq[String], String] {
        text = "Difficulty"
        cellValueFactory = {celld=> StringProperty(celld.value(3))}
      },
      new TableColumn[Seq[String], String] {
        text = "Infinite"
        cellValueFactory = {celld=> StringProperty(celld.value(4))}
      },
      new TableColumn[Seq[String], String] {
        text = "Maze"
        cellValueFactory = {celld=> StringProperty(celld.value(5))}
      },
      new TableColumn[Seq[String], String] {
        text = "State"
        cellValueFactory = {celld=> StringProperty(celld.value(6))}
      },
    )
  }
  
    
  def Reset():Unit = {
    val f = File("data/scoreboard.txt")
    if f.exists() then
      f.delete()
    reload()
  }

  
  val reset = sceneButton("Reset", Reset, null, 30, 7, 15)
  val update = sceneButton("Update", reload, null, 30, 7, 15)
  val back = sceneButton("Back", ()=> WeaveMaze.stage.scene = Menu, null, 30, 7, 15)
  


  
  val finalbox = sceneVbox(10, title, table, sceneHbox(70, update, back, reset))
  
  val scale = scalafx.scene.transform.Scale(1,1,0,0)
  finalbox.getTransforms().add(scale)
  content = finalbox
  scale.xProperty().bind(this.widthProperty().divide(StandardWidth))
  scale.yProperty().bind(this.heightProperty().divide(StandardHeight))
  this.reload()
}


class sceneVbox(space:Double, c:Node*) extends VBox {
  prefWidth = StandardWidth
  prefHeight = StandardHeight
  spacing = space
  padding = Insets(0,0,0,0)
  alignment = Pos.TopCenter
  prefWidth = Region.USE_COMPUTED_SIZE //Limit its width to its components
  children = c
}

class sceneHbox(space:Double, c:Node*) extends HBox {
  prefWidth = StandardWidth
  prefHeight = StandardHeight
  spacing = space
  padding = Insets(0,0,0,0)
  alignment = Pos.TopCenter
  prefHeight = Region.USE_COMPUTED_SIZE
  children = c
}

class sceneButton(txt:String, action:()=>Unit = null, textProp:ObservableValue[String, String] = null, revFontsize:Double = 20, revWidth:Double=3, revHeight:Double=7) extends StackPane { //Font Size decreases as revfontsize increases
  
  val back = new Rectangle {
    fill <== UIColors.InfoBarEdgeColor
    width = StandardWidth/revWidth
    height = StandardHeight/revHeight
    arcHeight = 20 
    arcWidth = 20 
  }
  maxHeight = back.height.value
  maxWidth = back.width.value
  if action != null then {
    onMouseClicked = _=> action()
    onMouseEntered = _=>
      back.width.value *= 1.5 
      back.height.value *= 1.5
    onMouseExited = _=>
      back.width.value /= 1.5
      back.height.value /= 1.5
    cursor = Cursor.Hand
  }

  val textfont = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/revFontsize)
  
  val label = new Label(txt) {
    font = textfont
    textFill <== UIColors.InfoTextColor
  }
  if textProp != null then label.text <== textProp
  this.children = Seq(back, label)
}
