package yami.ui

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.stage.Stage
import yami.model.Context
import java.net.URL
import java.util.*

class SelectorOfEnd(private val stage: Stage, private val context: Context) : Initializable, Starter {

    @FXML
    private lateinit var label1: Label

    @FXML
    private lateinit var label2: Label

    override fun layout(): String = "layout_end_selector.fxml"

    override fun stage(): Stage = stage

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        label1.text = context.dataXls!!.path
        label2.text = context.peopleXls!!.path
    }

    @FXML
    private fun cancel() {
        Controller(stage).start()
    }

    @FXML
    private fun last() {
        SelectorOfPeople(stage, context).start()
    }

    @FXML
    private fun next() {
        CountController(stage, context).start()
    }
}