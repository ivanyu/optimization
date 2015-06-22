import scala.io.Source

object GraphColoringApp extends App {
  if (args.length != 1) {
    println("Usage: java GraphColoringApp filename")
    System.exit(0)
  }

  val linesIter = Source.fromFile(args(0)).getLines()
  val firstLineArray = linesIter.next().replaceAll("\\s+", " ").split(" ").map(_.toInt)
  val n = firstLineArray(0)
  val e = firstLineArray(1)

  val graph = Array.ofDim[Int](n, n)
  for { line <- linesIter } {
    val lineArray = line.replaceAll("\\s+", " ").split(" ").map(_.toInt)
    val nodeFrom = lineArray(0)
    val nodeTo = lineArray(1)
    graph(nodeFrom)(nodeTo) = 1
    graph(nodeTo)(nodeFrom) = 1
  }

  val colorer = new Colorer(graph)

  val start = System.currentTimeMillis()
  val coloring = colorer.color()
  val end = System.currentTimeMillis()

  for {
    i <- Range(0, n)
    j <- Range(0, n) if graph(i)(j) == 1
  } {
    if (coloring(i) == coloring(j)) {
      throw new Exception("Fail")
    }
  }

  println(coloring)
  println(end - start)
}
