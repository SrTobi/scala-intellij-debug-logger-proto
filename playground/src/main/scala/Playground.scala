package com.github.srtobi

import sourcecode.{Args, Enclosing}

object Playground {
  def test(x: Int)(implicit encl: Args): Int = DebugLogger.func {
    var blub = x
    DebugLogger.log("before adding 5", blub)
    blub += 5
    DebugLogger.log("after adding 5", blub)

    if (x <= 0) blub
    else blub + test(x - 5)
  }

  def main(args: Array[String]): Unit = {
    println(test(5))
  }
}
