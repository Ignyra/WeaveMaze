package weavemaze

import java.io.File
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import javafx.application.Platform
import scala.util.Random

class WeaveMazeSpec extends AnyFlatSpec with Matchers {

  "The DisjointSet Data Structure" should "initially have all cells disconnected" in {
    val ds = DisjointSet(2,2)
    val c00 = ds.parents(0)(0)
    val c01 = ds.parents(0)(1)
    ds.isConnected(c00, c01) shouldBe false
  }

  it should "connect cells after union and record that" in {
    val ds = DisjointSet(2,2)
    val c00 = ds.parents(0)(0)
    val c01 = ds.parents(0)(1)
    ds.union(c00, c01)
    ds.isConnected(c00, c01) shouldBe true
  }

  "The Maze class" should "create a single connection in a 1x2 grid" in {
    val m = Maze(1,2,0.0)
    m.initEdgesAndCells()
    m.kruskal(m.edges)
    val c0 = m.grid(0)(0)
    val c1 = m.grid(1)(0)
    (c0.exit & E) should be > 0
    (c1.exit & W) should be > 0
  }

  it should "find the right path of length 1 in a 1x2 grid in both directions" in {
    val m = Maze(1,2,0.0)
    m.initEdgesAndCells()
    m.kruskal(m.edges)
    m.resetPath()
    val source = m.grid(0)(0)
    source._makeSource()
    m.findPath(source)
    val target = m.grid(1)(0)
    
    target.pred shouldEqual source
    target.dist shouldEqual 1

    m.resetPath()
    target._makeSource()
    m.findPath(target)
    source.pred shouldEqual target
    source.dist shouldEqual 1
  }

  it should "save and load a maze preserving dimensions" in {
    val tmp = File.createTempFile("testmaze", "")
    val path = tmp.getAbsolutePath
    val m1 = Maze(MinRows,MinCols,0.2)
    m1.makeNewMaze()
    m1.saveMaze(path)
    val m2 = Maze(0,0,0.0)
    m2.loadMaze(path)
    m2.height shouldEqual MinRows
    m2.width  shouldEqual MinCols
    tmp.delete()
    File(path + ".bin").delete()
  }

  it should "preserve all cell data when saving and loading a maze in various sizes" in {
    val tmp = File.createTempFile("testmaze", "")
    val path = tmp.getAbsolutePath
    val m1 = Maze(Random().between(MinRows, MaxRows),Random().between(MinCols, MaxCols),Random().nextDouble())
    m1.makeNewMaze()
    m1.saveMaze(path)
    val m2 = Maze(0,0,0.0)
    m2.loadMaze(path)
    m2.height shouldEqual m1.height
    m2.width  shouldEqual m1.width
    for (x <- 0 until m1.width; y <- 0 until m1.height) {m2.grid(x)(y).exit shouldEqual m1.grid(x)(y).exit}
    tmp.delete()
    File(path + ".bin").delete()
  }

  it should "generate large mazes within a small time" in {
    val start = System.currentTimeMillis()
    val m = Maze(100,150,0.5)
    m.makeNewMaze()
    val duration = System.currentTimeMillis() - start
    duration should be < 5000L
  }

  "The Player class" should "move east in a 1x2 maze and count moves correctly" in {
    val m = Maze(1,2,0.0)
    m.initEdgesAndCells()
    m.kruskal(m.edges)
    import scala.collection.mutable.ArrayBuffer
    Platform.startup(() => {})
    val tiles = ArrayBuffer.fill(2,1){new cellTile(N|S|E|W, 1.0, 1.0, 0.1, UIColors.MazeTileColor, UIColors.MazeEdgeColor)}
    val player = new Player("test", m.grid(0)(0), tiles, m.grid)
    player.move(E)
    player.cell shouldEqual m.grid(1)(0)
    player.getAndResetMoves shouldEqual 1
  }

  "The ScoreBar class" should "track time, moves, and detect game overs correctly" in {
    val sb = new ScoreBar(1000, 2, 1, false, 50.0, 50.0)
    // No moves yet, so isOver is true
    sb.isOver shouldBe true
    sb.addMoves(3)
    sb.isOver shouldBe false
    sb.addTime(-200)
    sb.getTime shouldEqual 800
    val formatted = sb.formattedTime(65000)
    formatted should include ("Time: 01:05.00")
  }

  "The Game.applyDifficulty function" should "honor userRows and userCols when above the minimums" in {
    val g = new Game("player", 0.4, 3, infinite = false, userRows = 10, userCols = 12)
    g.applyDifficulty()
    g.userRows shouldEqual 10
    g.userCols shouldEqual 12
    g.getRows shouldEqual 10
    g.getCols shouldEqual 12
  }

  it should "load a maze correctly via setAndLoadMaze" in {
    val dir = File("data/mazes")
    dir.mkdirs()
    val tmp = File.createTempFile("testmaze", "", dir)
    val path = tmp.getAbsolutePath
    val m1 = Maze(6,7,0.25)
    m1.makeNewMaze()
    m1.saveMaze(path)

    val filename = File(path).getName

    val g = new Game("p", 0.5, 3, infinite = false, userRows = 0, userCols = 0)
    g.setAndLoadMaze(filename)
    g.getMazeName shouldEqual filename
    g.getRows shouldEqual 6
    g.getCols shouldEqual 7
    
    tmp.delete()
    File("data/mazes/" + filename + ".bin").delete()
  }
  
  it should "throw an error when loading a non-existent maze file" in {
    val g = new Game("p", 0.5, 1, infinite = false, userRows = 0, userCols = 0)
    intercept[java.io.FileNotFoundException] {
      g.setAndLoadMaze("NonExistentFile.abcdefghijklmnopqrstuvwsyz")
    }
  }
  
  it should "throw an error on corrupted maze files" in {
    val name = File.createTempFile("corruptmaze", "").getAbsolutePath
    val f = new File(name + ".bin")
    f.getParentFile.mkdirs()
    val w = new java.io.PrintWriter(f)
    w.write("Not a valid maze binary file......")
    w.close()

    val m = new Maze(0,0,0.0)
    intercept[Exception] {
      m.loadMaze(name)
    }
    f.delete()
  }

  it should "save scores to the scoreboard file correctly" in {
    val tmp = File.createTempFile("scoreboard", ".txt").getAbsolutePath
    val g = new Game("tester", 0.2, 1, infinite = false, userRows = 5, userCols = 5)
    g.start()
    g.saveScore("TEST_STATE", filePath = tmp)
    val lines = scala.io.Source.fromFile(tmp).getLines().toList
    lines should not be empty
    lines.last should include (", tester, 0, 0.2, false, null, TEST_STATE")
    new File(tmp).delete()
  } 
  
  it should "determine correct direction in the SolutionRenderer class" in {
    val m = Maze(MinRows, MinCols, 0)
    m.initEdgesAndCells()
    m.makeNewMaze()
    val mr = Maze_Renderer(m)
    mr.generateMazeGui()
    val sr = Solution_Renderer(mr.tiles, mr.mazeWidth, mr.mazeHeight)
    val c1 = Cell(0,0,0)
    val c2 = Cell(1,0,0)
    sr.findDir(c1, c2) shouldEqual E
    sr.findDir(c2, c1) shouldEqual W
  }

}

