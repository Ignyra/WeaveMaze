package weavemaze

import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import scalafx.scene.text.Text
import scalafx.scene.text.{Text, Font}
import scalafx.beans.property.{StringProperty, IntegerProperty, DoubleProperty}
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



import scalafx.scene.transform.Scale
import scalafx.beans.property.BooleanProperty
import scalafx.animation.RotateTransition
import scalafx.util.Duration
import scalafx.animation.Interpolator
import scalafx.animation.TranslateTransition

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.compiletime.ops.double
import scalafx.collections.ObservableBuffer
import scalafx.application.Platform

import scalafx.scene.layout.{Background, BackgroundFill}


object MainScene extends Scene (StandardWidth, StandardHeight){
  fill <== UIColors.BackgroundColor
  this.content = Menu
}

object Menu extends sceneGeneral{

  val title = new Text("Weave Maze") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/15)
    fill <== UIColors.InfoTextColor
  }
  val NewGame = sceneButton("New Game", ()=> this.switch(NewGameScene, 1) )
  val LoadGame = sceneButton("Load Game", ()=> this.switch(LoadGameScene, 1) )
  val scoreBoard = sceneButton("ScoreBoard", ()=> this.switch(ScoreBoard, -1) )
  val ChangeColor = sceneButton("Change Theme", ()=>  UIColors.switchTheme())
  val buttonsbox = sceneVbox(30, sceneHbox(20, NewGame, LoadGame), scoreBoard, ChangeColor)
  val finalbox:Node = sceneVbox(100, title, buttonsbox)
  this.set(finalbox)
}


object NewGameScene extends sceneGeneral{
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
    min = 0.001
    max = 0.999
    value = 0.5
    blockIncrement = 0.01
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

  def startGame(act:()=>Unit = ()=>{}):Unit = {
    val game = Game(nameField.text.value, difficultySlider.value.value.toDouble,targetsSlider.value.value.toInt,infinite.selected.value, rowBindLabel.value.intValue(), colBindLabel.value.intValue())
    this.switch(game.start(),1,act)
  }

  val startButton = sceneButton("START", ()=>startGame(), null, 25, 3, 8)
  val backButton = sceneButton("back", ()=>this.switch(Menu, -1),null, 25, 3, 8)

  val finalbox = sceneVbox(slidersSpacing, title, labels, startButton, backButton)
  this.set(finalbox)

}


object LoadGameScene extends sceneGeneral{
  val title = new Text("Weave Maze") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/15)
    fill <== UIColors.InfoTextColor
  }
  val infinite = new CheckBox("")
  val difficultySlider = new Slider {
    min = 0.001
    max = 0.999
    value = 0.5
    blockIncrement = 0.01
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
      this.switch(game.start(), 1)
    else
      loadText.value = "No Maze Loaded!"
      val pause = PauseTransition(Duration(1500))
      pause.onFinished = _ => loadText.value = "Enter Maze Name:"
      pause.play()
  }
  val loadButton = sceneButton("Load", loadGame, null, 25, 3, 8)
  val startButton = sceneButton("START", startGame, null, 25, 3, 8)
  val backButton = sceneButton("back", ()=>this.switch(Menu, -1),null, 25, 3, 8)
  
  val finalbox = sceneVbox(slidersSpacing, title, labels, loadButton, startButton, backButton)
  this.set(finalbox)
}


object Done extends sceneGeneral{
  val title = new Text("") {
    font = Font.loadFont(this.getClass.getResourceAsStream(FontType), StandardHeight/25)
    fill <== UIColors.InfoTextColor
  }
  
  var gameContent:sceneGeneral = null
  var game:Game = null


  def textChange(t:StringProperty, s1:String, s2:String, time:Int = 1500) = {
    t.value = s1
    val pause = PauseTransition(Duration(time))
    pause.onFinished = _ => t.value = s2
    pause.play()
  }

  val screenshotField = TextField()
  screenshotField.promptText = "Maze1.png"
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
          this.game.saveScreenshot("data/images/" + screenshotFileName)
          textChange(saveScreenshotText, "Screenshot Saved", "Save Screenshot", 1000)
        }
        catch{case e:Exception => textChange(saveScreenshotText, "Error Saving Image", "Save Screenshot")}
      else
        screenshotFileName = screenshotField.text.value 
        saveScreenshotText.value = "File Exists, Click Again to Confirm"
    } else {
      try {
        this.game.saveScreenshot("data/images/" + screenshotField.text.value)
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
          this.game.saveMaze(mazeFileName)
          textChange(saveMazeText, "Maze Saved", "Save Maze", 1000)
        }
        catch{case e:Exception => textChange(saveMazeText, "Error Saving Maze", "Save Maze")}
      else
        mazeFileName = mazeField.text.value 
        saveMazeText.value = "File Exists, Click Again to Confirm"
    } else {
      try {
        this.game.saveMaze(mazeField.text.value)
        textChange(saveMazeText, "Maze Saved", "Save Maze", 1000)
      }
      catch{case e:Exception => textChange(saveMazeText, "Error Saving Maze", "Save Maze")}
    }
  }

  def resume():Unit = {
    this.switch(this.gameContent, -1)
    this.game.resume()
  }

  def saveScore():Unit = {
    try {
      this.game.saveScore(st)
      textChange(saveScoreText, "Score Saved", "Save Score", 500)
    }
    catch {case e: Exception => textChange(saveScoreText, "Error Occured", "Save Score")}
  }

  def playAgain():Unit = {
    this.game.replay()
    this.switch(this.gameContent, -1)
  }

  val slidersSpacing = 30
  val sliderHspacing = 140

  val finalbox = sceneVbox(slidersSpacing)
  val resumeButton = sceneButton("Resume", resume, null, 25, 3, 8)
  val saveScoreButton = sceneButton("Save Score!", saveScore, saveScoreText, 25, 3, 8)
  val playButton = sceneButton("Play it Again!", playAgain, null, 25, 3, 8)
  val quitButton = sceneButton("Main Menu", ()=>this.switch(Menu, 1),null, 25, 3, 8)
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

  this.set(finalbox)
}


