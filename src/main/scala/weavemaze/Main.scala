package weavemaze

import scalafx.application.JFXApp3
import scalafx.stage.Stage
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.application.Platform
import scalafx.beans.property.BooleanProperty
import scalafx.scene.input.MouseEvent
import javafx.scene.input.KeyEvent
import javafx.scene.input.KeyCode
import scalafx.scene.input.MouseButton


object WeaveMaze extends JFXApp3 {
  var mediaPlayer: MediaPlayer = null
  val startNow = BooleanProperty(false)
  

  
  def simulateWarmup():Unit = {
    
    val warmupStage = new Stage {
      scene = MainScene
    }
    val change1 = BooleanProperty(false)
    val change2 = BooleanProperty(false)
    val change3 = BooleanProperty(false)
    val change4 = BooleanProperty(false)
    val change5 = BooleanProperty(false)
    val change6 = BooleanProperty(false)
    val change7 = BooleanProperty(false)
    val change8 = BooleanProperty(false)
    warmupStage.x = -10000 //offscreen
    warmupStage.y = -10000
    warmupStage.show()
    Menu.switch(NewGameScene, 1, ()=>change1.value = true)
    change1.onChange((_,_,_)=> NewGameScene.switch(LoadGameScene,-1, ()=>change2.value = true))
    change2.onChange((_,_,_)=> LoadGameScene.switch(Done, 1, ()=> change3.value = true))
    change3.onChange((_,_,_)=> Done.switch(ScoreBoard, -1, ()=> change4.value = true))
    change4.onChange((_,_,_)=> ScoreBoard.switch(NewGameScene, 1, ()=> change5.value = true))
    change5.onChange((_,_,_)=> NewGameScene.startGame(()=> change6.value = true))
    change6.onChange((_,_,_)=> Done.gameContent.switch(Done, 1, ()=>change7.value = true))
    change7.onChange((_,_,_)=> Done.switch(Menu, -1, ()=>
        warmupStage.close()
        startNow.value = true))
    
  }
  
  
  override def start(): Unit = {
    try {
      mediaPlayer = new MediaPlayer(Media(getClass.getResource("/sounds/background.mp3").toString))
      mediaPlayer.cycleCount = MediaPlayer.Indefinite
    } catch {
      case e:Exception => println("Music Not Loaded")
    }

    simulateWarmup()
    
    startNow.onChange((_,_,_) => Platform.runLater{
      if mediaPlayer != null then mediaPlayer.play()
      this.stage = new JFXApp3.PrimaryStage {
        title = "Weave Maze"
        scene = MainScene
      }

    })
 }
}
