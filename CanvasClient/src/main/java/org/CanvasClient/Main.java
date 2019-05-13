package org.CanvasClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ui.MainUIController;

import java.io.IOException;

public class Main extends Application {

    private MainUIController mainUIController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = null;
        try{
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/view/main.fxml"));
            root = fxmlLoader.load();
            mainUIController = fxmlLoader.getController();
            mainUIController.setPrimaryStage(primaryStage);
            mainUIController.showLogin();
        }catch (IOException e){
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }
}
