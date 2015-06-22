import scala.annotation.tailrec
import scala.collection.mutable

class Colorer(graph: Array[Array[Int]]) {
  private val NOT_COLORED = -1

  private type Color = Int
  private type Coloring = Seq[Color]

  assert(graph.length == graph(0).length)

  private val n = graph.length

  private val originalNodesWithNeighboursNotSorted = graph.zipWithIndex
    .map(x => NodeWithNeighbours(x._2, x._1.zipWithIndex.filter(_._1 == 1).map(_._2).toSet))

  private val originalNodesWithNeighbours = graph.zipWithIndex
    .map(x => NodeWithNeighbours(x._2, x._1.zipWithIndex.filter(_._1 == 1).map(_._2).toSet))
    .sortBy(- _.neighbours.size).toList

  // NOT FINISHED YET

  def color(): Coloring = {
    // TODO lower bound for chromatic number (clique - ?)
    // TODO connected components

    @tailrec
    def color0(k: Int): Option[Coloring] = {
      if (k < 1000) {
        val coloring = tryColor(k)
        if (coloring.isDefined) {
          coloring
        } else {
          color0(k + 1)
        }
      } else {
        System.err.println("k = 1000")
        None
      }
    }

    color0(1).get
  }


//  def findCliques(): Unit = {
//    var cliques = List[Set[Int]]()
//
//    val compsub: mutable.Set[Int] = mutable.Set.empty
//    def findCliques0(candidates: mutable.Set[Int], not: mutable.Set[Int]): Unit = {
//      while (candidates.nonEmpty && !not.exists { i => candidates == originalNodesWithNeighboursNotSorted(i).neighbours }) {
//        val v = candidates.head
//        candidates.remove(v)
//        compsub.add(v)
//        val newCandidates = candidates.filter(i => originalNodesWithNeighboursNotSorted(i).neighbours.contains(v))
//        val newNot = not.filter(i => originalNodesWithNeighboursNotSorted(i).neighbours.contains(v))
//        if (newCandidates.isEmpty && newNot.isEmpty) {
//          cliques = compsub.toSet :: cliques
//        } else {
//          findCliques0(newCandidates, newNot)
//        }
//
//        compsub.remove(v)
//        candidates.remove(v)
//        not.add(v)
//      }
//    }
//    findCliques0(mutable.Set(Range(0, n):_*), mutable.Set.empty)
//    cliques.map(_.toList.sorted).sortBy(_.size)
//    cliques.foreach(println)
//  }

