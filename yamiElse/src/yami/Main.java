package yami;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yami.ui.Controller;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("慧博考勤管理系统");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("ui/icon.png")));
        new Controller(primaryStage).start();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
