package yami.ui

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.FileChooser
import javafx.stage.Stage
import yami.model.Context
import java.io.File
import java.net.URL
import java.util.*

class SelectorOfPeople(private val stage: Stage, private val context: Context) : Initializable, Starter {

    @FXML
    private lateinit var tipLabel: Label

    @FXML
    private lateinit var nextBtn: Button

    @FXML
    private lateinit var selectBtn: Button

    private var xlsFile: File? = null

    override fun layout(): String = "layout_people_selector.fxml"

    override fun stage(): Stage = stage

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        if (context.peopleXls != null) {
            xlsFile = context.peopleXls
            tipLabel.text = xlsFile!!.path
            nextBtn.isDisable = false
            selectBtn.text = "重选"
        }
    }

    @FXML
    private fun select() {
        val fileChooser = FileChooser()
        fileChooser.title = "选择Excel文件"
        val file = fileChooser.showOpenDialog(Stage())
        if (file != null) {
            xlsFile = file
            tipLabel.text = file.path
            nextBtn.isDisable = false
            selectBtn.text = "重选"
        }
    }

    @FXML
    private fun cancel() {
        Controller(stage).start()
    }

    @FXML
    private fun last() {
        SelectorOfData(stage, context).start()
    }

    @FXML
    private fun next() {
        context.peopleXls = xlsFile
        SelectorOfEnd(stage, context).start()
    }
}