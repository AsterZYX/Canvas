package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainUIController {

    @FXML
    private BorderPane pane;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }

    //显示初始界面
    public void showLogin(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/view/login.fxml"));
            Node node = fxmlLoader.load();

            LoginController loginController = fxmlLoader.getController();
            loginController.setMainUIController(this);

            pane.setCenter(node);
            primaryStage.setMaxHeight(516);
            primaryStage.setMaxWidth(772);
            primaryStage.setHeight(516);
            primaryStage.setWidth(772);
            primaryStage.setX((Screen.getPrimary().getBounds().getMaxX()-772)/2);
            primaryStage.setY((Screen.getPrimary().getBounds().getMaxY()-516)/2);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //显示画板界面
    public void showDrawBoard(String picName){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/view/draw.fxml"));
            Pane node = fxmlLoader.load();

            DrawController drawController = fxmlLoader.getController();
            drawController.setMainUIController(this);
            if(picName!=null && !picName.isEmpty()){
                drawController.setCanvas(picName);
            }

            pane.getChildren().removeAll();
            pane.setCenter(node);
            primaryStage.setMinHeight(913);
            primaryStage.setMinWidth(1435);
            primaryStage.setHeight(913);
            primaryStage.setWidth(1435);
            primaryStage.setX((Screen.getPrimary().getBounds().getMaxX()-1435)/2);
            primaryStage.setY((Screen.getPrimary().getBounds().getMaxY()-913)/2);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //显示我的图片界面
    public void showMyPage(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/view/page.fxml"));
            Pane node = fxmlLoader.load();

            PageController pageController = fxmlLoader.getController();
            pageController.setMainUIController(this);

            pane.getChildren().removeAll();
            pane.setCenter(node);
            primaryStage.setMinHeight(646);
            primaryStage.setMinWidth(1039);
            primaryStage.setHeight(646);
            primaryStage.setWidth(1039);
            primaryStage.setX((Screen.getPrimary().getBounds().getMaxX()-1039)/2);
            primaryStage.setY((Screen.getPrimary().getBounds().getMaxY()-646)/2);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //最小化窗口
    public void minimizeWindow(){
        primaryStage.setIconified(true);
    }

    //关闭窗口
    public void closeWindow(){
        Dialog alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("确定退出本系统吗？");
        alert.setTitle("Canvas");
        Node cancelButton = alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null)
            cancelButton.setId("CancelButton");
        alert.getDialogPane().getStylesheets().add("/style/dialog.css");
        Optional result = alert.showAndWait();


        if (result.isPresent()){
            if (result.get() == ButtonType.OK) {
                primaryStage.close();
                System.exit(0);
            }
        }
    }

}
