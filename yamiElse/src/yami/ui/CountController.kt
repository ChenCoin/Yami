package yami.ui

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.stage.Stage
import yami.model.Context
import yami.model.Reader
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class CountController(private val stage: Stage, private val context: Context) : Initializable, Starter {

    @FXML
    private lateinit var progress: ProgressBar

    @FXML
    private lateinit var progressLabel: Label

    @FXML
    private lateinit var board: Label

    @FXML
    private lateinit var mainBtn: Button

    private val textList = ArrayList<String>()

    override fun layout(): String = "layout_count.fxml"

    override fun stage(): Stage = stage

    private lateinit var reader: Reader

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        progress.progress = 0.0
        progressLabel.text = "0%"
        mainBtn.setOnMouseClicked { cancel() }
        Thread {
            reader = Reader(context) { now, content ->
                Platform.runLater {
                    if (now > 0) {
                        progress.progress = now.toDouble() / 100
                        progressLabel.text = "$now%"
                        if (now == 100) {
                            mainBtn.setOnMouseClicked { home() }
                            mainBtn.text = "返回"
                        }
                    } else {
                        progress.isVisible = false
                        progressLabel.isVisible = false
                    }
                }
                if (content.isNotEmpty()) {
                    textList.add(content)
                    refreshText()
                }
            }
            reader.read()
        }.start()
    }

    private fun refreshText() {
        Platform.runLater { board.text = textList.reduce { str1, str2 -> str1 + "\n" + str2 } }
    }

    private fun cancel() {
        reader.cancel()
        mainBtn.setOnMouseClicked { home() }
        mainBtn.text = "返回"
    }

    private fun home() {
        Controller(stage).start()
    }
}