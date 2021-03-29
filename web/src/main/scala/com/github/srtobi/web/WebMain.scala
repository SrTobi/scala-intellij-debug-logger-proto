package com.github.srtobi.web

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Constants, Var}
import org.lrng.binding.html
import org.lrng.binding.html.NodeBinding
import org.scalajs.dom.document
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{Event, HTMLInputElement, HTMLTextAreaElement, Node}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object WebMain {

  @html
  private def renderFrame(frame: Log.Frame, depth: Int): NodeBinding[Div] = {
    val color = Binding(
      if (frame.inners.isEmpty) "white"
      else if (frame.open.bind) colorInInfiniteColorSpectrum(depth)
      else "lightgrey"
    )

    val toggle = (e: Event) => frame.open.value = !frame.open.value

    {
      <div>
        <div style={s"background-color: ${color.bind}; cursor: pointer; padding: 2px; margin-top: 1px;"} onclick={toggle}>
          {
            frame.signatureString
          }
        </div>
        <div style={(!frame.open.bind || frame.inners.isEmpty).pen("display: none")}>
          <span style={s"display: flex"}>
            <div
              style={s"background-color: ${color.bind}; width: 20px; cursor: pointer"}
              onclick={toggle}
              >
              &nbsp;
            </div>
            <div>
              {
                renderMessages(frame.inners, depth + 1)
              }
            </div>
          </span>
          <div style={s"background-color: ${color.bind}; cursor: pointer; padding: 2px; margin-bottom: 1px;"} onclick={toggle}>
            {
            s"$htmlArrowRight ${frame.resultString}"
            }
          </div>
        </div>
      </div>
    }
  }

  @html
  private def renderMsg(msg: Log.Msg, depth: Int): NodeBinding[Div] = {
    <div style="padding: 2px; margin: 2px;">{msg.msg} ({msg.variables.map { case (name, value) => s"$name = $value"}.mkString(", ") })</div>
  }

  @html
  private def renderMessages(messages: Seq[Log], depths: Int)=
    for (msg <- Constants(messages: _*)) yield {
      msg match {
        case frame: Log.Frame => renderFrame(frame, depths).bind
        case msg: Log.Msg => renderMsg(msg, depths).bind
      }
    }

  @html
  private def startupView: NodeBinding[Div] = {
    val onclick = (_: Event) => {
      val text = document.getElementById("log-input").asInstanceOf[HTMLTextAreaElement].value.trim
      log.value = Some(Log.fromText(text))
    }
    <div>
      <textarea id="log-input"></textarea>
      <input type="button" value="Enter" onclick={onclick} />
    </div>
  }

  private val log: Var[Option[Seq[Log]]] = Var(None)

  private def doSearch(txt: String): Unit = {
    def search(log: Log): Boolean = {
      val foundHere = log.contains(txt)
      log match {
        case frame: Log.Frame =>
          val found = searchMulti(frame.inners) || foundHere
          frame.open.value = found
          found
        case _: Log.Msg => foundHere
      }
    }

    def searchMulti(log: Seq[Log]): Boolean =
      log.foldRight(false)(search(_) || _)

    log.value.foreach(searchMulti)
  }

  @html
  private def mainDiv: NodeBinding[Div] = {
    <div>
      {
        log.bind match {
          case Some(log) =>
            <div>
              {
                renderMessages(log, 0)
              }
              <div style="position: fixed; bottom: 2px; right: 2px; width: 400px; height: 100px; background-color: lightgrey; padding: 5px;">
                <input type="text" value="" id="search-input" />
                <input type="button" value="Search" onclick={(e: Event) => doSearch(document.getElementById("search-input").asInstanceOf[HTMLInputElement].value)} />
              </div>
            </div>
          case None =>
            startupView
        }
      }
    </div>
  }
  @JSExport
  def main(container: Node): Unit = {
    html.render(container, mainDiv)
  }

  val example =
    """
      |{"kind":{"$type":"func-start","args":[["x","5"]]},"stackTrace":[{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"msg","msg":"before adding 5","variables":[["blub","5"]]},"stackTrace":[{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":6},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"msg","msg":"after adding 5","variables":[["blub","10"]]},"stackTrace":[{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":8},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"func-start","args":[["x","0"]]},"stackTrace":[{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":11},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"msg","msg":"before adding 5","variables":[["blub","0"]]},"stackTrace":[{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":6},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":11},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"msg","msg":"after adding 5","variables":[["blub","5"]]},"stackTrace":[{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":8},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":11},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"func-end","result":{"$type":"succ","result":"5"}},"stackTrace":[{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"$anonfun$test$1","className":"com.github.srtobi.Playground$","line":11},{"method":"apply","className":"scala.runtime.java8.JFunction0$mcI$sp","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger","line":17},{"method":"func","className":"com.github.srtobi.DebugLogger$","line":48},{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |{"kind":{"$type":"func-end","result":{"$type":"succ","result":"15"}},"stackTrace":[{"method":"test","className":"com.github.srtobi.Playground$","line":4},{"method":"main","className":"com.github.srtobi.Playground$","line":15},{"method":"main","className":"com.github.srtobi.Playground","line":-1}]}
      |""".stripMargin.trim
}