object ScoreBoard extends sceneGeneral{ 
  
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
        val l = line.split(",").map(_.trim)
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

      //https://github.com/scalafx/scalafx/issues/243 implicit conversion from Int and double properties to ObservableValue is missing, so we can do it here
      new TableColumn[Seq[String], Double] {
        text = "Score"
        cellValueFactory = {celld=> DoubleProperty(try{celld.value(2).toDouble}catch{case e: Exception => Double.NaN}).asInstanceOf[ObservableValue[Double, Double]]}
      },
      new TableColumn[Seq[String], Double] {
        text = "Difficulty"
        cellValueFactory = {celld=> DoubleProperty(try{celld.value(3).toDouble}catch{case e: Exception => Double.NaN}).asInstanceOf[ObservableValue[Double, Double]]}
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

  
  val resetHistory = sceneButton("Reset", Reset, null, 30, 7, 15)
  val update = sceneButton("Update", reload, null, 30, 7, 15)
  val back = sceneButton("Back", ()=> this.switch(Menu, 1), null, 30, 7, 15)
  

  
  val finalbox = sceneVbox(10, title, table, sceneHbox(70, update, back, resetHistory))
  
  this.set(finalbox)
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
  if action != null && SMOOTHTRANSITIONS then {
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


class sceneGeneral(m:Node = null) extends Pane {
  var main:Node = null //set for each sceneGeneral init if no m
  var scale:Scale = null
  if m != null then
    main = m
    this.children.setAll(main)

    
  def set(n:Node):Unit = {
    main = n
    scale = Scale(1,1,0,0)
    this.main.getTransforms().add(scale)
    scale.xProperty().bind(MainScene.widthProperty().divide(StandardWidth))
    scale.yProperty().bind(MainScene.heightProperty().divide(StandardHeight))
    this.children.setAll(main)
  }
  
  val timeFactor = 270
  val dir = IntegerProperty(1)
  //same transition from player class
  val bounce = new TranslateTransition(Duration(timeFactor), this) {
    autoReverse = false
    cycleCount = 0
    interpolator = Interpolator.EaseOut
    toX <== -MainScene.width*dir
  }
  
  var lastBounce = bounce
  
  def bounces(n:Int):Unit = {
    require(n%2==1)//must be odd
    var prevBounce = bounce
    var t:Double = timeFactor - timeFactor * 2/n
    var x = 1.35 - 0.3*2/n
    for i <- 1 until n do 
      val newB = new TranslateTransition(Duration(t), this) {
        autoReverse = false
        cycleCount = 0
        interpolator = Interpolator.EaseOut
        if i%2 == 0 then 
          t-=timeFactor*1.6/n
          x-= 0.3 * 2/n
          toX <== -MainScene.width * dir 
        else 
          toX <== -MainScene.width/(x) * dir //initial = 2, final = 1
      }
      prevBounce.setOnFinished(_ => newB.play())
      prevBounce = newB
    lastBounce = prevBounce
  }
  bounces(5)
  
  var isSwitching = false 
  
  def reset():Unit = {
    this.main.translateX = 0
    this.main.layoutX = 0
    this.translateX = 0 
    this.layoutX = 0
    this.children.setAll(this.main)
  }
  
  def switch(other:sceneGeneral, dir:Int = 1, onFinished:()=>Unit = ()=>{}):Unit = {
    this.dir.value = dir
    require(dir == 1 || dir  == -1)
    //dir should either be -1 or 1, 1 is for left traslation and -1 is for right traslation transition
    
    if isSwitching then return
      // wait till the any other switching is done
    
    if SMOOTHTRANSITIONS then
      isSwitching = true
      other.isSwitching = true

      other.main.layoutX = MainScene.width()* dir //this.scaleX()*StandardWidth * dir //this is value that gets applied on each render/ should reset to 0 after for the next render.
    
      this.children.setAll(main, other.main) //shoud be main not other since other has a scale on it

      
      this.bounce.play()
      this.lastBounce.setOnFinished(_ => {
        
        other.reset()
        MainScene.content = other
        this.lastBounce.onFinished = null
        this.reset() //after switching
        isSwitching = false
        other.isSwitching = false
        onFinished()
      })
    else
      MainScene.content = other
  
  }
  
  
}


