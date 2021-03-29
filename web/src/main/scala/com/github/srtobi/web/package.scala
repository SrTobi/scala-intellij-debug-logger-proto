package com.github.srtobi

import org.lrng.binding.html.NodeBinding

import scala.language.implicitConversions
import scala.xml.Elem

package object web {
  val htmlArrowRight = "\u2192"

  implicit def makeIntellijHappy[T](x: Elem): NodeBinding[T] = ???
  //implicit def makeIntellijHappy[T <: HTMLElement](x: scala.xml.Elem): Binding[T] = ???

  implicit class BooleanExt(private val bool: Boolean) extends AnyVal {
    def pen(string: String): String =
      if (bool) string else ""
  }

  def colorInInfiniteColorSpectrum(index: Int): String = {
    assert(index >= 0)
    val colorTargets = Seq(
      Seq(0.6, 0.6, 1.0),
      Seq(0.0, 1.0, 0.0),
      Seq(1.0, 1.0, 0.0),
      Seq(1.0, 0.0, 0.0),
      Seq(0.0, 1.0, 1.0),
      //Seq(1.0, 1.0, 1.0),
    )

    val phaseSize = 6
    val limes = (colorTargets.size - 1).toDouble / 2.0

    val phaseIndex = index / phaseSize
    val phaseProgress = (index.toDouble / phaseSize) - phaseIndex
    //val target = limes * indexD / (halfTime + indexD)

    val lowerIdx = phaseIndex
    val upperIdx = phaseIndex + 1

    val lowerColor = colorTargets(lowerIdx % colorTargets.size)
    val upperColor = colorTargets(upperIdx % colorTargets.size)

    val lowerShare = 1.0 - phaseProgress
    val upperShare = 1.0 - lowerShare

    val lowerShareColor = lowerColor.map(_ * lowerShare)
    val upperShareColor = upperColor.map(_ * upperShare)

    val result = lowerShareColor.zip(upperShareColor)
      .map { case (a, b) => a + b  }
      .map(_ * 255)
      .map(_.toInt)
    result.mkString("rgb(", ", ", ")")
  }
}
