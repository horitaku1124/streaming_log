package com.ict_start.kotlin.gui

import com.ict_start.kotlin.db.DBManager
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.util.*
import javafx.stage.FileChooser
import java.sql.Connection
import javafx.scene.control.ComboBox
import javafx.collections.FXCollections
import java.sql.PreparedStatement
import java.sql.Statement
import kotlin.collections.ArrayList


class StreamLogGuiMain : Application() {
  private var historyButton: Button = Button("Hist")
  private var readFilePath: TextField = TextField("")
  private var eventStartButton: Button = Button("Start")
  private var fileOpenButton: Button = Button("Open")
  private var clearButton: Button = Button("Clear")
  private var closeButton: Button = Button("Close")
  private var readThread: ReadFileThread? = null
  private var logConsole: TextArea = TextArea("")
  private val period = 100L
  private var conn: Connection? = null

  override fun start(primaryStage: Stage) {
    val db = DBManager("test.db")
    conn = db.createConnection()
    primaryStage.title = "Log Read"

    var smt: Statement = conn?.createStatement() ?: throw RuntimeException("failed to create");

//    smt.executeUpdate("DROP TABLE IF EXISTS `history`")
    smt.executeUpdate("CREATE TABLE IF NOT EXISTS `history` (id integer primary key, path text unique)")
    smt.close()

    val args: MutableList<String> = parameters.raw
    readFilePath.text = if(args.size > 0) args[0] else ""

    val topPane = BorderPane()
    topPane.left = historyButton
    topPane.center = readFilePath
    topPane.right = fileOpenButton
    val pane = BorderPane()
    val buttonPane = BorderPane()
    buttonPane.left = eventStartButton
    buttonPane.center = clearButton
    buttonPane.right = closeButton

    pane.top = topPane
    pane.center = logConsole
    pane.bottom = buttonPane
    val scene = Scene(pane, 600.0, 400.0)
    logConsole.isWrapText = true

    val fileChooser = FileChooser()
    historyButton.setOnMouseClicked {

      var sql = "select path from `history`"
      var smt: Statement = conn?.createStatement() ?: throw RuntimeException("failed to create")
      var rs = smt.executeQuery(sql)
      var history = ArrayList<String>()
      while (rs.next()) {
        history.add(rs.getString(1))
      }

      val options = FXCollections.observableArrayList(history)
      val comboBox = ComboBox(options)
      var histStage = Stage()
      var completeButton = Button("Complete")
      completeButton.setOnMouseClicked {
        var option = comboBox.selectionModel.selectedItem.toString()
        readFilePath.text = option
        histStage.close()
      }
      val histPane = BorderPane()
      histPane.center = comboBox
      histPane.right = completeButton
      histStage.scene = Scene(histPane, 600.0, 400.0)
      histStage.show()
    }

    readFilePath.textProperty().addListener { _, _, newText ->
      readFilePath.text = newText
              .replace("\\", "/")
              .replace("^[ \t\r\n]".toRegex(), "")
              .replace("[ \t\r\n]$".toRegex(), "")
    }

    fileOpenButton.setOnMouseClicked {
      val file = fileChooser.showOpenDialog(primaryStage);
      if (file != null) {
        readFilePath.text = file.absolutePath
      }
    }

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

        var sql = "select id from `history` where path = ? "
        var smt: PreparedStatement = conn?.prepareStatement(sql) ?: throw RuntimeException("failed to create")
        smt.setString(1, filePath)
        var rs = smt.executeQuery()
        if (!rs.next()) {
          var sql2 = "insert into `history`(path) values (?) "
          var smt2: PreparedStatement = conn?.prepareStatement(sql2) ?: throw RuntimeException("failed to create")
          smt2.setString(1, filePath)
          smt2.executeUpdate()
          smt2.close()
        }
        smt.close()
      }
    }
    clearButton.setOnMouseClicked {
        logConsole.text = ""
    }
    closeButton.setOnMouseClicked {
      if (readThread != null) {
        readThread?.isReading = false
      }
      conn?.close()
      Platform.exit()
      System.exit(0)
    }
    primaryStage.scene = scene
    primaryStage.show()

    val timer = Timer()

    timer.schedule(object : TimerTask() {
      override fun run() {
        Platform.runLater {
          if (readThread != null) {
            val threadIsReading = readThread?.isReading ?: false
                    && readThread?.isAlive ?: false
            if (threadIsReading) {
              if (readThread?.hasUpdate() == true) {
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
        }
      }
    }, 0, period)
  }
}

fun main(args: Array<String>) {
  Application.launch(StreamLogGuiMain::class.java, *args)
}