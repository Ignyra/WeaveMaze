package weavemaze

import scalafx.application.JFXApp3
import scalafx.stage.Stage
import scalafx.scene.media.{Media, MediaPlayer}




object WeaveMaze extends JFXApp3 {
  var mediaPlayer: MediaPlayer = null
  override def start(): Unit = {
    try {
      mediaPlayer = new MediaPlayer(Media(getClass.getResource("/sounds/background.mp3").toString))
      mediaPlayer.cycleCount = MediaPlayer.Indefinite
    } catch {
      case e:Exception => println("Music Not Loaded")
    }

    if mediaPlayer != null then mediaPlayer.play()
    stage = new JFXApp3.PrimaryStage {
      title = "Weave Maze"
      scene = Menu
    }
 }
}
