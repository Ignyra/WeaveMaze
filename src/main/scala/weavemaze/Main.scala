package weavemaze

import scalafx.application.JFXApp3


//object WeaveMaze extends JFXApp3 {
//  override def start(): Unit = {
//    val game = Game("Player X", difficulty = 0.5,infinite = true, userRows = 15, userCols = 30)
//    stage = new JFXApp3.PrimaryStage {
//      title = "Weave Maze"
//      scene = game.start() 
//    }
// }
//}


object WeaveMaze extends JFXApp3 {
  override def start(): Unit = {
    val game = Game("Player X", difficulty = 0.5,infinite = true, userRows = 15, userCols = 30)
    game.setAndLoadMaze("Maze")
    stage = new JFXApp3.PrimaryStage {
      title = "Weave Maze"
      scene = game.start() 
    }
 }
}

//object WeaveMaze extends JFXApp3 {
//  override def start(): Unit = {
//    stage = new JFXApp3.PrimaryStage {
//      title = "Weave Maze"
//      scene = Menu 
//    }
// }
//}
