package com.github.srtobi.web

import com.github.srtobi.{FunctionResult, LogKind, DebugLogMsg, StackTraceEntry}
import com.thoughtworks.binding.Binding.Var
import upickle.default.read

import scala.annotation.tailrec

sealed trait Log {
  def stackTrace: Seq[StackTraceEntry]

  def currentFunction: String = {
    val e = stackTrace.head
    val className = e.className.split('.').last
    s"$className.${e.method}"
  }

  def contains(txt: String): Boolean

  def accInner: Int
}

object Log {
  final case class Frame(args: Seq[(String, String)],
                         result: FunctionResult,
                         inners: Seq[Log],
                         stackTrace: Seq[StackTraceEntry]) extends Log {
    val open: Var[Boolean] = Var(false)

    def signatureString: String = {
      val args = this.args.map { case (name, value) => s"$name = $value"}.mkString(", ")
      if (args.isEmpty) s"$currentFunction $htmlArrowRight $resultString"
      else s"$currentFunction($args) $htmlArrowRight $resultString"
    }

    def resultString: String = {
      result match {
        case FunctionResult.Succeed(result) => result
        case FunctionResult.Failed(msg) => s"threw $msg"
      }
    }

    def contains(txt: String): Boolean =
      args.exists { case (a, b) => a.contains(txt) || b.contains(txt) } || (result match {
        case FunctionResult.Succeed(result) => result.contains(txt)
        case FunctionResult.Failed(result) => result.contains(txt)
      })

    override def accInner: Int = 1 + inners.map(_.accInner).sum
  }

  final case class Msg(msg: String,
                       variables: Seq[(String, String)],
                       stackTrace: Seq[StackTraceEntry]) extends Log {

    def contains(txt: String): Boolean =
      msg.contains(txt) || variables.exists { case (a, b) => a.contains(txt) || b.contains(txt) }

    override def accInner: Int = 0
  }

  def fromText(text: String): Seq[Log] =
    fromLines(text.linesIterator)

  def fromLines(lines: IterableOnce[String]): Seq[Log] = {
    val it = lines.iterator

    def convertFrame(): (Option[FunctionResult], Seq[Log]) = {
      val builder = Seq.newBuilder[Log]

      @tailrec
      def convertNext(): Option[FunctionResult] = {
        it.nextOption().map(read[DebugLogMsg](_)) match {
          case None => None
          case Some(DebugLogMsg(LogKind.Msg(msg, variables), st)) =>
            builder += Msg(msg, variables, st)
            convertNext()
          case Some(DebugLogMsg(LogKind.FunctionStart(args), st)) =>
            val (end, inners) = convertFrame()
            val result = end.getOrElse(FunctionResult.Failed("<missing>"))
            builder += Frame(args, result, inners, st)
            convertNext()
          case Some(DebugLogMsg(LogKind.FunctionEnd(result), _)) =>
            Some(result)
        }
      }

      (convertNext(), builder.result())
    }

    convertFrame()._2
  }
}