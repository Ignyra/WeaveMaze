package weavemaze


import scala.util.Random
import scala.collection.mutable.ArrayBuffer

//refs:
//https://weblog.jamisbuck.org/2011/3/17/maze-generation-more-weave-mazes.html
//https://github.com/daleobrien/maze/blob/master/maze/maze.py
//https://www.geeksforgeeks.org/kruskals-minimum-spanning-tree-algorithm-greedy-algo-2/
//https://weblog.jamisbuck.org/2011/1/3/maze-generation-kruskal-s-algorithm
//https://docs.oracle.com/javase/8/docs/api/java/io/DataOutputStream.html

import java.io.FileOutputStream
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.DataInputStream

//Using bit representaions for efficincy
import java.lang.Integer

val N: Int = Integer.parseInt("000001", 2)
val S: Int = Integer.parseInt("000010", 2)
val E: Int = Integer.parseInt("000100", 2)
val W: Int = Integer.parseInt("001000", 2)
val B: Int = Integer.parseInt("010000", 2) //is a bridge
val T: Int = Integer.parseInt("100000", 2) //same cell but tunnel, made just for tracking the path and the player. need two cells for bridges, one for walking on them, and one for under them
val OPPOSITE: Map[Int, Int] = Map(N->S, S->N, E->W, W->E)

//change in x and y depedning on the direction from the current cell
val DX : Map[Int, Int] = Map(N->  0, S->  0, E-> +1, W-> -1)
val DY : Map[Int, Int] = Map(N-> -1, S-> +1, E->  0, W->  0)

val DIRECTIONS = Seq(N, S, W, E)

// Therfore 0 means a cell with no north, south, east, west exits and one without a tunnel under it.


class Cell(val x: Int, val y: Int, var exit:Int = 0, var pred: Cell = null, var tunnel:Cell = null, var dist:Int = 0) {
  override def toString = s"$x , $y"
  
  def _makeSource() = 
    this.pred = this

  def _resetPath() = 
    pred = null
    dist = 0
    if tunnel != null then 
      tunnel.pred = null
      tunnel.dist = 0
}

class DisjointSet(val h: Int, val w: Int) {
  
  //val parents:ArrayBuffer[ArrayBuffer[Cell]] = ArrayBuffer.fill(w)(ArrayBuffer())
  
  val parents:ArrayBuffer[ArrayBuffer[Cell]] = ArrayBuffer.fill(w, h)(null)
  

  for x <- 0 until w do 
    for y <- 0 until h do 
      parents(x)(y) = Cell(x, y, 0)
  

  val ranks = ArrayBuffer.fill(w)(ArrayBuffer.fill(h)(1))
  
 // def find(c: Cell): Cell = {
 //   val parent = this.parents(c.x)(c.y)
 //   if (parent.x != c.x || parent.y != c.y) {
 //     this.parents(c.x)(c.y) = find(parent) // Path compression
 //   }
 //   this.parents(c.x)(c.y)
 // }
  
  def find(c:Cell):Cell = 
    if this.parents(c.x)(c.y) != c then 
      this.parents(c.x)(c.y) = this.find(this.parents(c.x)(c.y))
    this.parents(c.x)(c.y)

  
  def isConnected(c1:Cell, c2:Cell)=
    this.find(c1) == this.find(c2)

  def union(c1:Cell, c2:Cell) =

    val p1 = this.find(c1)
    val p2 = this.find(c2)

    if p1!=p2 then
      if this.ranks(p1.x)(p1.y) < this.ranks(p2.x)(p2.y) then
        this.parents(p1.x)(p1.y) = p2

      else if this.ranks(p1.x)(p1.y) > this.ranks(p2.x)(p2.y) then
        this.parents(p2.x)(p2.y) = p1
        
      else 
        this.parents(p2.x)(p2.y) = p1
        this.ranks(p1.x)(p1.y) +=1


}




class Maze(var height:Int, var width: Int, var bridgeDensity:Double) {
  
  var dset:DisjointSet = null
  var grid:ArrayBuffer[ArrayBuffer[Cell]] = null
  var edges:ArrayBuffer[(Cell, Cell)] = ArrayBuffer()

  def initEdgesAndCells():Unit =
    dset = DisjointSet(height, width)
    this.grid = dset.parents.map(_.clone()) 
    for x <- 0 until width do 
      for y <- 0 until height do 
        if x < width - 1 then edges += ((grid(x)(y), grid(x+1)(y))) //every horizontal edge except grid(width-1)(:) since it is the last column

        if y < height-1 then edges += ((grid(x)(y), grid(x)(y+1))) //every vertical edge except grid(:)(height-1) since it is the last row
    edges = Random.shuffle(edges) // for a different maze
 
 

  def addBridges(density:Double): Unit = 
    //ignore first and last columns and rows since bridges can't form there.
    for cx <- 1 until width-1 do 
      for cy <- 1 until height-1 do 
        
