package yami.ui

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.util.Callback

interface Starter {

    fun layout(): String

    fun stage(): Stage

    fun start() {
        val loader = FXMLLoader(javaClass.getResource(layout()))
        loader.controllerFactory = Callback { this }
        stage().scene = Scene(loader.load(), stage().scene?.width ?: -1.0,
                stage().scene?.height ?: -1.0)
    }

    fun log(content: String) {
        println(content)
    }
}