  /**
   * @param k chromatic number
   */
  private def tryColor(k: Int): Option[Coloring] = {

    println(s"Tryign $k color(s)")

    def tryColor0(nodeByNeighbourCount: List[NodeWithNeighbours],
                  currentColoring: Coloring,
                  availableColors: Seq[Set[Color]]): Option[Coloring] = {
      nodeByNeighbourCount match {
        case h :: t if availableColors(h.nodeIdx).nonEmpty =>
//          println(s"Coloring node ${h.nodeIdx}")

          val availableColorsForCurrentNode = availableColors(h.nodeIdx)
          // Sort available colors by restricting force.
          val availableColorsByRestrictingForce = availableColorsForCurrentNode.toList.map { color =>
            (color, h.neighbours.flatMap(availableColors).count(_ == color))
          }.sortBy(_._2).map(_._1)

          @tailrec
          def iterate(colors: List[Color]): Option[Coloring] = colors match {
            case color :: rest =>
//              println(s"Trying color $color on node ${h.nodeIdx}")
              val newAvailableColors = availableColors.zipWithIndex.map {x =>
                if (h.neighbours.contains(x._2) || x._2 == h.nodeIdx) {
                  x._1.filter(_ != color)
                } else {
                  x._1
                }
              }
              if (newAvailableColors.exists(_.isEmpty)) {
                iterate(rest)
              } else {
                val updatedColoring = currentColoring.updated(h.nodeIdx, color)
                val next = tryColor0(t, updatedColoring, newAvailableColors)
                if (next.isDefined) {
                  next
                } else {
                  iterate(rest)
                }
              }


            case _ => None
          }
          iterate(availableColorsByRestrictingForce)

        case Nil if !currentColoring.contains(NOT_COLORED) =>
          Some(currentColoring)

        case _ =>
          None
      }
    }

    def tryColor1(currentColoring: Coloring,
                  availableColors: Seq[Set[Color]]): Option[Coloring] = {
      val notColoredNodes = currentColoring.zipWithIndex
        .filter(_._1 == NOT_COLORED).map(_._2)
      val canColorFurther = !notColoredNodes.map(availableColors).exists(_.isEmpty)

      if (notColoredNodes.nonEmpty && canColorFurther) {
        // Sort not colored nodes first by the count of available colors
        // then by the count of neighbours.
        val currentNode = notColoredNodes
          .sortBy { i =>
            val availableColorsCount = availableColors(i).size
            val neighboursCount = originalNodesWithNeighboursNotSorted(i).neighbours.count(neigh => currentColoring(neigh) == NOT_COLORED)
            (availableColorsCount, -neighboursCount)
          }
          .head

        val neighbours = originalNodesWithNeighboursNotSorted(currentNode).neighbours
        // Sort available colors by restricting force.
        val availableColorsByRestrictingForce = availableColors(currentNode).toList.map { color =>
          (color, neighbours.flatMap(availableColors).count(_ == color))
        }.sortBy(_._2).map(_._1)

        @tailrec
        def iterate(colors: List[Color]): Option[Coloring] = colors match {
          case color :: rest =>
            // println(s"Trying color $color on node ${h.nodeIdx}")

            val updatedColoring = currentColoring.updated(currentNode, color)

            var newAvailableColors = availableColors.updated(currentNode, Set(color))
            newAvailableColors = arcConsistency(updatedColoring, currentNode, newAvailableColors)

            val next = tryColor1(updatedColoring, newAvailableColors)
            if (next.isDefined) {
              next
            } else {
              iterate(rest)
            }

          case _ => None
        }
        iterate(availableColorsByRestrictingForce)
      } else if (notColoredNodes.isEmpty) {
        // Already colored.
        Some(currentColoring)
      } else {
        None
      }
    }

    def tryColor2(currentColoring: Coloring,
                  availableColors: Seq[Set[Color]],
                  front: Set[Int]): Option[Coloring] = {
      val notColoredNodes = currentColoring.zipWithIndex
        .filter(_._1 == NOT_COLORED).map(_._2)
      val canColorFurther = !notColoredNodes.map(availableColors).exists(_.isEmpty)

      if (notColoredNodes.nonEmpty && canColorFurther) {
        // Sort not colored nodes first by the count of available colors
        // then by the count of neighbours.
        val currentNode = front.toList
          .sortBy { i =>
          val availableColorsCount = availableColors(i).size
          val neighboursCount = originalNodesWithNeighboursNotSorted(i).neighbours.count(neigh => currentColoring(neigh) == NOT_COLORED)
          (availableColorsCount, -neighboursCount)
        }
          .head

        val neighbours = originalNodesWithNeighboursNotSorted(currentNode).neighbours

        var updatedFront = front ++ notColoredNodes.toSet.intersect(neighbours) - currentNode
        if (updatedFront.isEmpty) {
          updatedFront = Set(originalNodesWithNeighbours.collectFirst {
            case NodeWithNeighbours(nodeIdx, _) if notColoredNodes.contains(nodeIdx) => nodeIdx
          }.get)
        }

        // Sort available colors by restricting force.
        val availableColorsByRestrictingForce = availableColors(currentNode).toList.map { color =>
          (color, neighbours.flatMap(availableColors).count(_ == color))
        }.sortBy(_._2).map(_._1)

        @tailrec
        def iterate(colors: List[Color]): Option[Coloring] = colors match {
          case color :: rest =>
            // println(s"Trying color $color on node ${h.nodeIdx}")
            val updatedColoring = currentColoring.updated(currentNode, color)

            var newAvailableColors = availableColors.updated(currentNode, Set(color))
            newAvailableColors = arcConsistency(updatedColoring, currentNode, newAvailableColors)
            val next = tryColor2(updatedColoring, newAvailableColors, updatedFront)
            if (next.isDefined) {
              next
            } else {
              iterate(rest)
            }

          case _ => None
        }
        iterate(availableColorsByRestrictingForce)
      } else if (notColoredNodes.isEmpty) {
        // Already colored.
        Some(currentColoring)
      } else {
        None
      }
    }

    def arcConsistency(coloring: Coloring,
                       propagateFromNode: Int,
                       availableColors: Seq[Set[Color]]): Seq[Set[Color]] = {
      var newAvailableColors = availableColors
      var neighboursToPropagate = Set.empty[Int]
      for (neighbour <- originalNodesWithNeighboursNotSorted(propagateFromNode).neighbours
           if neighbour != propagateFromNode && coloring(neighbour) == NOT_COLORED) {
        // Find impossible neighbour colors.
        // Each color must have a feasible pair in `propagateFromNode`.
        val failColors = newAvailableColors(neighbour).filter(c => !newAvailableColors(propagateFromNode).exists(_ != c))
        if (failColors.nonEmpty) {
          neighboursToPropagate = neighboursToPropagate + neighbour
          newAvailableColors = newAvailableColors.updated(neighbour, newAvailableColors(neighbour) -- failColors)

          newAvailableColors = arcConsistency(coloring, neighbour, newAvailableColors)
        }
      }
      newAvailableColors
    }

    val availableColors: Seq[Set[Color]] = Seq.fill(n)(Range(0, k).toSet)
    val coloring = Seq.fill(n)(NOT_COLORED)
//    tryColor0(originalNodesWithNeighbours, coloring, availableColors)
    tryColor1(coloring, availableColors)
//    tryColor2(coloring, availableColors, Set(originalNodesWithNeighbours.head.nodeIdx))
  }
}

private case class NodeWithNeighbours(nodeIdx: Int,
                                          neighbours: Set[Int])
