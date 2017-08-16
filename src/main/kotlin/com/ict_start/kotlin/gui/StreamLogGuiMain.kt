package com.ict_start.kotlin.gui

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.util.*

class StreamLogGuiMain : Application() {
  private var readFilePath: TextField = TextField("")
  private var eventStartButton: Button = Button("Start")
  private var clearButton: Button = Button("Clear")
  private var closeButton: Button = Button("Close")
  private var readThread: ReadFileThread? = null
  private var logConsole: TextArea = TextArea("")
  val period = 100L

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Log Read"

    val args: MutableList<String> = parameters.raw
    readFilePath.text = if(args.size > 0) args[0] else ""

    val pane = BorderPane()
    val buttonPane = BorderPane()
    buttonPane.left = eventStartButton
    buttonPane.center = clearButton
    buttonPane.right = closeButton

    pane.top = readFilePath
    pane.center = logConsole
    pane.bottom = buttonPane
    val scene = Scene(pane, 600.0, 400.0)
    logConsole.isWrapText = true

    eventStartButton.setOnMouseClicked {
      val threadIsReading = readThread?.isReading ?: false
              && readThread?.isAlive ?: false

      if (threadIsReading) {
        readThread?.isReading = false
      } else {
        logConsole.text = ""
        val filePath: String = readFilePath.text ?: ""
        if (!filePath.isEmpty()) {
          eventStartButton.text = "Started."

          readThread = ReadFileThread(filePath)
          readThread?.start()
        }
      }
    }
    clearButton.setOnMouseClicked {
        logConsole.text = ""
    }
    closeButton.setOnMouseClicked {
      if (readThread != null) {
        readThread?.isReading = false
      }
      Platform.exit()
      System.exit(0)
    }
    primaryStage.scene = scene
    primaryStage.show()

    val timer = Timer()

    timer.schedule(object : TimerTask() {
      override fun run() {
        Platform.runLater({
          if (readThread != null) {
            val threadIsReading = readThread?.isReading ?: false
                    && readThread?.isAlive ?: false
            if (threadIsReading) {
              if (readThread?.hasUpdate() ?: false) {
                val list = readThread?.getUpdated()
                if (list != null) {
                  for (line:String in list) {
                    logConsole.appendText(line)
                    logConsole.appendText("\n")
                  }
                }
              }
            } else {
              readThread?.isReading = false
              readThread = null
              eventStartButton.text = "Click"
            }
          }
        })
      }
    }, 0, period)
  }
}

fun main(args: Array<String>) {
  Application.launch(StreamLogGuiMain::class.java, *args)
}