        //add bridge based on probabilty and validity
        if Random().nextDouble() < density then
          //neighbors
          val (nx, ny) = (cx, cy - 1) 
          val (wx, wy) = (cx - 1, cy)
          val (ex, ey) = (cx + 1, cy)
          val (sx, sy) = (cx, cy + 1)
          
          //respective cells
          val (c, nc, wc, ec, sc) = (grid(cx)(cy), grid(nx)(ny), grid(wx)(wy), grid(ex)(ey), grid(sx)(sy))
          
          
          //create if validity checks
          //
          if c.exit == 0 && !dset.isConnected(nc, sc) && !dset.isConnected(ec, wc) then
            
            
            //connect the opposite cells to create a straight perpendicluar cell
            dset.union(nc, sc)
            dset.union(ec, wc)
            
            //choose which straight cell connects to the current cell and becomes a bridge, more randomness
            if Random().nextInt(2) == 1 then
              grid(cx)(cy).exit = B | W | E
              grid(cx)(cy).tunnel = Cell(cx, cy, T | N | S) //it has a tunnel under it going in the opposte directions
            else
              grid(cx)(cy).exit = B | N | S
              grid(cx)(cy).tunnel = Cell(cx, cy, T | W | E)
            //we should probably only do this if the concrned cell isn't a bridge, other wise, we cover what the path under it by connecting a path. 
            grid(nx)(ny).exit |= S
            grid(wx)(wy).exit |= E
            grid(ex)(ey).exit |= W
            grid(sx)(sy).exit |= N
            
            //removing current edges connecting to our node as it can't be connected to anything anymore
            //edges.filterInPlace((c1,c2) => c1 != c && c2 != c)
            edges = edges.filterNot { case (c1, c2) => c1 == c || c2 == c }

  def kruskal(edges: ArrayBuffer[(Cell, Cell)]) = 
    
    while edges.nonEmpty do 
      val (c1,c2) = edges.remove(0)

      if !dset.isConnected(c1,c2) then
        dset.union(c1,c2)
        
        //record path
        //c2.pred = c1

        //c2 is east of c1
        if c1.x + 1 == c2.x then
          c1.exit |= E
          c2.exit |= W
        //c2 is south of c1
        else if c1.y + 1 == c2.y then
          c1.exit |= S
          c2.exit |= N 
        else
          throw Exception("Edges list is compromised")

  //finding a solution using DFS
  
  def resetPath() = 
    this.grid.map(_.map(_._resetPath()))


  // treating pred = null as "unvisted" and trating source.pred = source as if it is visted and it can't the pred of something else
  def findPath(source:Cell):Unit =
    for dir <- DIRECTIONS do 
      //connect a tile with its exit tiles only if not visited
      if (source.exit & dir) == dir then
        var next = this.grid(source.x + DX(dir))(source.y + DY(dir))
        
        //if the next is a bridge that is perpendicular/blocked from that direction (i.e. the current cell has a path under it), then 
        //we point next to the tunnel with its different exits so depth first search works on it
        //instead of the birdge forming a conection via pred where a path doesn't exist 
        if ((next.exit & B) == B) && ((next.exit & OPPOSITE(dir)) == 0) then
          next  = next.tunnel

        if next.pred == null then
          next.pred = source
          next.dist = source.dist + 1 //store distance
          findPath(next)

  def makeNewMaze():Unit = {
    this.initEdgesAndCells()
    this.addBridges(bridgeDensity)
    this.kruskal(this.edges)
  }

  def saveMaze(name:String):Unit = {
    val file = new DataOutputStream(new FileOutputStream(name + ".bin"))
    //2 bytes for dimension, maximum width and height is 2^16
    file.writeShort(this.height) //rows
    file.writeShort(this.width) //cols

    for (col <- grid; cell <- col) {
      file.writeByte(cell.exit)
    }
    file.close()
  }

  def loadMaze(name:String):Unit = {
    val file = new DataInputStream(new FileInputStream(name + ".bin"))
    this.height = file.readUnsignedShort()
    this.width = file.readUnsignedShort()

    this.grid = ArrayBuffer.fill[Cell](this.width, this.height)(null)

    var bridgesCount = this.width * this.height
    for (x <- 0 until width; y <- 0 until height) {
      val e = file.readByte()
      this.grid(x)(y) = Cell(x, y, e)
      if (e == (B | W | E)) then {    
        this.grid(x)(y).tunnel = Cell(x,y, T | N | S )
      } else if (e == (B | N | S)) then {    
        this.grid(x)(y).tunnel = Cell(x,y, T | W | E )
      } else {
        bridgesCount -=1
      }
    }

    this.bridgeDensity = bridgesCount/(this.width * this.height)

    assert(grid.forall(_!=null), "Corrupted File, Missing Info")
    file.close()
    
  }

}
