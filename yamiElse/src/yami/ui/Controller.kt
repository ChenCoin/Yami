package yami.ui;

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import yami.model.Context
import java.net.URL
import java.util.*

class Controller(private val stage: Stage) : Initializable, Starter {

    @FXML
    private lateinit var box1: VBox
    @FXML
    private lateinit var box2: StackPane
    @FXML
    private lateinit var box3: VBox

    override fun layout(): String = "layout_index.fxml"

    override fun stage(): Stage = stage

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    @FXML
    private fun next() {
        SelectorOfData(stage, Context()).start()
    }

    @FXML
    private fun showBox1() {
        box1.isVisible = true
        box2.isVisible = false
        box3.isVisible = false
    }

    @FXML
    private fun showBox2() {
        box1.isVisible = false
        box2.isVisible = true
        box3.isVisible = false
    }

    @FXML
    private fun showBox3() {
        box1.isVisible = false
        box2.isVisible = false
        box3.isVisible = true
    }

}
