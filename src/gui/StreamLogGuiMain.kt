import gui.ReadFileThread
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.application.Platform
import javafx.scene.control.TextArea
import java.util.TimerTask
import java.util.Timer


class StreamLogGuiMain : Application() {
    private var readFilePath: TextField = TextField("")
    private var eventStartButton: Button = Button("Click")
    private var readThread: ReadFileThread? = null
    private var logConsole: TextArea = TextArea("")
    val period = 100L

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Log Read"

        val args: MutableList<String> = parameters.raw

        if (args.size > 0) {
            readFilePath.text = args[0]
        } else {
            readFilePath.text = ""
        }

        val pane = BorderPane()

        pane.top = readFilePath
        pane.center = logConsole
        pane.bottom = eventStartButton
        val scene = Scene(pane, 600.0, 400.0)

        eventStartButton.setOnMouseClicked {
            val filePath: String = readFilePath.text ?: ""
            if (!filePath.isEmpty()) {
                eventStartButton.text = "Started."

                readThread = ReadFileThread(filePath)
                readThread?.start()
            }
        }
        primaryStage.scene = scene
        primaryStage.show()

        val timer = Timer()

        timer.schedule(object : TimerTask() {
            override fun run() {
                Platform.runLater({
                    if (readThread?.hasUpdate() ?: false) {
                        val list = readThread?.getUpdated()
                        var text:String = logConsole.text.toString()
                        if (list != null) {
                            for (line:String in list) {
                                text += line + "\n"
                            }
                        }
                        logConsole.text = text
                    }
                })
            }
        }, 0, period)
    }
}

fun main(args: Array<String>) {
    Application.launch(StreamLogGuiMain::class.java, *args)
}