package com.github.srtobi

import upickle.default.{ReadWriter => RW, _}

final case class DebugLogMsg(kind: LogKind, stackTrace: Seq[StackTraceEntry])

object DebugLogMsg {
  implicit val rw: RW[DebugLogMsg] = macroRW
}

final case class StackTraceEntry(method: String, className: String, line: Int)

object StackTraceEntry {
  implicit val rw: RW[StackTraceEntry] = macroRW
}

sealed trait LogKind
object LogKind {
  implicit val rw: RW[LogKind] = macroRW

  @upickle.implicits.key("msg")
  final case class Msg(msg: String,
                       variables: Seq[(String, String)]) extends LogKind

  @upickle.implicits.key("func-start")
  final case class FunctionStart(args: Seq[(String, String)]) extends LogKind

  @upickle.implicits.key("func-end")
  final case class FunctionEnd(result: FunctionResult) extends LogKind


  object Msg {
    implicit val rw: RW[Msg] = macroRW
  }

  object FunctionStart {
    implicit val rw: RW[FunctionStart] = macroRW
  }

  object FunctionEnd {
    implicit val rw: RW[FunctionEnd] = macroRW
  }
}



sealed trait FunctionResult

object FunctionResult {
  implicit val rw: RW[FunctionResult] = macroRW

  @upickle.implicits.key("succ")
  final case class Succeed(result: String) extends FunctionResult
  @upickle.implicits.key("fail")
  final case class Failed(exception: String) extends FunctionResult

  object Succeed {
    implicit val rw: RW[Succeed] = macroRW
  }

  object Failed {
    implicit val rw: RW[Failed] = macroRW
  }
}