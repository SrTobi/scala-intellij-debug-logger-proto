package com.github.srtobi

import upickle.default.writeTo

import java.io.PrintWriter

private class DebugLogger(writer: java.io.Writer) {
  def log(msg: String, values: Seq[sourcecode.Text[Any]]): Unit = {
    writeMsg(LogKind.Msg(msg, values.map(text => text.source -> text.value.toString)))
  }

  def func[T](body: => T, args: sourcecode.Args): T = {
    val argValues = args.value.flatten.map(s => (s.source, s.value.toString))
    writeMsg(LogKind.FunctionStart(argValues))

    try {
      val result = body
      writeMsg(LogKind.FunctionEnd(FunctionResult.Succeed(result.toString)))
      result
    } catch {
      case e: Throwable =>
        writeMsg(LogKind.FunctionEnd(FunctionResult.Failed(e.toString)))
        throw e
    }
  }

  private[this] def writeMsg(kind: LogKind): Unit = {
    val stackTrace = Thread.currentThread().getStackTrace.toSeq.drop(4).map {
      e => StackTraceEntry(e.getMethodName, e.getClassName, e.getLineNumber)
    }
    write(DebugLogMsg(kind, stackTrace))
  }

  private[this] def write(msg: DebugLogMsg): Unit = {
    writeTo(msg, writer)
    writer.append('\n')
    writer.flush()
  }
}

object DebugLogger {
  private val loggers = ThreadLocal.withInitial[DebugLogger](() => new DebugLogger(new PrintWriter(System.out)))

  def log(msg: => String, values: sourcecode.Text[Any]*): Unit =
    loggers.get().log(msg, values)

  def record(values: sourcecode.Text[Any]*): Unit =
    loggers.get().log("", values)

  def func[T](body: => T)(implicit args: sourcecode.Args): T =
    loggers.get().func(body, args)
}